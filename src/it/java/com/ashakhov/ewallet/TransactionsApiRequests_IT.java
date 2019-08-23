package com.ashakhov.ewallet;

import static org.assertj.core.api.Assertions.assertThat;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
import com.ashakhov.ewallet.models.Status;
import com.ashakhov.ewallet.models.Transaction;
import com.github.javafaker.Faker;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Random;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@ExtendWith(VertxExtension.class)
public class TransactionsApiRequests_IT {
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

    @DisplayName("Transaction Test")
    @RepeatedTest(value = 5, name = "Create Accounts from/to and Transfer money")
    public void creteTransactionTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);

        // 1. Account from
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
                    log.info("From Account: {}", from.toString());

                    // 1. Account to
                    final JsonObject jsonTo = accountBuilder();
                    client.post(PORT, HOST, "/accounts")
                            .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                            .as(BodyCodec.string())
                            .sendJsonObject(jsonTo, testContext.succeeding(response2 -> testContext.verify(() -> {
                                final Account to = Json.decodeValue(response2.body(), Account.class);
                                Assertions.assertThat(to.getAccountId()).isNotBlank();
                                Assertions.assertThat(to.getUsername()).isNotBlank();
                                Assertions.assertThat(to.getBalance()).isNotNegative();
                                Assertions.assertThat(to.getCurrency()).extracting(CurrencyCode::getName).isNotNull();
                                log.info("To Account: {}", to.toString());

                                // 3. Transaction
                                final JsonObject jsonTransaction = transactionBuilder(from, to);
                                client.post(PORT, HOST, "/transactions")
                                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                                        .sendJsonObject(jsonTransaction,
                                                testContext.succeeding(response3 -> testContext.verify(() -> {
                                                    final Transaction transaction = Json.decodeValue(response3.body(),
                                                            Transaction.class);
                                                    Assertions.assertThat(transaction.getTransactionId()).isNotBlank();
                                                    Assertions.assertThat(transaction.getFromAccountId()).isNotBlank();
                                                    Assertions.assertThat(transaction.getToAccountId()).isNotBlank();
                                                    Assertions.assertThat(transaction.getAmount()).isNotNull();
                                                    Assertions.assertThat(transaction.getCurrency()).isNotNull();
                                                    Assertions.assertThat(transaction.getCreatedOn()).isNotNull();
                                                    Assertions.assertThat(transaction.getStatus())
                                                            .isEqualTo(Status.SUCCESS);
                                                    Assertions.assertThat(transaction.getMessage())
                                                            .isEqualTo("Transaction successfully complete");
                                                    log.info("Transaction: {}", transaction.toString());
                                                    testContext.completeNow();
                                                })));
                                testContext.completeNow();
                            })));
                })));
    }

    @Test
    public void webServerStartAndListenOnPortTest(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        final WebClient client = WebClient.create(vertx);
        client.get(PORT, HOST, "/accounts")
                .as(BodyCodec.string())
                .send(testContext.succeeding(response -> testContext.verify(() -> {
                    assertThat(response.body()).isEqualTo("[]");
                    testContext.completeNow();
                })));
    }

    @Test
    public void getAllTransactions() {

    }

    @Test
    public void getTransactionByTransactionIdTest() {

    }

    private static JsonObject accountBuilder() {
        final Faker faker = Faker.instance();
        return new JsonObject().put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
    }

    private static JsonObject transactionBuilder(@NonNull Account from, @NonNull Account to) {
        return new JsonObject().put("fromAccountId", from.getAccountId())
                .put("toAccountId", to.getAccountId())
                .put("amount", from.getBalance())
                .put("currency", from.getCurrency());
    }
}
