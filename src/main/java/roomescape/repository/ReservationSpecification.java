package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

public class ReservationSpecification {

    public static Specification<Reservation> hasThemeId(Long themeId) {
        if (themeId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("theme").get("id"), themeId);
    }

    public static Specification<Reservation> hasMemberId(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> fromDate(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Reservation> toDate(LocalDate toDate) {
        if (toDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("date"), toDate);
    }
}
