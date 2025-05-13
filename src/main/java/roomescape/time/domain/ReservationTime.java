package roomescape.time.domain;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    private final ReservationTimeId id;
    private final LocalTime value;

    private static ReservationTime of(final ReservationTimeId id, final LocalTime value) {
        validate(id, value);
        return new ReservationTime(id, value);
    }

    public static ReservationTime withId(final ReservationTimeId id, final LocalTime value) {
        return of(id, value);
    }

    public static ReservationTime withoutId(final LocalTime value) {
        return of(ReservationTimeId.unassigned(), value);
    }

    private static void validate(final ReservationTimeId id, final LocalTime value) {
        Validator.of(ReservationTime.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.value, value);
    }

    public boolean isBefore(final LocalTime time) {
        return value.isBefore(time);
    }
}
