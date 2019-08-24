package com.ashakhov.ewallet.services;

import com.ashakhov.ewallet.exceptions.AccountNotFoundException;
import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.Status;
import com.ashakhov.ewallet.models.Transaction;
import com.ashakhov.ewallet.repositories.AccountRepository;
import com.ashakhov.ewallet.repositories.TransactionRepository;
import com.ashakhov.ewallet.utils.EWalletHandler;
import com.devskiller.friendly_id.FriendlyId;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.apache.http.entity.ContentType;

/**
 * @author Alexander Shakhov
 */
public class TransactionService {
    @NonNull
    private final TransactionRepository repository;
    @NonNull
    private final AccountRepository accountRepository;

    private static TransactionService instance;

    public static TransactionService getInstance() {
        if (instance == null) {
            instance = new TransactionService();
        }
        return instance;
    }

    private TransactionService() {
        repository = TransactionRepository.getInstance();
        accountRepository = AccountRepository.getInstance();
    }

    public void getTransactions(@NonNull RoutingContext context) {
        final List<Transaction> transactions = repository.getTransactions();
        context.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .end(new JsonArray(transactions).encode());
    }

    public void getTransactionById(@NonNull RoutingContext context) {
        final String transactionId = context.pathParam("transactionId");
        final Optional<Transaction> any = repository.getTransactions()
                .stream()
                .filter(transaction -> transaction.getTransactionId().equals(transactionId))
                .findAny();
        if (any.isPresent()) {
            context.response()
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .end(Json.encode(any.get()));
        } else {
            context.fail(ErrorCodes.NOT_FOUND.getCode(), new AccountNotFoundException(
                    String.format("Transactoin with transactionId '%s' not found", transactionId)));
        }
    }

    public void createTransaction(@NonNull RoutingContext context) {
        try {
            final String bodyAsString = context.getBodyAsString();
            final Transaction transaction = Json.decodeValue(bodyAsString, Transaction.class);
            final Optional<Account> from = accountRepository.findById(transaction.getFromAccountId());
            final Optional<Account> to = accountRepository.findById(transaction.getToAccountId());
            if (from.isPresent() && to.isPresent()) {
                final Account source = from.get();
                final Account target = to.get();
                if (source.getBalance() >= transaction.getAmount() && source.getCurrency()
                        .equals(transaction.getCurrency())) {
                    transfer(transaction, source, target);
                    transactionWithResponse(context, transaction, Status.SUCCESS, "Transaction successfully complete");
                } else {
                    transactionWithResponse(context, transaction, Status.FAILED, "Insufficient currency or balance");
                }
            } else {
                transactionWithResponse(context, transaction, Status.FAILED, "");
            }
        } catch (DecodeException ex) {
            context.fail(ErrorCodes.BAD_REQUEST.getCode(),
                    new ApiClientException(String.format("Provided json body is in sufficient '%s'", ex.getMessage())));
        }
    }

    private void transfer(@NonNull Transaction transaction, @NonNull Account from, @NonNull Account to) {
        final double sum;
        if(!from.getCurrency().equals(to.getCurrency())) {
            sum = Math.round(EWalletHandler.convert(transaction.getAmount(), from.getCurrency(), to.getCurrency())) + to.getBalance();
        } else {
            sum = Math.round(transaction.getAmount() + to.getBalance());
        }

        final Account accountFrom = new Account(from.getAccountId(), from.getUsername(),
                from.getBalance() - transaction.getAmount(), from.getCurrency());
        accountRepository.getAccounts().set(accountRepository.getAccounts().indexOf(from), accountFrom);

        final Account accountTo = new Account(to.getAccountId(), to.getUsername(), sum, to.getCurrency());
        accountRepository.getAccounts().set(accountRepository.getAccounts().indexOf(to), accountTo);
    }

    private void transactionWithResponse(@NonNull RoutingContext context, @NonNull Transaction transaction,
            @NonNull Status status, @NonNull String message) {
        final Transaction transactionResponse = new Transaction(FriendlyId.createFriendlyId(),
                transaction.getFromAccountId(), transaction.getToAccountId(), transaction.getAmount(),
                transaction.getCurrency(), Instant.now(), status, message);
        context.response()
                .setStatusCode("FAILED".equalsIgnoreCase(status.getName()) ? HttpResponseStatus.BAD_REQUEST.code() :
                        HttpResponseStatus.OK.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .end(Json.encode(transactionResponse));
        repository.add(transactionResponse);
    }
}
