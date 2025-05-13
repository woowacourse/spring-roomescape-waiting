package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class ReservationDate {

    private final LocalDate value;

    public static ReservationDate from(final LocalDate date) {
        validate(date);
        return new ReservationDate(date);
    }

    private static void validate(final LocalDate value) {
        Validator.of(ReservationDate.class)
                .validateNotNull(Fields.value, value, DomainTerm.RESERVATION_DATE.label());
    }

    public boolean isBefore(final LocalDate date) {
        return value.isBefore(date);
    }

    public boolean isAfter(final LocalDate date) {
        return value.isAfter(date);
    }
}
