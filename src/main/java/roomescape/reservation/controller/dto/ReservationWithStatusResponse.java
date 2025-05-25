package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationWithStatusResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {

    public ReservationWithStatusResponse {
        validate(id, themeName, date, time, status);
    }

    public ReservationWithStatusResponse(
            Long id,
            String themeName,
            LocalDate date,
            LocalTime time,
            Integer rank
    ) {
        this(id, themeName, date, time, rank.toString() + "번째 예약대기");
    }

    private void validate(
            final Long id,
            final String themeName,
            final LocalDate date,
            final LocalTime time,
            final String status
    ) {
        Validator.of(ReservationWithStatusResponse.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.themeName, themeName)
                .notNullField(Fields.date, date)
                .notNullField(Fields.time, time)
                .notNullField(Fields.status, status);
    }

}
