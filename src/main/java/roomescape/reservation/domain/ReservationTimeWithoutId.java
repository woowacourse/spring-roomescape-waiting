package roomescape.reservation.domain;

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
import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public class ReservationTimeWithoutId {

    private LocalTime startAt;

    public static ReservationTimeWithoutId from(final LocalTime startAt) {
        validate(startAt);
        return new ReservationTimeWithoutId(startAt);
    }

    private static void validate(final LocalTime startAt) {
        Validator.of(ReservationTime.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label());
    }

    public boolean isBefore(final LocalTime time) {
        return startAt.isBefore(time);
    }
}
