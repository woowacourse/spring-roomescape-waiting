package roomescape.member.domain.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.theme.domain.dto.ThemeResponseDto;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public record ReservationWithStateDto(
        Long id,
        LocalDate date,
        String statusText,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    private static final String STATE_TEXT = "%d번 예약대기";

    public static ReservationWithStateDto of(Reservation reservation) {
        return new ReservationWithStateDto(reservation.getId(),
                reservation.getDate(),
                reservation.getStatus().getDisplayName(),
                ReservationTimeResponseDto.of(reservation.getReservationTime()),
                ThemeResponseDto.of(reservation.getTheme())
        );
    }

    public static ReservationWithStateDto of (WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        return new ReservationWithStateDto(waiting.getId(),
                waiting.getDate(),
                String.format(STATE_TEXT, waitingWithRank.rank() + 1),
                ReservationTimeResponseDto.of(waiting.getTime()),
                ThemeResponseDto.of(waiting.getTheme())
        );
    }
}
