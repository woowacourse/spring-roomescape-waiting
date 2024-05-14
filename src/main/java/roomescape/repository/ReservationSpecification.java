package roomescape.repository;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

public class ReservationSpecification {

    public static Specification<Reservation> searchReservation(Map<String, Object> searchKey) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for(String key : searchKey.keySet()){
                predicates.add(criteriaBuilder.equal(root.get(key), searchKey.get(key)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
