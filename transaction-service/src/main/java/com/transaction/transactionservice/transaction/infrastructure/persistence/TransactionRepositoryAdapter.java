package com.transaction.transactionservice.transaction.infrastructure.persistence;

import com.transaction.transactionservice.transaction.application.command.TransactionFilter;
import com.transaction.transactionservice.transaction.application.port.out.TransactionRepositoryPort;
import com.transaction.transactionservice.transaction.domain.model.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;
    private final TransactionPersistenceMapper mapper;

    public TransactionRepositoryAdapter(SpringDataTransactionRepository repository, TransactionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(UUID transactionId, UUID userId) {
        return repository.findByIdAndUserId(transactionId, userId).map(mapper::toDomain);
    }

    @Override
    public Page<Transaction> findAll(TransactionFilter filter, Pageable pageable) {
        return repository.findAll(specification(filter), pageable).map(mapper::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        repository.delete(mapper.toEntity(transaction));
    }

    private Specification<TransactionEntity> specification(TransactionFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("userId"), filter.userId()));
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.to()));
            }
            if (filter.category() != null && !filter.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.category().toLowerCase(Locale.ROOT)));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.search() != null && !filter.search().isBlank()) {
                String term = "%" + filter.search().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(root.get("category")), term)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
