package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public record UserWaitingRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "테마 ID를 입력해주세요.")
        Long themeId,
        @NotNull(message = "시간 ID를 입력해주세요.")
        Long timeId
        ) {

    public Waiting toEntity(Member member, ReservationTime reservationTime, Theme theme) {
        return new Waiting(member, date, reservationTime, theme);
    }
}
