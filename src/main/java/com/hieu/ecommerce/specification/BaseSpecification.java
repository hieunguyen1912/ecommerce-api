package com.hieu.ecommerce.specification;

import jakarta.persistence.criteria.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

@Slf4j
@Getter
@RequiredArgsConstructor
public class BaseSpecification<T> implements Specification<T> {

    private final SpecSearchCriteria criteria;

    @Override
    public Specification<T> and(Specification<T> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<T> or(Specification<T> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (criteria == null) {
            return null;
        }

        Path<?> path = getPath(root, criteria.getKey());
        SearchOperation operation = criteria.getOperation();
        Object value = criteria.getValue();

        return switch (operation) {
            case EQUALITY -> criteriaBuilder.equal(path, value);
            case NEGATION -> criteriaBuilder.notEqual(path, value);
            case EQUALITY_CASE_SENSITIVE -> criteriaBuilder.equal(path, value);
            
            case GREATER_THAN -> {
                if (!(value instanceof Comparable)) {
                    throw new IllegalArgumentException("Value must be Comparable for GREATER_THAN operation");
                }
                @SuppressWarnings("unchecked")
                Expression<Comparable<Object>> comparablePath = (Expression<Comparable<Object>>) path;
                @SuppressWarnings("unchecked")
                Comparable<Object> comparableValue = (Comparable<Object>) value;
                yield criteriaBuilder.greaterThan(comparablePath, comparableValue);
            }
            case LESS_THAN -> {
                if (!(value instanceof Comparable)) {
                    throw new IllegalArgumentException("Value must be Comparable for LESS_THAN operation");
                }
                @SuppressWarnings("unchecked")
                Expression<Comparable<Object>> comparablePath = (Expression<Comparable<Object>>) path;
                @SuppressWarnings("unchecked")
                Comparable<Object> comparableValue = (Comparable<Object>) value;
                yield criteriaBuilder.lessThan(comparablePath, comparableValue);
            }
            case GREATER_THAN_OR_EQUAL -> {
                if (!(value instanceof Comparable)) {
                    throw new IllegalArgumentException("Value must be Comparable for GREATER_THAN_OR_EQUAL operation");
                }
                @SuppressWarnings("unchecked")
                Expression<Comparable<Object>> comparablePath = (Expression<Comparable<Object>>) path;
                @SuppressWarnings("unchecked")
                Comparable<Object> comparableValue = (Comparable<Object>) value;
                yield criteriaBuilder.greaterThanOrEqualTo(comparablePath, comparableValue);
            }
            case LESS_THAN_OR_EQUAL -> {
                if (!(value instanceof Comparable)) {
                    throw new IllegalArgumentException("Value must be Comparable for LESS_THAN_OR_EQUAL operation");
                }
                @SuppressWarnings("unchecked")
                Expression<Comparable<Object>> comparablePath = (Expression<Comparable<Object>>) path;
                @SuppressWarnings("unchecked")
                Comparable<Object> comparableValue = (Comparable<Object>) value;
                yield criteriaBuilder.lessThanOrEqualTo(comparablePath, comparableValue);
            }
            
            case LIKE -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), 
                    "%" + value.toString().toLowerCase() + "%");
            case LIKE_START -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), 
                    value.toString().toLowerCase() + "%");
            case LIKE_END -> criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), 
                    "%" + value.toString().toLowerCase());
            case LIKE_CASE_SENSITIVE -> criteriaBuilder.like(path.as(String.class), 
                    "%" + value.toString() + "%");
            
            case IN -> {
                if (value instanceof Collection<?> collection) {
                    yield path.in(collection);
                } else if (value instanceof Object[] array) {
                    yield path.in(array);
                } else {
                    log.warn("IN operation requires Collection or Array, but got: {}", value.getClass());
                    yield null;
                }
            }
            case NOT_IN -> {
                if (value instanceof Collection<?> collection) {
                    yield criteriaBuilder.not(path.in(collection));
                } else if (value instanceof Object[] array) {
                    yield criteriaBuilder.not(path.in(array));
                } else {
                    log.warn("NOT_IN operation requires Collection or Array, but got: {}", value.getClass());
                    yield null;
                }
            }
            
            case IS_NULL -> criteriaBuilder.isNull(path);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(path);
        };
    }

    private Path<?> getPath(Root<T> root, String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        String[] keys = key.split("\\.");
        Path<?> path = root;

        for (String k : keys) {
            path = path.get(k);
        }

        return path;
    }
}
