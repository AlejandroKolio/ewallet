package com.ashakhov.ewallet.repositories;

import com.ashakhov.ewallet.utils.EWalletHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
public abstract class AbstractRepository {

    @Getter
    protected final JDBCClient jdbc;

    public AbstractRepository(@NonNull Vertx vertx) {
        // @formatter:off
        jdbc = JDBCClient.createShared(vertx,
                new JsonObject()
                        .put("url", "jdbc:hsqldb:mem:ewalletdb?shutdown=true")
                        .put("driver_class", "org.hsqldb.jdbcDriver")
                        .put("max_pool_size", 30)
                        .put("user", "sa")
                        .put("password", "sa"), "e-wallet-service");

            init((connection) -> createDB(connection, Promise.promise()), Promise.promise());
    }

    private void init(@NonNull Handler<AsyncResult<SQLConnection>> next, @NonNull Promise<Void> promise) {
        jdbc.getConnection(asyncResult -> {
            if (asyncResult.failed()) {
                promise.fail(asyncResult.cause());
            } else {
                next.handle(Future.succeededFuture(asyncResult.result()));
            }
        });
    }

    private void createDB(@NonNull AsyncResult<SQLConnection> result, Promise<Void> promise) {
        if (result.failed()) {
            promise.fail(result.cause());
        } else {
            // @formatter:off
            final SQLConnection connection = result.result();
            connection.execute(EWalletHandler.getSqlScript("create_tables.sql"),
                    asyncResult -> {
                        if (asyncResult.failed()) {
                            promise.fail(asyncResult.cause());
                            connection.close();
                        }
                    });
            // @formatter:on—
        }
    }
}
