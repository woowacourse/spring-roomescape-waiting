package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Reservation;

public class AdminReservationValidator implements ReservationValidator {

    @Override
    public void validateCreate(Reservation reservation, LocalDateTime now) {
        // 현재 관리자 예약 생성 검증은 X
    }

    @Override
    public void validateDelete(Reservation reservation, LocalDateTime now) {
        // 현재 관리자 예약 삭제 검증은 X
    }
}
