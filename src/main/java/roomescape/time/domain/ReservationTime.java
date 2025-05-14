package roomescape.time.domain;

import jakarta.persistence.Column;
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

    @Column(name = "reservation_time")
    private LocalTime time;

    private static ReservationTime of(final Long id, final LocalTime time) {
        validate(time);
        return new ReservationTime(id, time);
    }

    public static ReservationTime withId(final Long id, final LocalTime time) {
        return of(id, time);
    }

    public static ReservationTime withoutId(final LocalTime time) {
        return of(null, time);
    }

    private static void validate(final LocalTime time) {
        Validator.of(ReservationTime.class)
                .notNullField(Fields.time, time);
    }

    public boolean isBefore(final LocalTime time) {
        return this.time.isBefore(time);
    }
}
