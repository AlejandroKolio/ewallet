package com.ashakhov.ewallet.repositories;

import com.ashakhov.ewallet.models.Account;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Alexander Shakhov
 */
public class AccountRepository implements BankRepository<Account> {
    @Getter
    @NonNull
    private final CopyOnWriteArrayList<Account> accounts;
    private static AccountRepository instance;

    public static AccountRepository getInstance() {
        if (instance == null) {
            instance = new AccountRepository();
        }
        return instance;
    }

    private AccountRepository() {
        accounts = new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean add(@NonNull Account account) {
        return accounts.add(account);
    }

    @Override
    public boolean remove(@NonNull String userId) {
        return accounts.removeIf(account -> account.getAccountId().equals(userId));
    }

    @Override
    public Optional<List<Account>> findAll() {
        return Optional.of(accounts);
    }

    @Override
    public Optional<Account> findById(String userId) {
        return accounts.stream().filter(account -> account.getAccountId().equals(userId)).findAny();
    }
}
