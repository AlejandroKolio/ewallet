package com.ashakhov.ewallet.repositories;

import com.ashakhov.ewallet.models.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
public class TransactionRepository implements BankRepository<Transaction> {
    @Getter
    @NonNull
    private final List<Transaction> transactions;
    private static TransactionRepository instance;

    public static TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    private TransactionRepository() {
        transactions = new ArrayList<>();
    }

    @Override
    public boolean add(@NonNull Transaction transaction) {
        return transactions.add(transaction);
    }

    @Override
    public boolean remove(@NonNull String transactionId) {
        return transactions.removeIf(transaction -> transaction.getTransactionId().equals(transactionId));
    }

    @Override
    public Optional<List<Transaction>> findAll() {
        return Optional.of(transactions);
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return transactions.stream()
                .filter(transaction -> transaction.getTransactionId().equals(transactionId))
                .findAny();
    }
}
