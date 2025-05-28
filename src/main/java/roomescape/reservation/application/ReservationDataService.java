package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationOwnerException;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;

@Service
public class ReservationDataService {

    private final ReservationRepository reservationRepository;

    public ReservationDataService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation save(final Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return reservationRepository.findByReservationMemberId(memberInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByReservationSlotIdAndMemberId(final Long reservationSlotId, final Long memberId) {
        validateWaitingOwner(reservationSlotId, memberId);
        reservationRepository.deleteByReservationIdAndMemberId(reservationSlotId, memberId);
    }

    public void validateWaitingOwner(final Long reservationSlotId, final Long memberId) {
        boolean doesExists = reservationRepository.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId);
        if (!doesExists) {
            throw new ReservationOwnerException("자신의 예약 대기가 아닙니다.");
        }
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllWaitingReservations();
    }

    public List<Reservation> findByCriteria(final Long themeId, final Long memberId,
                                            final LocalDate startDate, final LocalDate endDate) {
        return reservationRepository.findByThemeIdAndDateBetweenAndReservationMemberId(themeId, startDate, endDate,
                memberId);
    }

    public Reservation getById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationOwnerException("존재하지 않는 예약입니다."));
    }

    public void removeWaitingReservation(final Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    public void deleteById(final Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public boolean existsByReservationSlotIdAndMemberId(final Long reservationSlotId, final Long memberId) {
        return reservationRepository.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId);
    }
}
