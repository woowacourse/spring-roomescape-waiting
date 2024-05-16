package roomescape.service.exception;

import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;
import roomescape.service.request.AdminSearchedReservationAppRequest;

import java.util.ArrayList;
import java.util.List;

public class ReservationSpecification {

    public Specification<Reservation> generate(AdminSearchedReservationAppRequest request) {
        List<Specification<Reservation>> specifications = new ArrayList<>();
        if (request.themeId() != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("theme").get("id"), request.themeId()));
        }
        if (request.memberId() != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("member").get("id"), request.memberId()));
        }
        if (request.dateFrom() != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("date").get("date"), request.dateFrom()));
        }
        if (request.dateFrom() != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("date").get("date"), request.dateTo()));
        }

        return Specification.allOf(specifications);
    }
}
