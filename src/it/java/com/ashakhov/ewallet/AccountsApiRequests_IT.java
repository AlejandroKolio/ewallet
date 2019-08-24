package com.ashakhov.ewallet;

import static org.assertj.core.api.Assertions.assertThat;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.javafaker.Faker;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.Random;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@ExtendWith(VertxExtension.class)
public class AccountsApiRequests_IT {
    private static final int PORT = 8080;

    public static final String HOST = "localhost";

    @BeforeEach
    public void setUp(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        vertx.deployVerticle(new WebServer(), testContext.completing());
    }

    @AfterEach
    public void tearDown(@NonNull Vertx vertx, @NonNull VertxTestContext context) {
        vertx.close(context.completing());
    }

    @Test
    public void webServerStartAndListenOnPortTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        client.get(PORT, HOST, "/accounts")
                .as(BodyCodec.string())
                .send(testContext.succeeding(response -> testContext.verify(() -> {
                    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
                    testContext.completeNow();
                })));
    }

    @Test
    @DisplayName("Create Account.")
    public void createAccountTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        final JsonObject jsonFrom = accountBuilder();
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(jsonFrom, testContext.succeeding(response1 -> testContext.verify(() -> {
                    final Account from = Json.decodeValue(response1.body(), Account.class);
                    Assertions.assertThat(from.getAccountId()).isNotBlank();
                    Assertions.assertThat(from.getUsername()).isNotBlank();
                    Assertions.assertThat(from.getBalance()).isNotNegative();
                    Assertions.assertThat(from.getCurrency()).extracting(CurrencyCode::getName).isNotNull();
                    log.info("Account: {}", from.toString());
                })));
        testContext.completeNow();
    }

    @Test
    @DisplayName("Create Account and Get all.")
    public void getAllAccountsTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        final JsonObject json = accountBuilder();
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(json, testContext.succeeding(postResponse -> testContext.verify(() -> {
                    final Account account = Json.decodeValue(postResponse.body(), Account.class);
                    Assertions.assertThat(account.getAccountId()).isNotBlank();
                    Assertions.assertThat(account.getUsername()).isNotBlank();
                    Assertions.assertThat(account.getBalance()).isNotNegative();
                    Assertions.assertThat(account.getCurrency()).extracting(CurrencyCode::getName).isNotNull();
                    log.info("Account: {}", account.toString());

                    client.get(PORT, HOST, "/accounts")
                            .as(BodyCodec.string())
                            .send(testContext.succeeding(getResponse -> testContext.verify(() -> {
                                final List<Account> accounts = Json.decodeValue(getResponse.body(),
                                        new TypeReference<List<Account>>() {});
                                Assertions.assertThat(accounts.size()).isGreaterThan(0);
                                log.info("Accounts: {}", accounts.toString());
                                testContext.completeNow();
                            })));
                })));
    }

    @Test
    @DisplayName("Create Account and Retrieve one by Id.")
    public void getAccountByIdTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        final JsonObject jsonFrom = accountBuilder();
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(jsonFrom, testContext.succeeding(response1 -> testContext.verify(() -> {
                    final Account account = Json.decodeValue(response1.body(), Account.class);
                    Assertions.assertThat(account.getAccountId()).isNotBlank();
                    Assertions.assertThat(account.getUsername()).isNotBlank();
                    Assertions.assertThat(account.getBalance()).isNotNegative();
                    Assertions.assertThat(account.getCurrency()).extracting(CurrencyCode::getName).isNotNull();
                    log.info("Account: {}", account.toString());

                    client.get(PORT, HOST, "/accounts/" + account.getAccountId())
                            .as(BodyCodec.string())
                            .send(testContext.succeeding(response2 -> testContext.verify(() -> {
                                final Account foundAccount = Json.decodeValue(response2.body(), Account.class);
                                Assertions.assertThat(foundAccount)
                                        .extracting(Account::getAccountId)
                                        .isEqualTo(account.getAccountId());
                                log.info("Get Account by accountId: {}", foundAccount.toString());
                                testContext.completeNow();
                            })));
                })));
    }

    @Test
    @DisplayName("Create and Update Account's username.")
    public void updateAccountsUsernameTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        final JsonObject jsonFrom = accountBuilder();
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(jsonFrom, testContext.succeeding(response1 -> testContext.verify(() -> {
                    final Account account = Json.decodeValue(response1.body(), Account.class);
                    Assertions.assertThat(account.getAccountId()).isNotBlank();
                    Assertions.assertThat(account.getUsername()).isNotBlank();
                    Assertions.assertThat(account.getBalance()).isNotNegative();
                    Assertions.assertThat(account.getCurrency()).extracting(CurrencyCode::getName).isNotNull();
                    log.info("Account: {}", account.toString());

                    final String name = Faker.instance().name().fullName();
                    final JsonObject username = new JsonObject().put("username", name);
                    client.patch(PORT, HOST, "/accounts/" + account.getAccountId())
                            .as(BodyCodec.string())
                            .sendJsonObject(username, testContext.succeeding(response2 -> testContext.verify(() -> {
                                Assertions.assertThat(response2.statusCode()).isEqualTo(HttpStatus.SC_NO_CONTENT);

                                client.get(PORT, HOST, "/accounts/" + account.getAccountId())
                                        .as(BodyCodec.string())
                                        .send(testContext.succeeding(response3 -> testContext.verify(() -> {
                                            final Account foundAccount = Json.decodeValue(response3.body(),
                                                    Account.class);
                                            Assertions.assertThat(foundAccount)
                                                    .extracting(Account::getUsername)
                                                    .isEqualTo(name);
                                            log.info("Get Account by accountId: {}", foundAccount.toString());
                                            testContext.completeNow();
                                        })));
                                testContext.completeNow();
                            })));
                    testContext.completeNow();
                })));
    }

    private JsonObject accountBuilder() {
        final Faker faker = Faker.instance();
        return new JsonObject().put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
    }
}
