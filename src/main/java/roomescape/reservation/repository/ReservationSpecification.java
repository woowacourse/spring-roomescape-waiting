package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.Reservation;

public class ReservationSpecification {

    public static Specification<Reservation> getReservationSpecification(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        Specification<Reservation> specification = Specification.where(null);
        if (themeId != null) {
            specification = specification.and(ReservationSpecification.equalThemeId(themeId));
        }
        if (memberId != null) {
            specification = specification.and(ReservationSpecification.equalMemberId(memberId));
        }
        if (from != null) {
            specification = specification.and(ReservationSpecification.dateFrom(from));
        }
        if (to != null) {
            specification = specification.and(ReservationSpecification.dateTo(to));
        }
        return specification;
    }

    public static Specification<Reservation> equalMemberId(Long memberId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> equalThemeId(Long themeId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("theme").get("id"), themeId);
    }

    public static Specification<Reservation> dateFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Reservation> dateTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("date"), to);
    }
}
