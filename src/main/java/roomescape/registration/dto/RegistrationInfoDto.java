package roomescape.registration.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.reservation.domain.ReservationStatus;
import roomescape.registration.domain.reservation.dto.ReservationResponse;
import roomescape.registration.domain.waiting.domain.Waiting;
import roomescape.registration.domain.waiting.domain.WaitingWithRank;

public record RegistrationInfoDto(long id, String themeName, LocalDate date, LocalTime time, String status) {

    public static RegistrationInfoDto from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        Reservation reservation = waiting.getReservation();
        long rank = waitingWithRank.rank();

        // todo: 상태 문자열 만드는 것을 여기서 해도 괜찮을까 - enum 관리할 때 문자열 포맷팅 어케하지
        return new RegistrationInfoDto(waiting.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getReservationTime().getStartAt(),
                rank + ReservationStatus.WAITING.getStatus()
        );
    }

    // todo: 예약은 무조건 status가 RESERVED라 여기서 부여해 주는데 dto에서 값을 부여해도 괜찮을까?
    public static RegistrationInfoDto from(ReservationResponse reservationResponse) {
        return new RegistrationInfoDto(reservationResponse.id(), reservationResponse.themeName(),
                reservationResponse.date(), reservationResponse.startAt(),
                ReservationStatus.RESERVED.getStatus()
        );
    }
}
