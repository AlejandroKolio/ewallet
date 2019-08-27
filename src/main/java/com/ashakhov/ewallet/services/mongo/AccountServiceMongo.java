package com.ashakhov.ewallet.services.mongo;

import com.ashakhov.ewallet.exceptions.AccountNotFoundException;
import com.ashakhov.ewallet.exceptions.DuplicateAccountException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.repositories.AbstractRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.entity.ContentType;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class AccountServiceMongo extends AbstractRepository {

    private static final String ACCOUNTS = "accounts";

    public AccountServiceMongo(@NonNull Vertx vertx) {
        super(vertx);
    }

    @Override
    public void create(@NonNull RoutingContext context) {
        final JsonObject json = context.getBodyAsJson();
        final Account accountToCreate = new Account(json);
        final JsonObject query = new JsonObject().put("username", accountToCreate.getUsername())
                .put("currency", accountToCreate.getCurrency());
        //1. Check if there is any accounts with such username or currency exists.
        getClient().find(ACCOUNTS, query, response -> {
            if (response.succeeded()) {
                final Optional<Account> account = response.result()
                        .stream()
                        .map(Account::new)
                        .filter(acc -> acc.getUsername().equals(accountToCreate.getUsername()))
                        .filter(acc -> acc.getCurrency().equals(accountToCreate.getCurrency()))
                        .findAny();
                if (account.isPresent()) {
                    log.warn("Account exsists: {}", account.get());
                    context.fail(ErrorCodes.BAD_REQUEST.getCode(), new DuplicateAccountException(
                            String.format("Account with name '%s' and currency '%s' is already exsist",
                                    accountToCreate.getUsername(), accountToCreate.getCurrency())));
                } else {
                    //2. Create account.
                    getClient().save(ACCOUNTS, json, ar -> {
                        if (ar.succeeded()) {
                            final Account createdAccount = new Account(json.put("accountId", ar.result()));
                            log.debug("Account created: {}", createdAccount);
                            context.response()
                                    .setStatusCode(HttpResponseStatus.CREATED.code())
                                    .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                                    .end(Json.encode(createdAccount));
                            Promise.succeededPromise(createdAccount);
                        } else {
                            log.error("Failed while saving: ", ar.cause());
                            Promise.failedPromise(ar.cause());
                        }
                    });
                }
            } else {
                log.error("Failed while searching: ", response.cause());
                Promise.failedPromise(response.cause());
            }
        });
    }

    @Override
    public void update(@NonNull RoutingContext context) {
        final String accountId = context.pathParam("accountId");
        final JsonObject queryId = new JsonObject().put("_id", accountId);
        final JsonObject queryToUpdate = context.getBodyAsJson();
        getClient().findOneAndUpdate(ACCOUNTS, queryId, new JsonObject().put("$set", queryToUpdate), ar -> {
            if (ar.succeeded()) {
                if(Objects.nonNull(ar.result())) {
                    final Account updatedAccount = new Account(ar.result());
                    context.response()
                            .setStatusCode(HttpResponseStatus.ACCEPTED.code())
                            .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                            .end(Json.encode(updatedAccount));
                    Promise.succeededPromise(updatedAccount);
                } else {
                    log.warn("Error while updating Account with accountId:{}", accountId);
                    context.fail(ErrorCodes.BAD_REQUEST.getCode(), new AccountNotFoundException(
                            String.format("Account with accountId:%s not found", accountId)));
                }

            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
            }
        });
    }

    @Override
    public void delete(@NonNull RoutingContext context) {
        throw new NotImplementedException("API not implemented");
    }

    @Override
    public void searchOne(@NonNull RoutingContext context) {
        final String accountId = context.pathParam("accountId");
        final JsonObject query = new JsonObject().put("_id", accountId);
        getClient().findOne(ACCOUNTS, query, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                if (Objects.isNull(ar.result())) {
                    log.warn("Account with accountId:{} not found", accountId);
                    context.fail(ErrorCodes.NOT_FOUND.getCode(), new AccountNotFoundException(
                            String.format("Account with accountId:%s not found", accountId)));
                } else {
                    final JsonObject result = ar.result();
                    final Account account = new Account(result);
                    log.info("Found: {}", result);
                    context.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                            .end(Json.encode(account));
                    Promise.succeededPromise(account);
                }
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
                context.response()
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end();
            }
        });
    }

    @Override
    public void searchAll(@NonNull RoutingContext context) {
        getClient().find(ACCOUNTS, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                final List<Account> accounts = ar.result().stream().map(Account::new).collect(Collectors.toList());
                log.info("Found: {}", accounts);
                context.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end(Json.encode(accounts));
                Promise.succeededPromise(accounts);
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
                context.response()
                        .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end();
            }
        });
    }
}
