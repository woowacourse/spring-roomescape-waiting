package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record WaitingRequest(
        @NotNull(message = "[ERROR] 날짜가 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "[ERROR] 예약시간 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long timeId,
        @NotNull(message = "[ERROR] 테마 id가 없습니다.")
        @PositiveOrZero(message = "[ERROR] id는 양수여야 합니다.") Long themeId
) {

    public Waiting createWithoutId(ReservationTime reservationTime, Theme theme, Member member) {
        return new Waiting(null, member, date, reservationTime, theme);
    }
}
