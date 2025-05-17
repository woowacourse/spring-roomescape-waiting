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

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants
@EqualsAndHashCode
@ToString
@Embeddable
public final class ReservationDate {

    private LocalDate value;

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
