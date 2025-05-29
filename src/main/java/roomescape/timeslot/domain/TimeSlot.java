package roomescape.timeslot.domain;

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
@Table(name = "time_slots")
public class TimeSlot extends BaseEntity {

    @Embedded
    private ReservationTime startAt;

    private TimeSlot(final ReservationTime startAt) {
        validate(startAt);
        this.startAt = startAt;
    }

    private TimeSlot(final TimeSlotId id, final ReservationTime startAt) {
        super(id.getValue());
        validate(startAt);
        this.startAt = startAt;
    }

    public static TimeSlot withId(final TimeSlotId id, final ReservationTime startAt) {
        return new TimeSlot(id, startAt);
    }

    public static TimeSlot withoutId(final ReservationTime startAt) {
        return new TimeSlot(startAt);
    }

    private static void validate(final ReservationTime startAt) {
        Validator.of(TimeSlot.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }

    public boolean isBefore(final LocalTime time) {
        return startAt.isBefore(time);
    }

    public TimeSlotId getId() {
        return TimeSlotId.from(id);
    }
}
