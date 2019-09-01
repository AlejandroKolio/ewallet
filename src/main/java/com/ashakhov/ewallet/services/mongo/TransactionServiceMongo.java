package com.ashakhov.ewallet.services.mongo;

import static java.util.stream.Collectors.toList;

import com.ashakhov.ewallet.exceptions.ApiClientException;
import com.ashakhov.ewallet.exceptions.TransactionNotFoundException;
import com.ashakhov.ewallet.exceptions.WrongRequestFormatException;
import com.ashakhov.ewallet.exceptions.handler.ErrorCodes;
import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.ashakhov.ewallet.models.Status;
import com.ashakhov.ewallet.models.Transaction;
import com.ashakhov.ewallet.repositories.AbstractRepository;
import com.ashakhov.ewallet.utils.EWalletHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.time.Instant;
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
public class TransactionServiceMongo extends AbstractRepository {

    public static final String TRANSACTION_SUCCESSFUL = "Transaction completed successfully";
    public static final String TRANSACTION_FAILED = "Transaction failed. Insufficient balance or currency";
    private static final String TRANSACTIONS = "transactions";
    private static final String ACCOUNTS = "accounts";

    public TransactionServiceMongo(@NonNull Vertx vertx) {
        super(vertx);
    }

    @Override
    public void create(@NonNull RoutingContext context) {
        final JsonObject json = context.getBodyAsJson();

        final JsonObject accountsQry = new JsonObject().put("_id", new JsonObject().put("$in",
                new JsonArray().add(json.getString("fromAccountId")).add(json.getString("toAccountId"))));

        getClient().find(ACCOUNTS, accountsQry, arSearch -> {
            if (arSearch.succeeded()) {
                final List<Account> accounts = arSearch.result().stream().map(Account::new).collect(toList());

                final Optional<Account> fromAccountId = accounts.stream()
                        .filter(a -> a.getAccountId()
                                .equals(json.getString("fromAccountId")) && a.getBalance() >= json.getDouble(
                                "amount") && a.getCurrency().equals(CurrencyCode.of(json.getString("currency"))))
                        .findAny();

                final Optional<Account> toAccountId = accounts.stream()
                        .filter(a -> a.getAccountId().equals(json.getString("toAccountId")))
                        .findAny();

                final Transaction transaction;
                if (fromAccountId.isPresent() && toAccountId.isPresent()) {
                    transaction = prepareTransaction(json, Status.SUCCESS, TRANSACTION_SUCCESSFUL);
                    saveTransaction(context, transaction);
                    //The conditions meet all the requirements, so we save transaction and do transfer money.
                    transfer(transaction, fromAccountId.get(), toAccountId.get());
                } else {
                    transaction = prepareTransaction(json, Status.FAILED, TRANSACTION_FAILED);
                    //The conditions do not meet the requirements, so we just save failed transacrion to db.
                    saveTransaction(context, transaction);
                }
            } else {
                log.error("Request failed: ", arSearch.cause());
                context.fail(ErrorCodes.BAD_REQUEST.getCode(), new ApiClientException(
                        String.format("Provided transaction body is insufficient. Check account details: '%s'",
                                arSearch.cause().getMessage())));
            }
        });
    }

    @Override
    public void update(@NonNull RoutingContext context) {
        throw new NotImplementedException("Transactions are now allowed to updated or delete");
    }

    @Override
    public void delete(@NonNull RoutingContext context) {
        throw new NotImplementedException("Transactions are now allowed to updated or delete");
    }

