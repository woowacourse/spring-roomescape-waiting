package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.WaitingRequestDTO;
import roomescape.dto.WaitingResponseDTO;
import roomescape.exception.ReservationErrorCode;
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

    public WaitingService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository, WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public WaitingResponseDTO addWaiting(WaitingRequestDTO request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));

        ensureReservationExistsForWaiting(request, time, theme);
        validateUniqueWaiting(request, time, theme);

        Long nextWaitingNumber = generateNextWaitingNumber(request, time, theme);
        Waiting newWaiting = Waiting.create(
                request.name(),
                request.date(),
                time,
                theme,
                nextWaitingNumber
        );
        Waiting savedWaiting = waitingRepository.save(newWaiting);

        return WaitingResponseDTO.from(savedWaiting);
    }

    private Long generateNextWaitingNumber(WaitingRequestDTO request, ReservationTime time, Theme theme) {
        Long maxWaitingNumber = waitingRepository.findMaxWaitingNumberBy(
                request.date(),
                time,
                theme
        ).orElse(0L);

        return maxWaitingNumber + 1L;
    }

    private void validateUniqueWaiting(WaitingRequestDTO request, ReservationTime time, Theme theme) {
        boolean isDuplicateWaiting = waitingRepository.existsByNameAndDateAndTimeAndTheme(
                request.name(),
                request.date(),
                time,
                theme
        );
        if (isDuplicateWaiting) {
            throw new RoomEscapeException(WaitingErrorCode.WAITING_DUPLICATE);
        }
    }

    private void ensureReservationExistsForWaiting(WaitingRequestDTO request, ReservationTime time, Theme theme) {
        Reservation existReservation = reservationRepository.findByDateAndTimeAndThemeWithLock(
                request.date(),
                time,
                theme
        ).orElseThrow(() ->
                new RoomEscapeException(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE)
        );

        if (existReservation.isSameName(request.name())) {
            throw new RoomEscapeException(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT);
        }
    }

    @Transactional
    public void deleteWaiting(Long id) {
        Waiting existWaiting = waitingRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(WaitingErrorCode.WAITING_NOT_FOUND)
        );
        existWaiting.validateNotPastTime(LocalDateTime.now());
        waitingRepository.delete(id);
    }
}
