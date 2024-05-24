package roomescape.reservation.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.domain.MemberReservation;

import java.time.LocalDate;

public class MemberReservationSpecification {

    public static Specification<MemberReservation> greaterThanOrEqualToStartDate(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("reservationSlot").get("date"), startDate);
    }

    public static Specification<MemberReservation> lessThanOrEqualToEndDate(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("reservationSlot").get("date"), endDate);
    }

    public static Specification<MemberReservation> equalMemberId(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("member").get("id"), memberId);
    }

    public static Specification<MemberReservation> equalThemeId(Long themeId) {
        if (themeId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("reservationSlot").get("theme").get("id"), themeId);
    }
}
