package com.ashakhov.ewallet.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.vertx.core.json.JsonObject;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Alexander Shakhov
 */
@Getter
@ToString
@EqualsAndHashCode
/*@JsonInclude(JsonInclude.Include.NON_NULL)*/
@JsonPropertyOrder({
        "id", "transactionId", "fromAccountId", "toAccountId", "amount", "currency", "createdOn", "updatedOn", "status",
        "message"
})
public class Transaction {
    @NonNull
    private final String transactionId;
    @NonNull
    private final String fromAccountId;
    @NonNull
    private final String toAccountId;
    @NonNull
    private final Double amount;
    @NonNull
    private final CurrencyCode currency;
    @NonNull
    private final Instant createdOn;
    @NonNull
    private final Status status;
    @NonNull
    private final String message;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Transaction(@JsonProperty("transactionId") String transactionId,
            @JsonProperty("fromAccountId") String fromAccountId, @JsonProperty("toAccountId") String toAccountId,
            @JsonProperty("amount") Double amount, @JsonProperty("currency") CurrencyCode currency,
            @JsonProperty("createdOn") Instant createdOn, @JsonProperty("status") Status status, @JsonProperty("message") String message) {
        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.currency = currency;
        this.createdOn = createdOn;
        this.status = status;
        this.message = message;
    }

    public Transaction(@NonNull JsonObject json) {
        transactionId = json.getString("TRANSACTIONID");
        fromAccountId = json.getString("FROMACCOUNTID");
        toAccountId = json.getString("TOACCOUNTID");
        amount = json.getDouble("AMOUNT");
        currency = CurrencyCode.of(json.getString("CURRENCY"));
        createdOn = json.getInstant("CREATEDON");
        status = Status.of(json.getString("STATUS"));
        message = json.getString("MESSAGE");
    }
}
