package roomescape.time.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.BaseEntity;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@ToString
@Entity
public class ReservationTime extends BaseEntity {

    @Column(name = Fields.startAt)
    private LocalTime startAt;

    private ReservationTime(final LocalTime startAt) {
        validate(startAt);
        this.startAt = startAt;
    }

    private ReservationTime(final Long id, final LocalTime startAt) {
        super(id);
        validate(startAt);
        this.startAt = startAt;
    }

    public static ReservationTime withId(final ReservationTimeId id, final LocalTime startAt) {
        id.requireAssigned();
        return new ReservationTime(id.getValue(), startAt);
    }

    public static ReservationTime withoutId(final LocalTime startAt) {
        return new ReservationTime(startAt);
    }

    private static void validate(final LocalTime startAt) {
        Validator.of(ReservationTime.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }

    public boolean isBefore(final LocalTime time) {
        return startAt.isBefore(time);
    }

    public ReservationTimeId getId() {
        return ReservationTimeId.from(id);
    }
}
