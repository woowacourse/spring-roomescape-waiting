package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.RoomescapeErrorCode;
import roomescape.support.exception.ThemeErrorCode;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingReservationRepository waitingReservationRepository;
    private final Clock clock;

    public ReservationCreationResponse createReservation(ReservationCreationRequest request) {
        ReservationDate reservationDate = getReservationDate(request.dateId());
        ReservationTime reservationTime = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        ReservationSlot slot = new ReservationSlot(reservationDate, reservationTime, theme);
        validateReservableDate(slot);
        validateNotDuplicated(slot);
        Reservation savedReservation = reservationRepository.save(
                request.toEntity(reservationDate, reservationTime, theme));
        return ReservationCreationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        int deletedCount = reservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = findById(id);
        validateReservableDate(reservation);

        deleteReservationOrThrow(id);
        promoteOldestWaiting(ReservationSlot.from(reservation));
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, @Valid ReservationUpdateRequest request) {
        Reservation reservation = findById(id);
        validateReservableDate(reservation);

        ReservationDate newReservationDate = getReservationDate(request.dateId());
        ReservationTime newReservationTime = getReservationTime(request.timeId());
        ReservationSlot currentSlot = ReservationSlot.from(reservation);
        ReservationSlot newSlot = new ReservationSlot(newReservationDate, newReservationTime, reservation.getTheme());

        boolean sameSlot = currentSlot.isSameSlot(newSlot);
        if (sameSlot) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }

        validateReservableDate(newSlot);
        validateNotDuplicated(newSlot);
        updateReservationOrThrow(id, request);
        promoteOldestWaiting(currentSlot);
        return ReservationResponse.from(findById(id));
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
        deleteWaitingReservationOrThrow(waitingReservation.getId());
    }

    private void deleteReservationOrThrow(Long id) {
        int deletedCount = reservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private void deleteWaitingReservationOrThrow(Long id) {
        int deletedCount = waitingReservationRepository.deleteById(id);
        if (deletedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private void updateReservationOrThrow(Long id, ReservationUpdateRequest request) {
        int updatedCount = reservationRepository.updateReservation(id, request.dateId(), request.timeId());
        if (updatedCount == 0) {
            throw new RoomescapeException(RoomescapeErrorCode.DATA_CONSISTENCY_VIOLATION);
        }
    }

    private Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
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

    private void validateNotDuplicated(ReservationSlot slot) {
        if (reservationRepository.existsByDateIdAndTimeIdAndThemeId(slot.dateId(), slot.timeId(), slot.themeId())) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private void validateReservableDate(Reservation reservation) {
        validateReservableDate(ReservationSlot.from(reservation));
    }

    private void validateReservableDate(ReservationSlot slot) {
        if (slot.isOnOrBeforeToday(clock)) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_ALLOWED);
        }
    }
}
