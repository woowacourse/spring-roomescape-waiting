package roomescape.reservation.domain.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.bookingslot.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.SlotReservationOwnerException;

@Service
public class ReservationDomainService {

    private final ReservationRepository reservationRepository;

    public ReservationDomainService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationRepository.findByWaitingMemberId(memberInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByBookingSlotIdAndMemberId(final Long reservationId, final Long memberId) {
        validateWaitingOwner(reservationId, memberId);
        reservationRepository.deleteByBookingSlotIdAndMemberId(reservationId, memberId);
    }

    public void validateWaitingOwner(final Long reservationId, final Long memberId) {
        boolean doesExists = reservationRepository.existsByBookingSlotIdAndMemberId(reservationId, memberId);
        if (!doesExists) {
            throw new SlotReservationOwnerException("자신의 예약 대기가 아닙니다.");
        }
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllByWaitingStatus(ReservationStatus.WAITING);
    }

    public void removeWaitingReservation(final Long waitingId) {
        reservationRepository.deleteById(waitingId);
    }
}
