package roomescape.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.member.MemberInfo;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

@Service
@RequiredArgsConstructor
public class CancelService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public void cancelReservation(Long reservationId, MemberInfo memberInfo) {
        Reservation reservation = reservationRepository.getById(reservationId);
        reservation.cancel(memberInfo.id());
    }

    @Transactional
    public void forceCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.getById(reservationId);
        reservation.forceCancel();
    }
}
