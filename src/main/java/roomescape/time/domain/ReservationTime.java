package roomescape.time.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    private static ReservationTime of(final Long id, final LocalTime startAt) {
        validate(startAt);
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime withId(final Long id, final LocalTime startAt) {
        return of(id, startAt);
    }

    public static ReservationTime withoutId(final LocalTime startAt) {
        return of(null, startAt);
    }

    private static void validate(final LocalTime startAt) {
        Validator.of(ReservationTime.class)
                .notNullField(Fields.startAt, startAt);
    }

    public boolean isBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }
}
