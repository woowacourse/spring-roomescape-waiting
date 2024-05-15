package roomescape.service.dto.reservation;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.reservation.Reservation;

public class ReservationSearchParams {
    private final Long memberId;
    private final Long themeId;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;

    public ReservationSearchParams(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        this.memberId = memberId;
        this.themeId = themeId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public Long memberId() {
        return memberId;
    }

    public Long themeId() {
        return themeId;
    }

    public LocalDate dateFrom() {
        return dateFrom;
    }

    public LocalDate dateTo() {
        return dateTo;
    }

    public Specification<Reservation> getSearchSpecification() {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            root.fetch("member");
            root.fetch("theme");
            root.fetch("time");

            if (memberId != null) {
                predicates.add(builder.equal(root.get("member").get("id"), memberId));
            }
            if (themeId != null) {
                predicates.add(builder.equal(root.get("theme").get("id"), themeId));
            }
            if (dateFrom != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
