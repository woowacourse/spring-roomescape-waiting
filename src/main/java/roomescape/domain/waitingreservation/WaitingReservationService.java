package roomescape.domain.waitingreservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;
import roomescape.support.exception.WaitingReservationErrorCode;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingReservationCreationResponse createWaitingReservation(WaitingReservationCreationRequest request) {
        ReservationDate date = getReservationDate(request.dateId());
        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        ReservationSlot slot = new ReservationSlot(date, time, theme);
        validateReservableDate(slot);
        validateSlotIsReserved(slot);
        validateDuplicationOfWaitingReservation(request.name(), slot);

        WaitingReservation waitingReservation = request.toEntity(date, time, theme, LocalDateTime.now(clock));
        WaitingReservation savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
    }

    private ReservationDate getReservationDate(Long id) {
        return reservationDateRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_EXIST));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_EXIST));
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ThemeErrorCode.THEME_NOT_EXIST));
    }

    private void validateDuplicationOfWaitingReservation(String name, ReservationSlot slot) {
        if (waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(
            name,
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        )) {
            throw new RoomescapeException(WaitingReservationErrorCode.DUPLICATE_WAITING_RESERVATION);
        }
    }

    private void validateSlotIsReserved(ReservationSlot slot) {
        boolean reserved = reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            slot.dateId(),
            slot.timeId(),
            slot.themeId()
        );
        if (!reserved) {
            throw new RoomescapeException(WaitingReservationErrorCode.AVAILABLE_SLOT_NOT_WAITABLE);
        }
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isOnOrBeforeToday(clock)) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    public void cancelWaitingReservation(Long id) {
        int deletedCount = waitingReservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND);
        }
    }

    public List<WaitingReservationWithRankResponse> getWaitingReservationsWithRankByName(String name) {
        return waitingReservationRepository.findAllByNameWithRank(name)
            .stream()
            .map(WaitingReservationWithRankResponse::from)
            .toList();
    }
}
