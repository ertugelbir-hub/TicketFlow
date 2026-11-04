package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils; // Spring Framework utility sınıfı
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecifications {

    public static Specification<Event> withFilters(String city, String type, String q, LocalDateTime from, LocalDateTime to) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Şehir filtresi (Case-insensitive)
            if (StringUtils.hasText(city)) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("city")), 
                    city.toLowerCase()
                ));
            }

            // Tür filtresi
            if (StringUtils.hasText(type)) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("type")), 
                    type.toLowerCase()
                ));
            }

            // Genel Arama (Title üzerinden) 
            if (StringUtils.hasText(q)) {
                // Like pattern'i Java tarafında oluşturulup parametre olarak geçilir.
                // Hibernate bu parametreyi String olarak bağlar, PostgreSQL text olarak algılar.
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    pattern
                ));
            }

            // Tarih Aralığı Filtreleri
            if (from!= null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateTime"), from));
            }
            if (to!= null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateTime"), to));
            }

            // Tüm koşulları AND ile birleştir
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}