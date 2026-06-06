package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.dto.MyWaitingResult;
import roomescape.domain.waiting.dto.MyWaitingsResponse;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository,
        ReservationRepository reservationRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse createWaiting(WaitingRequest waitingRequest) {
        ReservationTime reservationTime = reservationTimeRepository.findById(waitingRequest.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(waitingRequest.themeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        validateDuplicateWaiting(waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId(),
            waitingRequest.name());
        reservationTime.validateIfTimePast(waitingRequest.date());
        validateWaitingIsAvailable(waitingRequest);

        Waiting waiting = Waiting.of(
            waitingRequest.name(),
            waitingRequest.date(),
            reservationTime,
            theme
        );

        try {
            Waiting saved = waitingRepository.save(waiting);
            return WaitingResponse.of(saved);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME);
        }
    }

    @Transactional
    public void deleteWaiting(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id).orElse(null);
        if (waiting == null) {
            return;
        }
        waiting.validateOwner(name);
        waitingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MyWaitingsResponse getMyWaitings(String name) {
        List<MyWaitingResult> myWaitingResults = waitingRepository.findByName(name);
        return MyWaitingsResponse.from(myWaitingResults);
    }


    private void validateDuplicateWaiting(LocalDate date, Long timeId, Long themeId, String name) {
        boolean isDuplicated = waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME);
        }
    }

    private void validateWaitingIsAvailable(WaitingRequest waitingRequest) {
        String reservationName = reservationRepository.findNameByDateAndTimeIdAndThemeIdForUpdate(
                waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservationName.equals(waitingRequest.name())) {
            throw new RoomescapeException(ErrorCode.WAITING_NOT_AVAILABLE);
        }
    }

}
