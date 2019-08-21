package com.ashakhov.ewallet.services.db;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.repositories.db.AccountsRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class AccountsService {
    @NonNull
    private final AccountsRepository repository;

    private static AccountsService instance;

    public static AccountsService getInstance(@NonNull Vertx vertx) {
        if (instance == null) {
            instance = new AccountsService(vertx);
        }
        return instance;
    }

    private AccountsService(@NonNull Vertx vertx) {
        repository = AccountsRepository.getInstance(vertx);
    }

    public void createAccount(@NonNull RoutingContext context) {
        /*final Account account = Json.decodeValue(context.getBodyAsString(), Account.class);
        repository.insert(account, context);*/
        repository.getJdbc().getConnection(asyncResult -> {
            try {
                final Account account = Json.decodeValue(context.getBodyAsString(), Account.class);
                final SQLConnection connection = asyncResult.result();

                repository.insert(account, connection, (r) -> context.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                        .end(Json.encodePrettily(r.result())));
                connection.close();
            } catch (DecodeException ex) {
                context.fail(ErrorCodes.BAD_REQUEST.getCode(), new ApiClientException(
                        String.format("Provided json body is in sufficient '%s'", ex.getMessage())));
            }
        });
    }

    public void getAccounts(@NonNull RoutingContext context) {
/*        repository.getJdbc().rxGetConnection().flatMap(connection -> {
            final Single<ResultSet> resultSetSingle = connection.rxQuery("SELECT * FROM Accounts");
            return resultSetSingle.doAfterTerminate(connection::close);
        }).subscribe(resultSet -> {
            final List<Account> accounts = resultSet.getRows().stream().map(Account::new).collect(toList());
            context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(Json.encodePrettily(accounts));
        }, err -> log.error("Error occured during request {}", err.getMessage()));*/

        repository.getJdbc().getConnection(asyncResult -> {
            final SQLConnection connection = asyncResult.result();
            connection.query("SELECT * FROM Accounts", result -> {
                final List<Account> accounts = result.result()
                        .getRows()
                        .stream()
                        .map(Account::new)
                        .collect(Collectors.toList());
                context.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                        .end(Json.encodePrettily(accounts));
                connection.close();
            });
        });
    }

    public void getAccountById(@NonNull RoutingContext context) {
        final String accountId = context.pathParam("accountId");
        if (Objects.isNull(accountId)) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        } else {
            repository.selectOne(accountId, context);
        }
    }
}
