package roomescape.service;

import java.util.List;
import roomescape.domain.reservation.CanceledReservation;
import roomescape.domain.reservation.CanceledReservationRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;

public class ReservationManualApprovalService implements ReservationApprovalService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final CanceledReservationRepository canceledReservationRepository;

    public ReservationManualApprovalService(ReservationRepository reservationRepository,
                                            ReservationWaitingRepository reservationWaitingRepository,
                                            CanceledReservationRepository canceledReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.canceledReservationRepository = canceledReservationRepository;
    }

    @Override
    public void cancelReservation(long reservationId, long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        canceledReservationRepository.findByReservation(reservation).ifPresent(r -> {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        });
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findAllByReservation(reservation);
        if (reservationWaitings.isEmpty()) {
            reservationRepository.delete(reservation);
            return;
        }
        CanceledReservation canceledReservation = new CanceledReservation(reservation);
        canceledReservationRepository.save(canceledReservation);
    }

    @Override
    public void approveWaiting(long reservationWaitingId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(reservationWaitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 대기입니다."));
        Reservation reservation = reservationWaiting.getReservation();
        CanceledReservation canceledReservation = canceledReservationRepository.findByReservation(reservation)
                .orElseThrow(() -> new IllegalArgumentException("취소된 예약입니다."));
        reservationWaitingRepository.delete(reservationWaiting);
        canceledReservationRepository.delete(canceledReservation);
        reservation.changeMember(reservationWaiting.getMember());
    }
}
