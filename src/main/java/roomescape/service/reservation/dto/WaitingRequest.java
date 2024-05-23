package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;

public record WaitingRequest(
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
