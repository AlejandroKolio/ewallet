package com.ashakhov.ewallet;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.javafaker.Faker;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
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
public class EWalletApi_IT {
    private static final int PORT = 8080;
    @NonNull
    private Vertx vertx;
    @NonNull
    private HttpServer server;
    @NonNull
    private WebClient client;
    @NonNull
    private Faker faker;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        faker = Faker.instance();
        final ServerSocket socket = new ServerSocket(0);
        socket.close();
        final DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.PORT", PORT));
        vertx.deployVerticle(WebServer.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void createAccountTest(TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
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
                        async.complete();
                        log.info(account.toString());
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void getAccountsTest(TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
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
                        async.complete();
                        log.info(account.toString());
                    });
                })
                .write(json)
                .end();

        vertx.createHttpClient()
                .get(PORT, "localhost", "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    response.bodyHandler(body -> {
                        final List<Account> accounts = Json.decodeValue(body.toString(), new TypeReference<List<Account>>() {});
                        context.assertTrue(accounts.size() == 1);
                        log.info(accounts.toString());
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void getAccountByIdTest(TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
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
                        async.complete();
                        log.info(account.toString());
                    });
                })
                .write(json)
                .end();

        vertx.createHttpClient()
                .get(PORT, "localhost", "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    response.bodyHandler(body -> {
                        final List<Account> accounts = Json.decodeValue(body.toString(), new TypeReference<List<Account>>() {});
                        context.assertTrue(accounts.size() == 1);
                        log.info(accounts.toString());
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }
}
