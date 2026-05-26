package roomescape.domain.waiting;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingResponse createWaiting(WaitingRequest waitingRequest) {
        validateDuplicateWaiting(waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId(),
                waitingRequest.name());

        ReservationTime reservationTime = reservationTimeRepository.findById(waitingRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(waitingRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        Waiting waiting = Waiting.of(
                waitingRequest.name(),
                waitingRequest.date(),
                reservationTime,
                theme
        );

        Waiting saved = waitingRepository.save(waiting);

        return WaitingResponse.of(saved);
    }

    private void validateDuplicateWaiting(LocalDate date, Long timeId, Long themeId, String name) {
        boolean isDuplicated = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME);
        }
    }
}
