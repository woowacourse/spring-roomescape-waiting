package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
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

        Reservation existReservation = reservationRepository.findByDateAndTimeAndTheme(
                request.date(),
                time,
                theme
        ).orElseThrow(() ->
                new RoomEscapeException(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE)
        );

        if (existReservation.isSameName(request.name())) {
            throw new RoomEscapeException(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT);
        }

        boolean isDuplicateWaiting = waitingRepository.existsByNameAndDateAndTimeAndTheme(
                request.name(),
                request.date(),
                time,
                theme
        );
        if (isDuplicateWaiting) {
            throw new RoomEscapeException(WaitingErrorCode.WAITING_DUPLICATE);
        }

        Long currentWaitingNumber = waitingRepository.findMaxWaitingNumberBy(
                request.date(),
                time,
                theme
        ).orElse(0L) + 1;

        Waiting newWaiting = Waiting.create(
                request.name(),
                request.date(),
                time,
                theme,
                currentWaitingNumber
        );
        Waiting savedWaiting = waitingRepository.save(newWaiting);

        return WaitingResponseDTO.from(savedWaiting);
    }
}
