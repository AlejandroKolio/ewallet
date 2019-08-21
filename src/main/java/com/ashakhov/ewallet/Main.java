package com.ashakhov.ewallet;

import io.vertx.core.Vertx;

/**
 * @author Alexander Shakhov
 */
public class Main {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer());
    }
}
