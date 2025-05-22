package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.waiting.WaitingWithRank;

public record ReservationAndWaitingResponseDto(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public ReservationAndWaitingResponseDto(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public ReservationAndWaitingResponseDto(WaitingWithRank waitingWithRank) {
        this(
                waitingWithRank.getWaiting().getId(),
                waitingWithRank.getWaiting().getTheme().getName(),
                waitingWithRank.getWaiting().getDate(),
                waitingWithRank.getWaiting().getTime().getStartAt(),
                waitingWithRank.getRank() + "번째 예약대기"
        );
    }
}
