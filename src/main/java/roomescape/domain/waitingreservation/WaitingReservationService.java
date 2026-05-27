package roomescape.domain.waitingreservation;

import java.time.Clock;
import java.time.LocalDateTime;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDateService reservationDateService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final Clock clock;

    public WaitingReservationService(
        WaitingReservationRepository waitingReservationRepository,
        ReservationRepository reservationRepository,
        ReservationDateService reservationDateService,
        ReservationTimeService reservationTimeService,
        ThemeService themeService,
        Clock clock
    ) {
        this.waitingReservationRepository = waitingReservationRepository;
        this.reservationRepository = reservationRepository;
        this.reservationDateService = reservationDateService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.clock = clock;
    }

    public WaitingReservationCreationResponse createWaitingReservation(WaitingReservationCreationRequest request) {
        validateDuplicationOfWaitingReservation(request);
        ReservationDate date = reservationDateService.findById(request.dateId());
        ReservationTime time = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        validateSlotIsReserved(request);

        WaitingReservation waitingReservation = request.toEntity(date, time, theme, LocalDateTime.now(clock));
        WaitingReservation savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
    }

    private void validateDuplicationOfWaitingReservation(WaitingReservationCreationRequest request) {
        if (waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(request.name(), request.dateId(), request.timeId(), request.themeId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }

    private void validateSlotIsReserved(WaitingReservationCreationRequest request) {
        boolean reserved = reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            request.dateId(),
            request.timeId(),
            request.themeId()
        );
        if (!reserved) {
            throw new RoomescapeException(WaitingReservationErrorCode.AVAILABLE_SLOT_NOT_WAITABLE);
        }
    }
}
