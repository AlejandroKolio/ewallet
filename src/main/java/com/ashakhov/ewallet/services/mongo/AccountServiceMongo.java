package com.ashakhov.ewallet.services.mongo;

import com.ashakhov.ewallet.repositories.AbstractRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;

/**
 * @author Alexander Shakhov
 */
@Slf4j
public class AccountServiceMongo extends AbstractRepository {

    private static final String ACCOUNTS = "accounts";

    public AccountServiceMongo(@NonNull Vertx vertx) {
        super(vertx);
    }

    @Override
    public void save(@NonNull RoutingContext context) {
        final JsonObject json = context.getBodyAsJson();
        getClient().save(ACCOUNTS, json, ar -> {
            if (ar.succeeded()) {
                log.info("Saved: {}", ar.result());
                context.response()
                        .setStatusCode(HttpResponseStatus.CREATED.code())
                        .putHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                        .end(Json.encode(ar.result()));
                Promise.succeededPromise(ar.result());
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
            }
        });
    }
}
