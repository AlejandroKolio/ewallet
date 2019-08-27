package com.ashakhov.ewallet.repositories;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alexander Shakhov
 */
@Slf4j
@Getter
public abstract class AbstractRepository {
    @NonNull
    private final MongoClient client;

    public AbstractRepository(@NonNull Vertx vertx) {
        final JsonObject embeddedMongoOptions = new DeploymentOptions()
                // @formatter:off
                .setWorker(true)
                .setConfig(new JsonObject()
                        .put("host", "127.0.0.1")
                        .put("port", 27018)
                        .put("db_name", "ewallet")
                        .put("version", "3.4.3")
                        .put("waitQueueMultiple", 1000)).toJson();
                        // @formatter:on
        client = MongoClient.createShared(vertx, embeddedMongoOptions);
    }

    public abstract void create(@NonNull RoutingContext context);

    public abstract void update(@NonNull RoutingContext context);

    public abstract void delete(@NonNull RoutingContext context);

    public abstract void searchOne(@NonNull RoutingContext context);

    public abstract void searchAll(@NonNull RoutingContext context);
}
