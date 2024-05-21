package roomescape.domain.reservation;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecification {
    private ReservationSpecification() {
    }

    public static Specification<Reservation> applyFiltersForSearch(
            Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        return Specification.where(hasMemberId(memberId))
                .and(hasThemeId(themeId))
                .and(dateAfterOrEqual(startDate))
                .and(dateBeforeOrEqual(endDate));
    }

    private static Specification<Reservation> hasMemberId(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            if (memberId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("member").get("id"), memberId);
        };
    }

    private static Specification<Reservation> hasThemeId(Long themeId) {
        return (root, query, criteriaBuilder) -> {
            if (themeId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("theme").get("id"), themeId);
        };
    }

    private static Specification<Reservation> dateAfterOrEqual(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> {
            if (dateFrom == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
        };
    }

    private static Specification<Reservation> dateBeforeOrEqual(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> {
            if (dateTo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
        };
    }
}
