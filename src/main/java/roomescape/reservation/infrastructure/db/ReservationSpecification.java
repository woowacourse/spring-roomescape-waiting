package roomescape.reservation.infrastructure.db;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.reservation.model.entity.Reservation;

public class ReservationSpecification {

    public static Specification<Reservation> themeIdEquals(Long themeId) {
        return (root, query, criteriaBuilder) -> {
            if (themeId == null) {
                return null;
            }
            //TODO : QueryDSL로 안정성 높이기
            return criteriaBuilder.equal(root.get("theme").get("id"), themeId);
        };
    }

    public static Specification<Reservation> memberIdEquals(Long memberId) {
        return (root, query, criteriaBuilder) -> {
            if (memberId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("memberId"), memberId);
        };
    }

    public static Specification<Reservation> betweenDate(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.between(root.get("date"), from, to);
    }
}
