package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class ReservationDate {

    @Column(name = "reservation_date", nullable = false)
    private LocalDate value;

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
