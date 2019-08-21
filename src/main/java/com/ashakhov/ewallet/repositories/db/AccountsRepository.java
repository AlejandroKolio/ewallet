package com.ashakhov.ewallet.repositories.db;

import com.ashakhov.ewallet.exceptions.AccountNotFoundException;
import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.repositories.AbstractRepository;
import com.devskiller.friendly_id.FriendlyId;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class AccountsRepository extends AbstractRepository {
    private static AccountsRepository instance;

    public static AccountsRepository getInstance(@NonNull Vertx vertx) {
        if (instance == null) {
            instance = new AccountsRepository(vertx);
        }
        return instance;
    }

    private AccountsRepository(@NonNull Vertx vertx) {
        super(vertx);
    }

    public void insert(@NonNull Account account, @NonNull SQLConnection connection,
            @NonNull Handler<AsyncResult<Account>> handler) {
/*        final String accountId = FriendlyId.createFriendlyId();

        jdbc.rxGetConnection().flatMap(connection -> {
            final Single<ResultSet> resultSetSingle = connection.rxQueryWithParams(
                    "INSERT INTO Accounts (accountId, username, balance, currency) VALUES (?,?,?,?)",
                    new JsonArray()
                            .add(accountId)
                            .add(account.getUsername())
                            .add(account.getBalance())
                            .add(account.getCurrency()));
            return resultSetSingle.doAfterTerminate(connection::close);
        }).subscribe(resultSet -> {
            final Optional<String> json = resultSet.getRows()
                    .stream()
                    .findAny()
                    .map(JsonObject::encode);
            context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(Json.encodePrettily(json));
        }, err -> log.error("Error occured during request {}", err.getMessage()));*/

        // @formatter:off
        final String accountId = FriendlyId.createFriendlyId();
        final String sql = "INSERT INTO Accounts (" +
                "accountId, " +
                "username, " +
                "balance, " +
                "currency) VALUES (?, ?, ?, ?)";
        connection.updateWithParams(sql,
                new JsonArray()
                        .add(accountId)
                        .add(account.getUsername())
                        .add(account.getBalance())
                        .add(account.getCurrency()), (ar) -> {
                    if (ar.failed()) {
                        handler.handle(Future.failedFuture(ar.cause()));
                        connection.close();
                        return;
                    }
                    ar.result();

                    final Account acc = new Account(
                            accountId,
                            account.getUsername(),
                            account.getBalance(),
                            account.getCurrency());
                    handler.handle(Future.succeededFuture(acc));
                });
        // @formatter:on
    }

    public void select(@NonNull String id, @NonNull SQLConnection connection,
            @NonNull Handler<AsyncResult<Account>> handler) {
        connection.queryWithParams("SELECT * FROM Accounts WHERE accountId=?", new JsonArray().add(id), asyncResult -> {
            if (asyncResult.failed()) {
                handler.handle(Future.failedFuture("Account is not found"));
            } else {
                if (asyncResult.result().getNumRows() >= 1) {
                    final Account account = asyncResult.result()
                            .getRows()
                            .stream()
                            .findAny()
                            .map(Account::new)
                            .orElseThrow(() -> new ApiClientException("Failed to retrieve account from DB"));
                    handler.handle(Future.succeededFuture(account));
                } else {
                    handler.handle(Future.failedFuture(new AccountNotFoundException(String.format("Account %s is not found", id))));
                }
            }
        });
    }

    public void selectOne(@NonNull String id, @NonNull RoutingContext context) {
/*        jdbc.rxGetConnection().flatMap(connection -> {
            final Single<ResultSet> resultSetSingle = connection.rxQueryWithParams(
                    "SELECT * FROM Accounts WHERE accountId=?", new JsonArray().add(id));
            return resultSetSingle.doAfterTerminate(connection::close);
        }).subscribe(resultSet -> {
            final Optional<String> json = resultSet.getRows().stream().findAny().map(JsonObject::encode);
            context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(Json.encodePrettily(json));
        }, err -> log.error("Error occured during request {}", err.getMessage()));*/

        jdbc.getConnection(asyncResult -> {
            final SQLConnection connection = asyncResult.result();
            select(id, connection, (r) -> context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(Json.encodePrettily(r.result())));
        });
    }
}
