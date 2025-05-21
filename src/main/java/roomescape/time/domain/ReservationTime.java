package roomescape.time.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "reservation_times")
public class ReservationTime extends BaseEntity {

    @Embedded
    private TimeValue startAt;

    private ReservationTime(final TimeValue startAt) {
        validate(startAt);
        this.startAt = startAt;
    }

    private ReservationTime(final ReservationTimeId id, final TimeValue startAt) {
        super(id.getValue());
        validate(startAt);
        this.startAt = startAt;
    }

    public static ReservationTime withId(final ReservationTimeId id, final TimeValue startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime withoutId(final TimeValue startAt) {
        return new ReservationTime(startAt);
    }

    private static void validate(final TimeValue startAt) {
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
