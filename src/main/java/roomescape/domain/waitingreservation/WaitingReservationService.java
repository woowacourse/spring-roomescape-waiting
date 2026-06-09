package roomescape.domain.waitingreservation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDateService reservationDateService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingReservationCreationResponse createWaitingReservation(WaitingReservationCreationRequest request) {
        ReservationDate date = reservationDateService.findById(request.dateId());
        ReservationTime time = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        validateNotPast(date, time);
        validateSlotIsReserved(request);
        validateAlreadyReserved(request);
        validateDuplicationOfWaitingReservation(request);

        WaitingReservation waitingReservation = request.toEntity(date, time, theme, LocalDateTime.now());
        WaitingReservation savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
    }

    public void cancelWaitingReservation(Long id) {

        WaitingReservation waitingReservation = waitingReservationRepository
            .findById(id)
            .orElseThrow(() -> new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND));

        if (reservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(
            waitingReservation.getName(),
            waitingReservation.getDate().getId(),
            waitingReservation.getTime().getId(),
            waitingReservation.getTheme().getId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.ALREADY_PROMOTED_TO_RESERVATION);
        }

        int deletedCount = waitingReservationRepository.deleteById(id);
        if (deletedCount == 0) {
            log.warn("이미 삭제된 예약 대기 삭제 요청이 들어왔습니다. reservationId={}", id);
        }
    }

    public List<WaitingReservationWithRankResponse> getWaitingReservationsWithRankByName(String name) {
        return waitingReservationRepository.findAllByNameWithRank(name)
            .stream()
            .map(WaitingReservationWithRankResponse::from)
            .toList();
    }

    private void validateNotPast(ReservationDate reservationDate, ReservationTime reservationTime) {
        if (reservationDate.isPast(reservationTime)) {
            throw new RoomescapeException(ReservationDateErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateAlreadyReserved(WaitingReservationCreationRequest request) {
        if(reservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(
            request.name(),
            request.dateId(),
            request.timeId(),
            request.themeId())) {
            throw new RoomescapeException(WaitingReservationErrorCode.ALREADY_RESERVED);
        }
    }

    private void validateDuplicationOfWaitingReservation(WaitingReservationCreationRequest request) {
        if (waitingReservationRepository.existsByNameAndDateIdAndTimeIdAndThemeId(
            request.name(),
            request.dateId(),
            request.timeId(),
            request.themeId())) {
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
