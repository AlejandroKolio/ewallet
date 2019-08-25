package com.ashakhov.ewallet.repositories;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

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
                .setWorker(true)
                .setConfig(new JsonObject().put("port", "mongodb://localhost:27018").put("version", "3.4.3").put("db_name", "e-wallet"))
                .toJson();
        client = MongoClient.createShared(vertx, embeddedMongoOptions);
    }

    public abstract void save(@NonNull RoutingContext context);

    protected void find(@NonNull String id, @NonNull String collection) {
        final JsonObject query = new JsonObject().put("_id", new ObjectId(id));
        client.findOne(collection, query, null, ar -> {
            if (ar.succeeded()) {
                log.info("Found: {}", ar.result());
                Promise.succeededPromise(ar.result());
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
            }
        });
    }

    protected void findAll(@NonNull JsonObject json, @NonNull String collection) {
        client.find(collection, json, ar -> {
            if (ar.succeeded()) {
                log.info("Found: {}", ar.result());
                Promise.succeededPromise(ar.result());
            } else {
                log.error("Failed: ", ar.cause());
                Promise.failedPromise(ar.cause());
            }
        });
    }
}
