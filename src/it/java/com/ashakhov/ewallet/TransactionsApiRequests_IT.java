package com.ashakhov.ewallet;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.github.javafaker.Faker;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
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
    public static final String HOST = "localhost";
    private static final int PORT = 8080;
    @NonNull
    private static List<Account> accounts = new CopyOnWriteArrayList<>();
    @NonNull
    private Vertx vertx;
    @NonNull
    private WebClient client;
    @NonNull
    private Faker faker;

    @Before
    public void setUp(@NonNull TestContext context) throws IOException {
        faker = Faker.instance();
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
        vertx.deployVerticle(new WebServer());
    }

    @After
    public void tearDown(@NonNull TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void createTransactionTest(@NonNull TestContext context) {
        final Async async = context.async();
        final JsonObject accountFromJson = accountBuilder();
        final Buffer buffer = Buffer.buffer();

        final HttpRequest<Buffer> post = client.post(PORT, HOST, "/accounts");

        post
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .sendJsonObject(accountFromJson, ar -> {
                    if (ar.succeeded()) {
                        final Account account = Json.decodeValue(ar.result().body(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        buffer.appendString(account.getAccountId());
                        async.complete();
                    } else {
                        log.error("Error ", ar.cause());
                        Promise.failedPromise(ar.cause());
                        async.complete();
                    }
                });

        final JsonObject accountToJson = accountBuilder();
        post
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .sendJsonObject(accountToJson, ar -> {
                    if (ar.succeeded()) {
                        final Account account = Json.decodeValue(ar.result().body(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        buffer.appendString(account.getAccountId());
                        async.complete();
                    } else {
                        log.error("Error ", ar.cause());
                        Promise.failedPromise(ar.cause());
                        async.complete();
                    }
                });

        log.info(buffer.toString());
    }

    private JsonObject accountBuilder() {
        return new JsonObject()
                    .put("username", faker.name().fullName())
                    .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                    .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
    }

    @Test
    public void getAllTransactions() {

    }

    @Test
    public void getTransactionByTransactionIdTest() {

    }

    private void createAccount(@NonNull TestContext context) {

/*        final String json = Json.encodePrettily(obj);
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
        return buffer;*/
    }
}
