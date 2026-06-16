package roomescape.domain.waiting;

import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
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

        ReservationSlot slot = ReservationSlot.of(waitingRequest.date(), reservationTime, theme);

        Reservation reservation = reservationRepository.findBySlot(slot)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        Waitings waitings = Waitings.of(waitingRepository.findAllBySlot(slot));
        waitings.validateCanEnqueue(waitingRequest.name(), reservation);

        try {
            Waiting saved = waitingRepository.save(
                    Waiting.of(waitingRequest.name(), waitingRequest.date(), reservationTime, theme)
            );
            return WaitingResponse.of(saved);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_WAITING_NAME);
        }
    }

    @Transactional
    public void deleteWaiting(Long id) {
        if (!waitingRepository.existsById(id)) {
            throw new RoomescapeException(ErrorCode.WAITING_ID_NOT_FOUND);
        }
        waitingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MyWaitingsResponse getMyWaitings(String name) {
        List<Waiting> myWaitings = waitingRepository.findByName(name);
        List<MyWaitingResult> results = myWaitings.stream()
                .map(waiting -> {
                    Waitings slotWaitings = Waitings.of(waitingRepository.findAllBySlot(waiting.getSlot()));
                    return MyWaitingResult.of(waiting, slotWaitings.positionOf(name));
                })
                .toList();
        return MyWaitingsResponse.from(results);
    }
}