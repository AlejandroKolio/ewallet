package com.ashakhov.ewallet;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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
    @NonNull
    private Vertx vertx;
    @NonNull
    private HttpServer server;
    @NonNull
    private WebClient client;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        server = vertx.createHttpServer()
                .requestHandler(HttpServerRequest::response)
                .listen(8080, "localhost", context.asyncAssertSuccess());
        final WebClientOptions options = new WebClientOptions()
                .setUserAgent("EWallet");
        options.setKeepAlive(false);
        client = WebClient.create(vertx, options);
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHttpCall(TestContext context) {
        // Send a GET request
        client
                .get(8080, "http://localhost", "/accounts")
                .send(ar -> {
                    if (ar.succeeded()) {
                        // Obtain response
                        final HttpResponse<Buffer> response = ar.result();
                        final JsonArray accounts = response.bodyAsJsonArray();
                        log.info("Accounts: {}", accounts.toString());
                        System.out.println("Received response with status code" + response.statusCode());
                    } else {
                        System.out.println("Something went wrong " + ar.cause().getMessage());
                    }
                });

    }
}
