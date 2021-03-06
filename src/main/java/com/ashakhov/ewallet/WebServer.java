package com.ashakhov.ewallet;

import com.ashakhov.ewallet.exceptions.handler.DefaultExceptionResolver;
import com.ashakhov.ewallet.services.AccountService;
import com.ashakhov.ewallet.services.TransactionService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
public class WebServer extends AbstractVerticle {
    private static final int PORT = 8080;

    private static final String HOST = "localhost";
    @NonNull
    private HttpServer server;
    @NonNull
    private final AccountService accountService;
    @NonNull
    private final TransactionService transactionService;

    public WebServer() {
        accountService = AccountService.getInstance();
        transactionService = TransactionService.getInstance();
    }

    @Override
    public void start(@NonNull Promise<Void> promise) {
        OpenAPI3RouterFactory.create(vertx, "openapi.yml", asyncResult -> {
            if (asyncResult.succeeded()) {
                final OpenAPI3RouterFactory routerFactory = asyncResult.result();

                // 1. Get All Accounts.
                routerFactory.addHandlerByOperationId("getAccounts", accountService::getAccounts);
                // 2. Get Account by accountId.
                routerFactory.addHandlerByOperationId("getAccountById", accountService::getAccountById);
                // 3. Create Account.
                routerFactory.addHandlerByOperationId("createAccount", accountService::createAccount);
                // 4. Update Account's username.
                routerFactory.addHandlerByOperationId("updateAccount", accountService::updateAccount);
                // 5. Create Transaction
                routerFactory.addHandlerByOperationId("createTransaction", transactionService::createTransaction);
                // 6. Get Transaction by id.
                routerFactory.addHandlerByOperationId("getTransactionById", transactionService::getTransactionById);
                // 7. Get All Transactions
                routerFactory.addHandlerByOperationId("getTransactions", transactionService::getTransactions);

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
