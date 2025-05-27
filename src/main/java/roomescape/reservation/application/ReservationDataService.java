package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationOwnerException;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
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

    public List<Reservation> findFilteredReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate, final LocalDate endDate) {
        return reservationRepository.findByThemeIdAndDateBetweenAndReservationMemberId(themeId, startDate, endDate,
                memberId);
    }

    public void removeWaitingReservation(final Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public void deleteById(final Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public Optional<Reservation> findByReservationSlot(final ReservationSlot reservationSlot) {
        return reservationRepository.findByReservationSlot(reservationSlot);
    }

    public boolean existsByReservationSlotIdAndMemberId(final Long reservationSlotId, final Long memberId) {
        return reservationRepository.existsByReservationSlotIdAndMemberId(reservationSlotId, memberId);
    }

    public Reservation getById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationOwnerException("존재하지 않는 예약입니다."));
    }
}
