package roomescape.reservation.domain;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class ReservationDate {

    private final LocalDate value;

    public static ReservationDate from(final LocalDate date) {
        validate(date);
        return new ReservationDate(date);
    }

    private static void validate(final LocalDate value) {
        Validator.of(ReservationDate.class)
                .notNullField(Fields.value, value);
    }

    public boolean isBefore(final LocalDate date) {
        return value.isBefore(date);
    }

    public boolean isAfter(final LocalDate date) {
        return value.isAfter(date);
    }
}
