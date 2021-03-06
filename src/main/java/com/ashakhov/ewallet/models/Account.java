package com.ashakhov.ewallet.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.vertx.core.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Alexander Shakhov
 */
@Getter
@ToString
@EqualsAndHashCode
/*@JsonInclude(JsonInclude.Include.NON_NULL)*/
@JsonPropertyOrder({"accountId", "username", "balance", "currency"})
public class Account {
    @NonNull
    private final String accountId;
    @NonNull
    private final String username;
    @NonNull
    private final Double balance;
    @NonNull
    private final CurrencyCode currency;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Account(@JsonProperty("accountId") String accountId, @JsonProperty("username") String username,
            @JsonProperty("balance") Double balance, @JsonProperty("currency") CurrencyCode currency) {
        this.accountId = accountId;
        this.username = username;
        this.balance = balance;
        this.currency = currency;
    }

    public Account(@NonNull JsonObject json) {
        accountId = json.getString("ACCOUNTID");
        username = json.getString("USERNAME");
        balance = json.getDouble("BALANCE");
        currency = CurrencyCode.of(json.getString("CURRENCY"));
    }
}
