package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotResolver reservationSlotResolver;
    private final WaitingReservationRepository waitingReservationRepository;
    private final Clock clock;

    public ReservationCreationResponse createReservation(ReservationCreationRequest request) {
        ReservationSlot slot = reservationSlotResolver.resolve(request.dateId(), request.timeId(), request.themeId());
        validateReservableDate(slot);
        validateNotDuplicated(slot);
        Reservation savedReservation = reservationRepository.save(
                request.toEntity(slot.date(), slot.time(), slot.theme()));
        return ReservationCreationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        return reservationRepository.findUpcomingByName(name, LocalDate.now(clock), LocalTime.now(clock)).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.delete(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = getReservation(id);
        validateReservableDate(reservation);

        deleteReservationOrThrow(id);
        reservationRepository.flush();
        promoteOldestWaiting(ReservationSlot.from(reservation));
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, @Valid ReservationUpdateRequest request) {
        Reservation reservation = getReservation(id);
        validateReservableDate(reservation);

        ReservationSlot currentSlot = ReservationSlot.from(reservation);
        ReservationSlot newSlot = reservationSlotResolver.resolveWithTheme(
                request.dateId(),
                request.timeId(),
                reservation.getTheme()
        );

        boolean sameSlot = currentSlot.isSameSlot(newSlot);
        if (sameSlot) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }

        validateReservableDate(newSlot);
        validateNotDuplicated(newSlot);
        reservation.changeSlot(newSlot.date(), newSlot.time());
        reservationRepository.flush();
        promoteOldestWaiting(currentSlot);
        return ReservationResponse.from(reservation);
    }

    private void promoteOldestWaiting(ReservationSlot slot) {
        Optional<WaitingReservation> waitingReservationOpt = waitingReservationRepository.findOldestBySlot(
                slot.dateId(),
                slot.timeId(),
                slot.themeId()
        );
        if (waitingReservationOpt.isEmpty()) {
            return;
        }

        WaitingReservation waitingReservation = waitingReservationOpt.get();
        reservationRepository.save(Reservation.createWithoutId(
                waitingReservation.getName(),
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme()
        ));
        waitingReservationRepository.delete(waitingReservation);
    }

    private void deleteReservationOrThrow(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.delete(reservation);
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateNotDuplicated(ReservationSlot slot) {
        if (reservationRepository.existsByDateIdAndTimeIdAndThemeId(slot.dateId(), slot.timeId(), slot.themeId())) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private void validateReservableDate(Reservation reservation) {
        ReservationSlot slot = ReservationSlot.from(reservation);
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED);
        }
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isClosedForReservation(clock)) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED);
        }
    }
}
