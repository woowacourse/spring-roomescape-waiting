package roomescape.timeslot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class ReservationTime {

    @Column(name = "start_at")
    private LocalTime value;

    public static ReservationTime from(final LocalTime startAt) {
        validate(startAt);
        return new ReservationTime(startAt);
    }

    private static void validate(final LocalTime startAt) {
        Validator.of(ReservationTime.class)
                .validateNotNull(Fields.value, startAt, DomainTerm.RESERVATION_TIME.label());
    }

    public boolean isBefore(final LocalTime time) {
        return value.isBefore(time);
    }
}
