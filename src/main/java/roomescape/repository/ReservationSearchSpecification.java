package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.Reservation;
import roomescape.domain.Status;

public class ReservationSearchSpecification {

    private Specification<Reservation> spec;

    public ReservationSearchSpecification() {
        this.spec = Specification.where(null);
    }

    public ReservationSearchSpecification sameThemeId(Long themeId) {
        if (themeId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("theme").get("id"), themeId));
        }
        return this;
    }

    public ReservationSearchSpecification sameMemberId(Long memberId) {
        if (memberId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member").get("id"), memberId));
        }
        return this;
    }

    public ReservationSearchSpecification sameTimeId(Long timeId) {
        if (timeId != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("time").get("id"), timeId));
        }
        return this;
    }

    public ReservationSearchSpecification sameDate(LocalDate date) {
        if (date != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("date"), date));
        }
        return this;
    }

    public ReservationSearchSpecification sameStatus(Status status) {
        if (status != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
        }
        return this;
    }

    public ReservationSearchSpecification dateStartFrom(LocalDate dateFrom) {
        if (dateFrom != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateFrom));
        }
        return this;
    }

    public ReservationSearchSpecification dateEndAt(LocalDate toDate) {
        if (toDate != null) {
            this.spec = this.spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("date"), toDate));
        }
        return this;
    }

    public Specification<Reservation> build() {
        return this.spec;
    }
}
