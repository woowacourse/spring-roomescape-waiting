package roomescape.domain;

import java.time.LocalDateTime;
import roomescape.domain.dto.ReservationRequest;
import roomescape.exception.ReservationFailException;

public class ReservationCreateValidator {
    private final ReservationRequest reservationRequest;
    private final ReservationTime reservationTime;
    private final Theme theme;
    private final Member member;

    public ReservationCreateValidator(final ReservationRequest reservationRequest,
                                      final ReservationTime reservationTime, final Theme theme, final Member member) {
        validatePastDate(reservationRequest, reservationTime);
        this.reservationRequest = reservationRequest;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    private void validatePastDate(final ReservationRequest reservationRequest, final ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationRequest.date(), reservationTime.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ReservationFailException("지나간 날짜와 시간으로 예약할 수 없습니다.");
        }
    }

    public Reservation create() {
        return new Reservation(member, reservationRequest.date(), reservationTime, theme);
    }
}
