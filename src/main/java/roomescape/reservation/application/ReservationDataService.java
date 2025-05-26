package roomescape.reservation.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationOwnerException;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationslot.presentation.dto.response.MyReservationSlotResponse;

@Service
public class ReservationDataService {

    private final ReservationRepository reservationRepository;

    public ReservationDataService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<MyReservationSlotResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationRepository.findByReservationMemberId(memberInfo.id())
                .stream()
                .map(MyReservationSlotResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByReservationSlotIdAndMemberId(final Long reservationId, final Long memberId) {
        validateWaitingOwner(reservationId, memberId);
        reservationRepository.deleteByReservationSlotIdAndMemberId(reservationId, memberId);
    }

    public void validateWaitingOwner(final Long reservationId, final Long memberId) {
        boolean doesExists = reservationRepository.existsByReservationSlotIdAndMemberId(reservationId, memberId);
        if (!doesExists) {
            throw new ReservationOwnerException("자신의 예약 대기가 아닙니다.");
        }
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllWaitingReservations();
    }

    public void removeWaitingReservation(final Long waitingId) {
        reservationRepository.deleteById(waitingId);
    }

    public void deleteById(final Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
