package com.ashakhov.ewallet;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.ashakhov.ewallet.models.Status;
import com.ashakhov.ewallet.models.Transaction;
import com.github.javafaker.Faker;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@RunWith(VertxUnitRunner.class)
public class TransactionsApiRequests_IT {
    private static final int PORT = 8080;
    @NonNull
    private Vertx vertx;
    @NonNull
    private Faker faker;
    @NonNull
    private static List<Account> accounts = new CopyOnWriteArrayList<>();

    @Before
    public void setUp(@NonNull TestContext context) throws IOException {
        vertx = Vertx.vertx();
        faker = Faker.instance();
        final ServerSocket socket = new ServerSocket(0);
        socket.close();
        final DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.PORT", PORT));
        vertx.deployVerticle(WebServer.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(@NonNull TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void createTransactionTest(@NonNull TestContext context) {
        final Buffer buffer1 = createAccount(context);
        final Buffer buffer2 = createAccount(context);
        final Account from = Json.decodeValue(buffer1, Account.class);
        final Account to = Json.decodeValue(buffer2, Account.class);
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
                obj.put("fromAccountId", from.getAccountId())
                .put("toAccountId", to.getAccountId())
                .put("amount", from.getBalance())
                .put("currency", from.getCurrency());

        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());

        vertx.createHttpClient()
                .post(PORT, "localhost", "/transactions")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    response.bodyHandler(body -> {
                        final Transaction transaction = Json.decodeValue(body.toString(), Transaction.class);
                        context.assertNotNull(transaction.getTransactionId());
                        context.assertNotNull(transaction.getFromAccountId());
                        context.assertNotNull(transaction.getToAccountId());
                        context.assertNotNull(transaction.getAmount());
                        context.assertNotNull(transaction.getCurrency());
                        context.assertNotNull(transaction.getCreatedOn());
                        context.assertTrue(transaction.getStatus().equals(Status.SUCCESS));
                        context.assertTrue("Transaction successfully complete".equals(transaction.getMessage()));
                        log.info(transaction.toString());
                        async.complete();
                    });
                }).write(json)
                .end();
    }

    @Test
    public void getAllTransactions() {

    }

    @Test
    public void getTransactionByTransactionIdTest() {

    }

    private Buffer createAccount(@NonNull TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
        final Buffer buffer = Buffer.buffer();
        vertx.createHttpClient()
                .post(PORT, "localhost", "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    response.bodyHandler(body -> {
                        final Account account = Json.decodeValue(body.toString(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        async.complete();
                    });
                })
                .write(json)
                .end();
        return buffer;
    }
}
