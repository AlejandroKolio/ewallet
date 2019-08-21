package com.ashakhov.ewallet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.ashakhov.ewallet.models.Account;
import com.ashakhov.ewallet.models.CurrencyCode;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(VertxExtension.class)
public class FullTest {
    private static final int PORT = 8080;
    @NonNull
    private static final String HOST = "localhost";
    @NonNull
    private static Faker faker;
    @NonNull
    private static WebClient client;

    @BeforeAll
    @DisplayName("Deploy a verticle")
    static void prepare(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        vertx.deployVerticle(new WebServer(), testContext.completing());
        client = WebClient.create(vertx);
        faker = Faker.instance();
    }

    @AfterEach
    @DisplayName("Check if not down")
    void checkIfAlive(Vertx vertx) {
        assertThat(vertx.deploymentIDs()).asList().hasSize(1);
    }

    @Test
    @DisplayName("Let's create Account #1")
    void createAccountTest(@NonNull VertxTestContext testContext) {
        createAccount(testContext);
    }

    @Test
    @DisplayName("Let's get Account #1")
    void getAccount(@NonNull Vertx vertx, @NonNull VertxTestContext testContext) {
        testContext.completeNow();
    }

    private void createAccount(@NonNull VertxTestContext testContext) {
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(accountBuilder(), ar -> {
                    if (ar.succeeded()) {
                        final String body = ar.result().body();
                        final Account account = Json.decodeValue(body, Account.class);
                        log.info(account.toString());
                        assertThat(account.getAccountId()).isNotNull();
                        assertThat(account.getUsername()).isNotNull();
                        assertThat(account.getBalance()).isNotNull();
                        assertThat(account.getCurrency()).extracting(CurrencyCode::name).isIn(CurrencyCode.EUR.name(), CurrencyCode.USD.name(), CurrencyCode.RUB.name());
                        testContext.completeNow();
                    } else {
                        log.error("FullTest Failed ", ar.cause());
                        testContext.failNow(testContext.causeOfFailure());
                    }
                });
        testContext.completeNow();
    }

    private JsonObject accountBuilder() {
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        return obj;
    }
}
