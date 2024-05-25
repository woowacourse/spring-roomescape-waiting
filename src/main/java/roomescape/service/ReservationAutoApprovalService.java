package roomescape.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationAutoApprovalService implements ReservationApprovalService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationAutoApprovalService(ReservationRepository reservationRepository,
                                          ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Override
    public void cancelReservation(long reservationId, long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        List<ReservationWaiting> reservationWaitings = reservationWaitingRepository.findAllByReservation(reservation);
        if (reservationWaitings.isEmpty()) {
            reservationRepository.delete(reservation);
            return;
        }
        changeReservationMember(reservation, reservationWaitings);
    }

    private void changeReservationMember(Reservation reservation, List<ReservationWaiting> reservationWaitings) {
        reservationWaitings.sort(Comparator.comparing(ReservationWaiting::getDate)
                .thenComparing(ReservationWaiting::getTime));
        ReservationWaiting firstWaiting = reservationWaitings.get(0);
        reservation.changeMember(firstWaiting.getMember());
        reservationWaitingRepository.delete(firstWaiting);
    }

    @Override
    public void approveWaiting(long reservationWaitingId) {
        throw new UnsupportedOperationException("자동 승인 서비스는 수동 승인 서비스를 지원하지 않습니다.");
    }
}
