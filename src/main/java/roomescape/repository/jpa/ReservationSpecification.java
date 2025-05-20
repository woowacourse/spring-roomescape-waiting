package roomescape.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

public class ReservationSpecification {

    public static Specification<Reservation> hasMemberId(final Long memberId) {
        return (Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (memberId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("memberId"), memberId);
        };
    }

    public static Specification<Reservation> hasThemeId(final Long themeId) {
        return (Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (themeId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("themeId"), themeId);
        };
    }

    public static Specification<Reservation> betweenDateFromAndDateTo(final LocalDate from, final LocalDate to) {
        return (Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }

            if (from == null) {
                return cb.lessThanOrEqualTo(root.get("date"), to);
            }

            if (to == null) {
                return cb.greaterThanOrEqualTo(root.get("date"), from);
            }

            return cb.and(
                    cb.greaterThanOrEqualTo(root.get("date"), from),
                    cb.lessThanOrEqualTo(root.get("date"), to)
            );
        };
    }
}
