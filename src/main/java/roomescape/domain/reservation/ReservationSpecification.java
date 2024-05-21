package roomescape.domain.reservation;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecification {
    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasMemberId(Long memberId) {
        return (root, query, builder) -> {
            if (memberId == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("member").get("id"), memberId);
        };
    }

    public static Specification<Reservation> hasThemeId(Long themeId) {
        return (root, query, builder) -> {
            if (themeId == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("theme").get("id"), themeId);
        };
    }

    public static Specification<Reservation> hasStartDate(LocalDate dateFrom) {
        return (root, query, builder) -> {
            if (dateFrom == null) {
                return builder.conjunction();
            }
            return builder.greaterThanOrEqualTo(root.get("date"), dateFrom);
        };
    }

    public static Specification<Reservation> hasEndDate(LocalDate dateTo) {
        return (root, query, builder) -> {
            if (dateTo == null) {
                return builder.conjunction();
            }
            return builder.lessThanOrEqualTo(root.get("date"), dateTo);
        };
    }
}
