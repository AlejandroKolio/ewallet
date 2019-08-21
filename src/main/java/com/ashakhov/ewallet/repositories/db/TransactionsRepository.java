package com.ashakhov.ewallet.repositories.db;

import static java.util.stream.Collectors.toList;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.InsufficientBalanceException;
import com.ashakhov.ewallet.exceptions.TransactionNotFoundException;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.Status;
import com.ashakhov.ewallet.models.Transaction;
import com.ashakhov.ewallet.repositories.AbstractRepository;
import com.ashakhov.ewallet.utils.EWalletHandler;
import com.devskiller.friendly_id.FriendlyId;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class TransactionsRepository extends AbstractRepository {

    private static final String INSERT_TRANSACTION = "INSERT INTO Transactions (transactionId, fromAccountId, " +
            "toAccountId, amount, currency, createdOn, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_FROM = "SELECT * FROM Accounts WHERE accountId=? AND balance>=?";
    private static final String SELECT_TO = "SELECT * FROM Accounts WHERE accountId=?";
    private static final String UPDATE_FROM = "UPDATE Accounts SET balance=balance-? WHERE accountId=?";
    private static final String UPDATE_TO = "UPDATE Accounts SET balance = balance+? WHERE accountId=?";

    private static TransactionsRepository instance;

    public static TransactionsRepository getInstance(@NonNull Vertx vertx) {
        if (instance == null) {
            instance = new TransactionsRepository(vertx);
        }
        return instance;
    }

    private TransactionsRepository(@NonNull Vertx vertx) {
        super(vertx);
    }

    public void insert(@NonNull Transaction transaction, @NonNull SQLConnection connection,
            @NonNull Handler<AsyncResult<Transaction>> handler) {
        connection.queryWithParams(SELECT_FROM,
                new JsonArray().add(transaction.getFromAccountId()).add(transaction.getAmount()),
                getAccountAsyncResultHandler(handler))
                .queryWithParams(SELECT_TO, new JsonArray().add(transaction.getToAccountId()),
                        getAccountAsyncResultHandler(handler))
                .updateWithParams(UPDATE_FROM,
                        new JsonArray().add(transaction.getAmount()).add(transaction.getFromAccountId()), result -> {
                            if (result.succeeded()) {
                                log.info(result.result().toString());
                            } else {
                                log.error(result.cause().getMessage());
                            }
                        })
                .updateWithParams(UPDATE_TO,
                        new JsonArray().add(transaction.getAmount()).add(transaction.getToAccountId()), result -> {
                            if (result.succeeded()) {
                                log.info(result.result().toString());
                            } else {
                                log.error(result.cause().getMessage());
                            }
                        })
                .setAutoCommit(false, h -> {
                    if (h.failed()) {
                        saveTransaction(transaction, connection, handler, Status.FAILED);
                        handler.handle(Future.failedFuture(h.cause()));
                        log.error(h.cause().getMessage());
                    } else {
                        handler.handle(Future.succeededFuture());
                        saveTransaction(transaction, connection, handler, Status.SUCCESS);
                    }
                });
    }

    private void saveTransaction(@NonNull Transaction transaction, @NonNull SQLConnection connection,
            @NonNull Handler<AsyncResult<Transaction>> handler, @NonNull Status status) {
        // @formatter:off
        final String transactionId = FriendlyId.createFriendlyId();
        final String message = "Complete";
        final Instant createdOn = Instant.now();
        connection.updateWithParams(INSERT_TRANSACTION,
                new JsonArray()
                        .add(transactionId)
                        .add(transaction.getFromAccountId())
                        .add(transaction.getToAccountId())
                        .add(transaction.getAmount())
                        .add(transaction.getCurrency())
                        .add(createdOn)
                        .add(status)
                        .add(message), (ar) -> {
                    if (ar.failed()) {
                        handler.handle(Future.failedFuture(ar.cause()));
                        connection.close();
                        return;
                    }
                    ar.result();
                    final Transaction trns = new Transaction(
                            transactionId,
                            transaction.getFromAccountId(),
                            transaction.getToAccountId(),
                            transaction.getAmount(),
                            transaction.getCurrency(),
                            createdOn,
                            status,
                            message);
                    handler.handle(Future.succeededFuture(trns));
                });
        // @formatter:on
    }

    private Handler<AsyncResult<ResultSet>> getAccountAsyncResultHandler(
            @NonNull Handler<AsyncResult<Transaction>> handler) {
        return result -> {
            if (result.failed()) {
                handler.handle(Future.failedFuture(result.cause()));
            } else {
                final List<Account> accounts = result.result().getRows().stream().map(Account::new).collect(toList());
                if (accounts.isEmpty()) {
                    throw new ApiClientException("Insufficient balance or AccountId not found");
                }
                log.info(accounts.toString());
            }
        };
    }

    public void select(@NonNull String id, @NonNull SQLConnection connection,
            @NonNull Handler<AsyncResult<Transaction>> handler) {
        connection.queryWithParams("SELECT * FROM Transactions WHERE transactionId=?", new JsonArray().add(id),
                asyncResult -> {
                    if (asyncResult.failed()) {
                        handler.handle(Future.failedFuture("Transaction is not found"));
                    } else {
                        if (asyncResult.result().getNumRows() >= 1) {
                            final Transaction transaction = asyncResult.result()
                                    .getRows()
                                    .stream()
                                    .findAny()
                                    .map(Transaction::new)
                                    .orElseThrow(
                                            () -> new ApiClientException("Failed to retrieve saveTransaction from DB"));
                            handler.handle(Future.succeededFuture(transaction));
                        } else {
                            handler.handle(Future.failedFuture(new TransactionNotFoundException(
                                    String.format("Transaction %s is not found", id))));
                        }
                    }
                });
    }

    public void selectOne(@NonNull String id, @NonNull RoutingContext context) {
        jdbc.getConnection(asyncResult -> {
            final SQLConnection connection = asyncResult.result();
            select(id, connection, (r) -> context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(Json.encodePrettily(r.result())));
        });
    }
}