    @Override
    public void searchOne(@NonNull RoutingContext context) {
        final String transactionId = context.pathParam("transactionId");
        final JsonObject query = new JsonObject().put("_id", transactionId);
        getClient().findOne(TRANSACTIONS, query, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                if (Objects.isNull(ar.result())) {
                    log.warn("Transaction with transactionId:{} is not found", transactionId);
                    context.fail(ErrorCodes.NOT_FOUND.getCode(), new TransactionNotFoundException(
                            String.format("Transaction with transactionId:%s is not found", transactionId)));
                } else {
                    final JsonObject result = ar.result();
                    final Transaction transaction = new Transaction(result);
                    log.info("Found: {}", result);
                    context.response()
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                            .end(Json.encode(transaction));
                    Promise.succeededPromise(transaction);
                }
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
                context.fail(ErrorCodes.BAD_REQUEST.getCode(), new WrongRequestFormatException("Bad format request"));
            }
        });
    }

    @Override
    public void searchAll(@NonNull RoutingContext context) {
        getClient().find(TRANSACTIONS, new JsonObject(), ar -> {
            if (ar.succeeded()) {
                final List<Transaction> transactions = ar.result()
                        .stream()
                        .map(Transaction::new)
                        .collect(Collectors.toList());
                log.info("Found: {}", transactions);
                context.response()
                        .setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end(Json.encode(transactions));
                Promise.succeededPromise(transactions);
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

    /**
     * Save any (FAILED or SUCCESS) transaction attempt to MongoDB.
     * @param transaction prepared Transaction POJO.
     */
    private void saveTransaction(@NonNull RoutingContext context, @NonNull Transaction transaction) {
        final JsonObject transactionToSave = new JsonObject(Json.encode(transaction));
        getClient().save(TRANSACTIONS, transactionToSave, asyncResponse -> {
            if (asyncResponse.succeeded()) {
                final String transactionId = asyncResponse.result();
                final Transaction trn = prepareTransaction(transactionToSave.put("_id", transactionId),
                        transaction.getStatus(),
                        transaction.getMessage());
                transactionResponse(context, trn);
                Promise.succeededPromise(transaction);
            } else {
                log.error("Failed to save Transaction: {}", transaction);
                Promise.failedPromise(asyncResponse.cause());
            }
        });
    }

    /**
     * Transaction details response, including transactionId. Either Failed or Success.
     */
    private void transactionResponse(@NonNull RoutingContext context, @NonNull Transaction transaction) {
        context.response()
                .setStatusCode("FAILED".equalsIgnoreCase(transaction.getStatus().getName()) ?
                        HttpResponseStatus.BAD_REQUEST.code() : HttpResponseStatus.OK.code())
                .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .end(Json.encode(transaction));
    }

    /**
     * Prepares transaction before db comit.
     */
    @NonNull
    private Transaction prepareTransaction(@NonNull JsonObject json, @NonNull Status status, @NonNull String message) {
        return new Transaction(json.put("status", status).put("message", message).put("createdOn", Instant.now()));
    }

    /**
     * Transfer money between accounts.
     */
    private void transfer(@NonNull Transaction transaction, @NonNull Account from, @NonNull Account to) {
        final double sum;
        if (!from.getCurrency().equals(to.getCurrency())) {
            sum = Math.round(EWalletHandler.convert(transaction.getAmount(), from.getCurrency(), to.getCurrency())) + to
                    .getBalance();
        } else {
            sum = Math.round(transaction.getAmount() + to.getBalance());
        }
        final JsonObject sourceBalance = new JsonObject().put("$set",
                new JsonObject().put("balance", from.getBalance() - transaction.getAmount()));
        final JsonObject targetBalance = new JsonObject().put("$set", new JsonObject().put("balance", sum));
        final JsonObject fromQry = new JsonObject().put("_id", from.getAccountId());
        final JsonObject toQry = new JsonObject().put("_id", to.getAccountId());
        getClient().findOneAndUpdate(ACCOUNTS, fromQry, sourceBalance, ar1 -> {
            if (ar1.succeeded()) {
                getClient().findOneAndUpdate(ACCOUNTS, toQry, targetBalance, ar2 -> {
                    if (ar2.succeeded()) {
                        log.debug("Balance fullfilment complete: {}", ar2.result());
                        Promise.succeededPromise(ar2.result());
                    } else {
                        log.error("Balance fulfilment failed: ", ar2.cause());
                        Promise.failedPromise(ar2.cause());
                    }
                });
                log.debug("Balance withdrawal complete: {}", ar1.result());
                Promise.succeededPromise(ar1.result());
            } else {
                log.error("Balance withdrawal failed: ", ar1.cause());
                Promise.failedPromise(ar1.cause());
            }
        });
    }
}

