package com.ashakhov.ewallet;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.javafaker.Faker;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@RunWith(VertxUnitRunner.class)
public class AccountsApiRequests_IT {
    private static final int PORT = 8080;
    public static final String HOST = "localhost";
    @NonNull
    private Vertx vertx;
    @NonNull
    private Faker faker;

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
    @DisplayName("Create Account.")
    public void createAccountTest(@NonNull TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
        vertx.createHttpClient()
                .post(PORT, HOST, "/accounts")
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
    @DisplayName("Create Account and Retrieve all.")
    public void getAllAccountsTest(@NonNull TestContext context) {
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
                .handler(createResponse -> {
                    context.assertEquals(createResponse.statusCode(), 201);
                    context.assertTrue(createResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    createResponse.bodyHandler(bodyFromPost -> {
                        final Account account = Json.decodeValue(bodyFromPost.toString(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        async.complete();

                        vertx.createHttpClient()
                                .get(PORT, "localhost", "/accounts")
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                                .handler(getAllResponse -> {
                                    context.assertEquals(getAllResponse.statusCode(), 200);
                                    context.assertTrue(getAllResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                                    getAllResponse.bodyHandler(bodyFromGet -> {
                                        final List<Account> accounts = Json.decodeValue(bodyFromGet.toString(),
                                                new TypeReference<List<Account>>() {});
                                        context.assertTrue(accounts.size() == 1);
                                        log.info(accounts.toString());
                                        async.complete();
                                    });
                                })
                                .write(json)
                                .end();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    @DisplayName("Create Account and Retrieve one by Id.")
    public void getAccountByIdTest(@NonNull TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());
        // 1. Create Account.
        vertx.createHttpClient()
                .post(PORT, "localhost", "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(createResponse -> {
                    context.assertEquals(createResponse.statusCode(), 201);
                    context.assertTrue(createResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    createResponse.bodyHandler(bodyFromPost -> {
                        final Account account = Json.decodeValue(bodyFromPost.toString(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        async.complete();

                        // 2. Get Account.
                        vertx.createHttpClient()
                                .get(PORT, "localhost", String.format("/accounts/%s", account.getAccountId()))
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                                .handler(getAllResponse -> {
                                    context.assertEquals(getAllResponse.statusCode(), 200);
                                    context.assertTrue(getAllResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                                    getAllResponse.bodyHandler(bodyFromGet -> {
                                        final Account foundAccount = Json.decodeValue(bodyFromGet.toString(), Account.class);
                                        context.assertTrue(foundAccount.getAccountId().equals(account.getAccountId()));
                                        log.info(foundAccount.toString());
                                        async.complete();
                                    });
                                })
                                .write(json)
                                .end();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    @DisplayName("Create Account and Update username and find it by accountId.")
    public void updateAccountsUsernameTest(@NonNull TestContext context) {
        final Async async = context.async();
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        final String json = Json.encodePrettily(obj);
        final String length = Integer.toString(json.length());

        // 1. Create Account.
        vertx.createHttpClient()
                .post(PORT, "localhost", "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                .handler(createResponse -> {
                    context.assertEquals(createResponse.statusCode(), 201);
                    context.assertTrue(createResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                    createResponse.bodyHandler(bodyFromPost -> {
                        final Account account = Json.decodeValue(bodyFromPost.toString(), Account.class);
                        context.assertNotNull(account.getUsername());
                        context.assertNotNull(account.getBalance());
                        context.assertNotNull(account.getAccountId());
                        context.assertNotNull(account.getCurrency());
                        log.info(account.toString());
                        async.complete();

                        final JsonObject newName = new JsonObject();
                        obj.put("username", faker.name().fullName());
                        final String newJson = Json.encodePrettily(newName);

                        // 2. Update Username.
                        vertx.createHttpClient()
                                .put(PORT, "localhost", String.format("/accounts/%s", account.getAccountId()))
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(newJson.length()))
                                .handler(updateResponse -> {
                                    context.assertEquals(updateResponse.statusCode(), 204);
                                    context.assertTrue(updateResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));

                                    // 3. Find Account.
                                    vertx.createHttpClient()
                                            .get(PORT, "localhost", String.format("/accounts/%s", account.getAccountId()))
                                            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                            .putHeader(HttpHeaders.CONTENT_LENGTH, length)
                                            .handler(getResponse -> {
                                                context.assertEquals(getResponse.statusCode(), 200);
                                                context.assertTrue(getResponse.headers().get(HttpHeaders.CONTENT_TYPE).contains("application/json"));
                                                getResponse.bodyHandler(bodyFromGet -> {
                                                    final Account updated = Json.decodeValue(bodyFromGet.toString(), Account.class);
                                                    log.info("New Name: {}", updated.getUsername());
                                                    context.assertTrue(updated.getUsername().equals(newName.getString("username")));
                                                    log.info(updated.toString());
                                                    async.complete();
                                                });
                                            })
                                            .write(json)
                                            .end();
                                })
                                .write(json)
                                .end();
                    });
                })
                .write(json)
                .end();
    }
}
