package roomescape.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

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
        return new Specification<Reservation>() {
            @Override
            public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("member").get("id"), memberId);
            }
        };
    }

    public static Specification<Reservation> equalThemeId(Long themeId) {
        return new Specification<Reservation>() {
            @Override
            public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get("theme").get("id"), themeId);
            }
        };
    }

    public static Specification<Reservation> dateFrom(LocalDate from) {
        return new Specification<Reservation>() {
            @Override
            public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), from);
            }
        };
    }

    public static Specification<Reservation> dateTo(LocalDate to) {
        return new Specification<Reservation>() {
            @Override
            public Predicate toPredicate(Root<Reservation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("date"), to);
            }
        };
    }
}
