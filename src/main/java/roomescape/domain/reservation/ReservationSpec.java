package roomescape.domain.reservation;

import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpec {
    private Specification<Reservation> spec;

    private ReservationSpec() {
        spec = Specification.where(null);
    }

    public static ReservationSpec where() {
        return new ReservationSpec();
    }

    public ReservationSpec equalsMemberId(Long memberId) {
        if (memberId == null) {
            return this;
        }
        spec = spec.and((root, query, cb) -> cb.equal(root.get("member").get("id"), memberId));
        return this;
    }

    public ReservationSpec equalsThemeId(Long themeId) {
        if (themeId == null) {
            return this;
        }
        spec = spec.and((root, query, cb) -> cb.equal(root.get("theme").get("id"), themeId));
        return this;
    }

    public ReservationSpec greaterThanOrEqualsStartDate(LocalDate startDate) {
        if (startDate == null) {
            return this;
        }
        spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        return this;
    }

    public ReservationSpec lessThanOrEqualsEndDate(LocalDate endDate) {
        if (endDate == null) {
            return this;
        }
        spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        return this;
    }

    public Specification<Reservation> build() {
        if (spec == null) {
            throw new IllegalStateException("예약 검색 조건이 존재하지 않습니다.");
        }
        return spec;
    }
}
