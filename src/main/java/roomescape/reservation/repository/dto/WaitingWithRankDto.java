package roomescape.reservation.repository.dto;

import java.sql.Date;
import java.sql.Time;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record WaitingWithRankDto(
        Long id,
        String themeName,
        Date date,
        Time time,
        Long rank
) {

    public WaitingWithRankDto {
        validate(id, themeName, date, time, rank);
    }

    private void validate(final Long id, final String themeName, final Date date, final Time time, final Long rank) {
        Validator.of(WaitingWithRankDto.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.themeName, themeName)
                .notNullField(Fields.date, date)
                .notNullField(Fields.time, time)
                .notNullField(Fields.rank, rank);
    }
}
