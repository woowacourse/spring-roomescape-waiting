package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;

public class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> withTheme(Theme theme) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("theme"), theme);
    }

    public static Specification<Reservation> withMember(Member member) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member"), member);
    }

    public static Specification<Reservation> withDateFrom(LocalDate dateFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom);
    }

    public static Specification<Reservation> withDateTo(LocalDate dateTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateTo);
    }
}
