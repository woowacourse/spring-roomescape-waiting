package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;

import java.util.List;

@Transactional
@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;

    public WaitingService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse createReservationWaiting(final Reservation waiting) {
        validateAlreadyReserved(waiting);
        validateDuplicatedWaiting(waiting);
        return ReservationResponse.from(reservationRepository.save(waiting));
    }

    private void validateAlreadyReserved(final Reservation waiting) {
        final boolean isExisting = reservationRepository.existsByThemeAndDateAndTimeAndStatusAndMember(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.RESERVED, waiting.getMember());
        if (isExisting) {
            throw new IllegalStateException("이미 예약한 건에는 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(final Reservation waiting) {
        final boolean isExisting = reservationRepository.existsByThemeAndDateAndTimeAndStatusAndMember(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.WAITING, waiting.getMember());
        if (isExisting) {
            throw new IllegalStateException("중복해서 예약 대기를 할 수 없습니다.");
        }
    }

    public List<ReservationResponse> findReservationWaitings() {
        final List<Reservation> waitings = reservationRepository.findByStatus(ReservationStatus.WAITING);
        return waitings.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void approveReservationWaiting(final Long waitingId) {
        final Reservation waiting = reservationRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약 대기가 없습니다."));
        validateIsApprovable(waiting);
        waiting.toReserved();
    }

    private void validateIsApprovable(final Reservation waiting) {
        final boolean isExisting = reservationRepository.existsByThemeAndDateAndTimeAndStatus(
                waiting.getTheme(), waiting.getDate(), waiting.getTime(), ReservationStatus.RESERVED);
        if (isExisting) {
            throw new IllegalStateException("이미 예약이 존재하여 승인이 불가능합니다.");
        }
    }

    public void rejectReservationWaiting(final Long id) {
        final boolean isExist = reservationRepository.existsById(id);
        if (!isExist) {
            throw new IllegalArgumentException("해당 ID의 예약 대기가 없습니다.");
        }
        reservationRepository.deleteById(id);
    }
}
