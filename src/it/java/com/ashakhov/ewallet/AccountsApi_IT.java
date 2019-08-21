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
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@ExtendWith(VertxExtension.class)
public class AccountsApi_IT {
    private static final int PORT = 7000;
    @NonNull
    private static final String HOST = "localhost";
    @NonNull
    private Faker faker;
    @NonNull
    private WebClient client;
    @NonNull
    private Vertx vertx;
    @NonNull
    private VertxTestContext context;

    @BeforeEach
    void setUp() {
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
        context = new VertxTestContext();
        vertx.deployVerticle(new WebServer(), context.completing());
        faker = Faker.instance();
    }

    @RepeatedTest(3)
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void createSeveralAccounts(@NonNull VertxTestContext testContext) {
        client.post(PORT, HOST, "/accounts")
                .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
                .as(BodyCodec.string())
                .sendJsonObject(accountBuilder(), ar -> {
                    if (ar.succeeded()) {
                        final String body = ar.result().body();

                        final Account account = Json.decodeValue(body, Account.class);
                        assertThat(account.getAccountId()).isNotNull();
                        assertThat(account.getUsername()).isNotNull();
                        assertThat(account.getBalance()).isNotNull();
                        assertThat(account.getCurrency()).extracting(CurrencyCode::name)
                                .isIn(CurrencyCode.EUR.name(), CurrencyCode.USD.name(), CurrencyCode.RUB.name());
                    }
                });
    }

    private JsonObject accountBuilder() {
        final JsonObject obj = new JsonObject();
        obj.put("username", faker.name().fullName())
                .put("balance", faker.random().nextInt(100, 1000).doubleValue())
                .put("currency", CurrencyCode.of(new Random().nextInt(CurrencyCode.values().length)));
        return obj;
    }
}
