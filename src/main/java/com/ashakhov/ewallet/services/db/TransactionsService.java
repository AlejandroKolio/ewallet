package com.ashakhov.ewallet.services.db;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.InsufficientBalanceException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Transaction;
import com.ashakhov.ewallet.repositories.db.TransactionsRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class TransactionsService {
    @NonNull
    private final TransactionsRepository repository;

    private static TransactionsService instance;

    public static TransactionsService getInstance(@NonNull Vertx vertx) {
        if (instance == null) {
            instance = new TransactionsService(vertx);
        }
        return instance;
    }

    private TransactionsService(@NonNull Vertx vertx) {
        repository = TransactionsRepository.getInstance(vertx);
    }

    public void createTransaction(@NonNull RoutingContext context) {
        repository.getJdbc().getConnection(asyncResult -> {
            try {
                final Transaction transaction = Json.decodeValue(context.getBodyAsString(), Transaction.class);
                final SQLConnection connection = asyncResult.result();

                repository.insert(transaction, connection, (r) -> context.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                        .end(Json.encodePrettily(r.result())));
                connection.close();
            } catch (DecodeException | InsufficientBalanceException ex) {
                context.fail(ErrorCodes.BAD_REQUEST.getCode(),
                        new ApiClientException(String.format("Provided json body is in sufficient '%s'", ex.getMessage())));
            }
        });
    }

    public void getTransactions(@NonNull RoutingContext context) {
        repository.getJdbc().getConnection(asyncResult -> {
            final SQLConnection connection = asyncResult.result();
            connection.query("SELECT * FROM Transactions", result -> {
                final List<Transaction> transactions = result.result()
                        .getRows()
                        .stream()
                        .map(Transaction::new)
                        .collect(Collectors.toList());
                context.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                        .end(Json.encodePrettily(transactions));
                connection.close();
            });
        });
    }

    public void getTransactionById(@NonNull RoutingContext context) {
        final String transactionId = context.pathParam("transactionId");
        if (Objects.isNull(transactionId)) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        } else {
            repository.selectOne(transactionId, context);
        }
    }
}
