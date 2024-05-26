package roomescape.domain.reservation;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.exception.reservation.DuplicatedReservationException;

@RequiredArgsConstructor
public class ReservationFactory {
    private final ReservationRepository reservationRepository;

    public Reservation createReservation(ReservationDetail detail, Member member) {
        rejectPastReservation(detail);
        rejectDuplicateReservation(detail, member);
        return getReservation(detail, member);
    }

    private void rejectPastReservation(ReservationDetail detail) {
        LocalDateTime now = LocalDateTime.now();
        if (detail.isBefore(now)) {
            throw new IllegalArgumentException(String.format("이미 지난 시간입니다. 입력한 예약 시간: %s", detail.getDateTime()));
        }
    }

    private void rejectDuplicateReservation(ReservationDetail detail, Member member) {
        if (reservationRepository.existsByDetailAndMemberAndStatusNot(detail, member, Status.CANCELED)) {
            throw new DuplicatedReservationException();
        }
    }

    private Reservation getReservation(ReservationDetail detail, Member member) {
        boolean isReservationExists = reservationRepository.existsByDetailAndStatus(detail, Status.RESERVED);
        return new Reservation(member, detail, Status.from(isReservationExists));
    }
}
