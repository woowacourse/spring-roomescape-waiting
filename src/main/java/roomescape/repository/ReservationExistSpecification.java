package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;

public class ReservationExistSpecification {

    private Specification<Reservation> spec;

    public ReservationExistSpecification() {
        this.spec = Specification.where(null);
    }

    public ReservationExistSpecification themeId(Long themeId) {
        if (themeId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("theme").get("id"), themeId));
        }
        return this;
    }

    public ReservationExistSpecification memberId(Long memberId) {
        if (memberId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member").get("id"), memberId));
        }
        return this;
    }

    public ReservationExistSpecification timeId(Long timeId) {
        if (timeId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("time").get("id"), timeId));
        }
        return this;
    }

    public ReservationExistSpecification date(LocalDate date) {
        if (date != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("date"), date));
        }
        return this;
    }

    public Specification<Reservation> build() {
        return this.spec;
    }
}
