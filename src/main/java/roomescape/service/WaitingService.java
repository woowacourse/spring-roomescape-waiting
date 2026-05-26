package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.WaitingRequestDTO;
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

    public void addWaiting(WaitingRequestDTO request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));

        boolean exist = reservationRepository.existsByDateAndTimeAndTheme(
                request.date(),
                time,
                theme
        );

        if(!exist) {
            throw new RoomEscapeException(WaitingErrorCode.IMMEDIATE_RESERVATION_AVAILABLE);
        }
    }
}
