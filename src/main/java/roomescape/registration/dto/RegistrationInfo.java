package roomescape.registration.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.registration.reservation.domain.ReservationStatus;
import roomescape.registration.reservation.dto.ReservationResponse;
import roomescape.registration.waiting.Waiting;
import roomescape.registration.waiting.WaitingWithRank;

/**
 * todo: 상태를 어디서 지정할까?
 * 현재 Reservation과 Waiting은 분리되어 있다.
 * 그리고 status는 Reservation에 없다.
 * status 계산은 예약자가 자신의 정보를 볼 때만 사용한다.(추가로 4단계 - 예약 대기 관리에서 사용)
 * 결론: 예약 페이지와 예약 대기 페이지는 따로 관리된다.
 * 그럼 예약 대기와 예약을 모두 포함하는 네이밍이 좋겠지.
 * 이 dto는  예약, 예약 대기 모두가 쓸 수 있는 dto로 사용.
 **/
public record RegistrationInfo(long id, String themeName, LocalDate date, LocalTime time, String status) {

    public static RegistrationInfo from(WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.waiting();
        long rank = waitingWithRank.rank();

        // todo: 상태 문자열 만드는 것을 여기서 해도 괜찮을까 - enum 관리할 때 문자열 포맷팅 어케하지
        return new RegistrationInfo(waiting.getId(), waiting.getTheme().getName(),
                waiting.getDate(), waiting.getReservationTime().getStartAt(),
                rank + ReservationStatus.WAITING.getStatus()
        );
    }

    // todo: 예약은 무조건 status가 RESERVED라 여기서 부여해 주는데 dto에서 값을 부여해도 괜찮을까?
    public static RegistrationInfo from(ReservationResponse reservationResponse) {
        return new RegistrationInfo(reservationResponse.id(), reservationResponse.themeName(),
                reservationResponse.date(), reservationResponse.startAt(),
                ReservationStatus.RESERVED.getStatus()
        );
    }
}
