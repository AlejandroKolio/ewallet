package com.ashakhov.ewallet.services;

import com.ashakhov.ewallet.exceptions.AccountNotFoundException;
import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.DuplicateAccountException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.repositories.AccountRepository;
import com.devskiller.friendly_id.FriendlyId;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.apache.http.entity.ContentType;

/**
 * @author Alexander Shakhov
 */
public class AccountService {
    @NonNull
    private final AccountRepository repository;

    private static AccountService instance;

    public static AccountService getInstance() {
        if (instance == null) {
            instance = new AccountService();
        }
        return instance;
    }

    private AccountService() {
        repository = AccountRepository.getInstance();
    }

    public void getAccounts(@NonNull RoutingContext context) {
        final List<Account> accounts = repository.getAccounts();
        context.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .end(new JsonArray(accounts).encode());
    }

    public void getAccountById(@NonNull RoutingContext context) {
        final String accountId = context.pathParam("accountId");
        final Optional<Account> any = repository.getAccounts()
                .stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findAny();
        if (any.isPresent()) {
            context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .end(Json.encode(any.get()));
        } else {
            context.fail(ErrorCodes.NOT_FOUND.getCode(),
                    new AccountNotFoundException(String.format("Account with accountId '%s' not found", accountId)));
        }
    }

    public void updateAccount(@NonNull RoutingContext context) {
        final String accountId = context.pathParam("accountId");
        final String name = context.getBodyAsJson().getString("username");

        final Optional<Account> any = repository.getAccounts()
                .stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findAny();
        if (any.isPresent()) {
            final Account account = any.get();
            final int index = repository.getAccounts().indexOf(account);
            final Account updatedAcc = new Account(account.getAccountId(), name, account.getBalance(),
                    account.getCurrency());
            repository.getAccounts().set(index, updatedAcc);
            context.response()
                    .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .end(Json.encode(account));
        } else {
            context.fail(ErrorCodes.NOT_FOUND.getCode(),
                    new AccountNotFoundException(String.format("Account with accountId '%s' not found", accountId)));
        }
    }

    public void createAccount(@NonNull RoutingContext context) {
        try {
            final String bodyAsString = context.getBodyAsString();
            final Account account = Json.decodeValue(bodyAsString, Account.class);
            final Optional<Account> any = repository.getAccounts()
                    .stream()
                    .filter(a -> a.getUsername().equals(account.getUsername())
                            && a.getCurrency().equals(account.getCurrency()))
                    .findAny();

            if (any.isPresent()) {
                context.fail(ErrorCodes.BAD_REQUEST.getCode(), new DuplicateAccountException(
                        String.format("Account with name '%s' and currency '%s' is already exsist",
                                account.getUsername(), account.getCurrency())));
            } else {
                final Account acc = new Account(FriendlyId.createFriendlyId(), account.getUsername(),
                        account.getBalance(), account.getCurrency());
                context.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end(Json.encode(acc));
                repository.add(acc);
            }
        } catch (DecodeException ex) {
            context.fail(ErrorCodes.BAD_REQUEST.getCode(),
                    new ApiClientException(String.format("Provided json body is in sufficient '%s'", ex.getMessage())));
        }
    }
}
