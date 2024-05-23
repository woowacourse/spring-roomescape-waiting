package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.domain.repository.ReservationRepository;
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
        if (detail.isBeforeNow()) {
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
