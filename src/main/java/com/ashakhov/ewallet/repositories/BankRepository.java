package com.ashakhov.ewallet.repositories;

import java.util.List;
import java.util.Optional;

/**
 * @author Alexander Shakhov
 */
public interface BankRepository<T> {

    boolean add(T element);

    boolean remove(String id);

    Optional<List<T>> findAll();

    Optional<T> findById(String id);
}
