package roomescape.domain;

import roomescape.domain.dto.ReservationRequest;
import roomescape.exception.ReservationFailException;

import java.time.LocalDateTime;

public class ReservationCreateValidator {
    private final ReservationRequest reservationRequest;
    private final TimeSlot timeSlot;
    private final Theme theme;
    private final Member member;

    public ReservationCreateValidator(final ReservationRequest reservationRequest, final TimeSlot timeSlot, final Theme theme, final Member member) {
        validatePastDate(reservationRequest, timeSlot);
        this.reservationRequest = reservationRequest;
        this.timeSlot = timeSlot;
        this.theme = theme;
        this.member = member;
    }

    private void validatePastDate(final ReservationRequest reservationRequest, final TimeSlot timeSlot) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationRequest.date(), timeSlot.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ReservationFailException("지나간 날짜와 시간으로 예약할 수 없습니다.");
        }
    }

    public Reservation create() {
        return new Reservation(member, reservationRequest.date(), timeSlot, theme);
    }
}
