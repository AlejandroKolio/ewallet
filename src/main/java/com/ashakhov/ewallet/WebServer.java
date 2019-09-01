package com.ashakhov.ewallet;

import com.ashakhov.ewallet.exceptions.handler.DefaultExceptionResolver;
import com.ashakhov.ewallet.services.mongo.AccountServiceMongo;
import com.ashakhov.ewallet.services.mongo.TransactionServiceMongo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
@NoArgsConstructor
public class WebServer extends AbstractVerticle {
    private static final int PORT = 8080;

    private static final String HOST = "localhost";
    @NonNull
    private HttpServer server;

    @Override
    public void start(@NonNull Promise<Void> promise) {
        OpenAPI3RouterFactory.create(vertx, "openapi.yml", asyncResult -> {
            if (asyncResult.succeeded()) {
                final OpenAPI3RouterFactory routerFactory = asyncResult.result();

                final AccountServiceMongo accountServiceMongo = new AccountServiceMongo(vertx);
                final TransactionServiceMongo transactionServiceMongo = new TransactionServiceMongo(vertx);

                // 1. Get All Accounts.
                routerFactory.addHandlerByOperationId("getAccounts", accountServiceMongo::searchAll);
                // 2. Get Account by accountId.
                routerFactory.addHandlerByOperationId("getAccountById", accountServiceMongo::searchOne);
                // 3. Create Account.
                routerFactory.addHandlerByOperationId("create", accountServiceMongo::create);
                // 4. Update Account's username.
                routerFactory.addHandlerByOperationId("updateAccount", accountServiceMongo::update);
                // 5. Delete Account by accountId
                routerFactory.addHandlerByOperationId("deleteAccountById", accountServiceMongo::delete);
                // 6. Create Transaction
                routerFactory.addHandlerByOperationId("createTransaction", transactionServiceMongo::create);
                // 7. Get Transaction by id.
                routerFactory.addHandlerByOperationId("getTransactionById", transactionServiceMongo::searchOne);
                // 8. Get All Transactions
                routerFactory.addHandlerByOperationId("getTransactions", transactionServiceMongo::searchAll);

                final Router router = routerFactory.getRouter();
                // Error handlers.
                final DefaultExceptionResolver exceptionResolver = DefaultExceptionResolver.getInstance(router);
                exceptionResolver.resolveBadRequestException();
                exceptionResolver.resolveNotFoundException();
                exceptionResolver.resolveApiClientException();

                server = vertx.createHttpServer(new HttpServerOptions().setPort(PORT).setHost(HOST))
                        .requestHandler(router)
                        .listen(config().getInteger("http.port", PORT), result -> {
                            if (result.succeeded()) {
                                promise.complete();
                            } else {
                                promise.fail(result.cause());
                            }
                        });

            }
        });
    }

    @Override
    public void stop() {
        server.close();
    }
}
