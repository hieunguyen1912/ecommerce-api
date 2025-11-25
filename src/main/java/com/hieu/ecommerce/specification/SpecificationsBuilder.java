package com.hieu.ecommerce.specification;

import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SpecificationsBuilder<T> {

    private final List<SpecSearchCriteria> params;

    public SpecificationsBuilder() {
        this.params = new ArrayList<>();
    }

    public SpecificationsBuilder(List<SpecSearchCriteria> params) {
        this.params = params != null ? new ArrayList<>(params) : new ArrayList<>();
    }

    public SpecificationsBuilder<T> with(String key, SearchOperation operation, Object value) {
        return with(key, operation, value, false);
    }

    public SpecificationsBuilder<T> with(String key, SearchOperation operation, Object value, boolean orPredicate) {
        params.add(new SpecSearchCriteria(key, operation, value, orPredicate));
        return this;
    }

    public Specification<T> build() {
        if (params == null || params.isEmpty()) {
            return null;
        }

        Specification<T> result = new BaseSpecification<>(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            SpecSearchCriteria criteria = params.get(i);
            BaseSpecification<T> spec = new BaseSpecification<>(criteria);
            
            result = criteria.isOrPredicate()
                    ? result.or(spec)
                    : result.and(spec);
        }

        return result;
    }
}
