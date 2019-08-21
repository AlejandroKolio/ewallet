package com.ashakhov.ewallet.exceptions.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Alexander Shakhov
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultExceptionResolver {

    @NonNull
    private final Router router;

    private static DefaultExceptionResolver instance;

    public static DefaultExceptionResolver getInstance(@NonNull Router router) {
        if (instance == null) {
            instance = new DefaultExceptionResolver(router);
        }
        return instance;
    }

    public void resolveNotFoundException() {
        router.errorHandler(HttpResponseStatus.NOT_FOUND.code(), routingContext -> {
            final ErrorResponse exception = new ErrorResponse(routingContext.statusCode(), ErrorCodes.NOT_FOUND.name(),
                    Objects.nonNull(routingContext.failure()) ? routingContext.failure().getMessage() :
                            "Resource is not found");

            routingContext.response()
                    .setStatusCode(404)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(exception));
        });
    }

    public void resolveBadRequestException() {
        router.errorHandler(HttpResponseStatus.BAD_REQUEST.code(), routingContext -> {

            final ErrorResponse exception = new ErrorResponse(routingContext.statusCode(),
                    ErrorCodes.BAD_REQUEST.name(),
                    Objects.nonNull(routingContext.failure()) ? routingContext.failure().getMessage() : "Bad Request");

            routingContext.response()
                    .setStatusCode(ErrorCodes.BAD_REQUEST.getCode())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(exception));
        });
    }

    public void resolveApiClientException() {
        router.errorHandler(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), routingContext -> {
            final ErrorResponse exception = new ErrorResponse(routingContext.statusCode(),
                    ErrorCodes.SERVER_ERROR.name(),
                    Objects.nonNull(routingContext.failure()) ? routingContext.failure().getMessage() : "Server Error");

            routingContext.response()
                    .setStatusCode(ErrorCodes.SERVER_ERROR.getCode())
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(Json.encode(exception));
        });
    }
}
