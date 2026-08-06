package roomescape.domain.waitingreservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotResolver;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationRequest;
import roomescape.domain.waitingreservation.dto.WaitingReservationCreationResponse;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRankResponse;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Service
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final Clock clock;

    public WaitingReservationCreationResponse createWaitingReservation(WaitingReservationCreationRequest request) {
        ReservationSlot slot = reservationSlotResolver.resolve(request.dateId(), request.timeId(), request.themeId());
        validateReservableDate(slot);
        validateSlotIsReserved(slot);
        validateDuplicationOfWaitingReservation(request.name(), slot);

        WaitingReservation waitingReservation = request.toEntity(
                slot.date(),
                slot.time(),
                slot.theme(),
                LocalDateTime.now(clock)
        );
        WaitingReservation savedWaitingReservation = waitingReservationRepository.save(waitingReservation);
        return WaitingReservationCreationResponse.from(savedWaitingReservation);
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
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    public void cancelWaitingReservation(Long id) {
        WaitingReservation waitingReservation = getWaitingReservation(id);
        waitingReservationRepository.delete(waitingReservation);
    }

    public List<WaitingReservationWithRankResponse> getWaitingReservationsWithRankByName(String name) {
        return waitingReservationRepository.findUpcomingByNameWithRank(name, LocalDate.now(clock), LocalTime.now(clock))
            .stream()
            .map(WaitingReservationWithRankResponse::from)
            .toList();
    }

    private WaitingReservation getWaitingReservation(Long id) {
        return waitingReservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(WaitingReservationErrorCode.WAITING_RESERVATION_NOT_FOUND));
    }
}
