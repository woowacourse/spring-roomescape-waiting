package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.WaitingRequestDTO;
import roomescape.dto.WaitingResponseDTO;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.ThemeErrorCode;
import roomescape.exception.WaitingErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
            WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public WaitingResponseDTO addWaiting(WaitingRequestDTO request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId()).orElseThrow(
                () -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
        ReservationSlot slot = ReservationSlot.of(request.date(), time, theme);

        ensureReservationExistsForWaiting(request.name(), slot);
        validateUniqueWaiting(request.name(), slot);

        Long nextWaitingNumber = generateNextWaitingNumber(slot);

        Waiting newWaiting = Waiting.create(request.name(), slot, nextWaitingNumber);
        Waiting savedWaiting = waitingRepository.save(newWaiting);

        return WaitingResponseDTO.from(savedWaiting);
    }

    private void ensureReservationExistsForWaiting(String name, ReservationSlot slot) {
        Reservation existReservation = reservationRepository.findBySlotWithLock(slot).orElseThrow(
                () -> new RoomEscapeException(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE));
        existReservation.validateNotMyReservation(name);
    }

    private void validateUniqueWaiting(String name, ReservationSlot slot) {
        boolean isDuplicateWaiting = waitingRepository.existsByNameAndSlot(name,
                slot);
        if (isDuplicateWaiting) {
            throw new RoomEscapeException(WaitingErrorCode.WAITING_DUPLICATE);
        }
    }

    private Long generateNextWaitingNumber(ReservationSlot slot) {
        Long maxWaitingNumber = waitingRepository.findMaxWaitingNumberBy(slot.getDate(),
                slot.getTime(), slot.getTheme()).orElse(0L);

        return maxWaitingNumber + 1L;
    }

    @Transactional(readOnly = true)
    public List<WaitingResponseDTO> readAllWaiting() {
        return waitingRepository.findAll().stream()
                .map(waiting -> calculateWaitingNumber(waiting))
                .map(waiting -> WaitingResponseDTO.from(waiting)).toList();
    }

    private Waiting calculateWaitingNumber(Waiting waiting) {
        return Waiting.of(
                waiting.getId(),
                waiting.getName(),
                waiting.getReservationSlot(),
                waitingRepository.countWaitingOrder(waiting)
        );
    }

    @Transactional(readOnly = true)
    public List<WaitingResponseDTO> findWaitingsByName(String name) {
        return waitingRepository.findByName(name).stream()
                .map(waiting -> calculateWaitingNumber(waiting))
                .map(waiting -> WaitingResponseDTO.from(waiting)).toList();
    }

    @Transactional
    public void deleteWaiting(Long id) {
        Waiting existWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(WaitingErrorCode.WAITING_NOT_FOUND));
        existWaiting.validateNotPastTime(LocalDateTime.now());
        waitingRepository.delete(id);
    }
}
