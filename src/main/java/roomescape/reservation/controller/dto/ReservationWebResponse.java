package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.theme.controller.dto.ThemeWebResponse;
import roomescape.time.controller.dto.ReservationTimeWebResponse;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationWebResponse(
        Long id,
        MemberInfo memberInfo,
        LocalDate date,
        ReservationTimeWebResponse time,
        ThemeWebResponse theme
) {

    public ReservationWebResponse {
        validate(id, memberInfo, date, time, theme);
    }

    private void validate(final Long id,
                          final MemberInfo memberInfo,
                          final LocalDate date,
                          final ReservationTimeWebResponse time,
                          final ThemeWebResponse theme) {
        Validator.of(ReservationWebResponse.class)
                .notNullField(Fields.id, id)
                .notNullField(Fields.memberInfo, memberInfo)
                .notNullField(Fields.date, date)
                .notNullField(Fields.time, time)
                .notNullField(Fields.theme, theme);
    }
}
