package roomescape.reservation.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;

public class ReservationSpecification {

    public static Specification<Reservation> greaterThanOrEqualToStartDate(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("reservationSlot").get("date"), startDate);
    }

    public static Specification<Reservation> lessThanOrEqualToEndDate(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("reservationSlot").get("date"), endDate);
    }

    public static Specification<Reservation> equalMemberId(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<Reservation> equalThemeId(Long themeId) {
        if (themeId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("reservationSlot").get("theme").get("id"), themeId);
    }
}
