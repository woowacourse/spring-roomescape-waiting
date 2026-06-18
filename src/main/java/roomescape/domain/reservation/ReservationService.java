package roomescape.domain.reservation;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.ReservationCreationRequest;
import roomescape.domain.reservation.dto.ReservationCreationResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.dto.ReservationUpdateRequest;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.waitingreservation.WaitingReservation;
import roomescape.domain.waitingreservation.WaitingReservationRepository;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.RoomescapeException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationDateService reservationDateService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final WaitingReservationRepository waitingReservationRepository;

    public ReservationCreationResponse createReservation(ReservationCreationRequest request) {
        ReservationDate reservationDate = reservationDateService.findById(request.dateId());
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        validateNotPast(reservationDate, reservationTime);

        Theme theme = themeService.findById(request.themeId());
        validateNotDuplicated(request.dateId(), request.timeId(), request.themeId());
        Reservation savedReservation = reservationRepository.save(
            request.toEntity(reservationDate, reservationTime, theme));
        return ReservationCreationResponse.from(savedReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
            .stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByName(String name) {
        return reservationRepository.findByName(name)
            .stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = findById(id);
        validateModifiable(reservation.getDate(), reservation.getTime());

        reservationRepository.deleteById(id);
        reservationRepository.flush();
        promoteWaitingReservation(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, @Valid ReservationUpdateRequest request) {
        Reservation reservation = findById(id);
        validateModifiable(reservation.getDate(), reservation.getTime());

        ReservationDate newReservationDate = reservationDateService.findById(request.dateId());
        ReservationTime newReservationTime = reservationTimeService.findById(request.timeId());

        validateNotPast(newReservationDate, newReservationTime);
        validateNotDuplicated(request.dateId(), request.timeId(), reservation.getTheme()
            .getId());

        ReservationDate originalDate = reservation.getDate();
        ReservationTime originalTime = reservation.getTime();
        reservation.update(newReservationDate, newReservationTime);
        reservationRepository.flush();

        promoteWaitingReservationBySlot(originalDate, originalTime, reservation.getTheme());
        return ReservationResponse.from(findById(id));
    }

    private Reservation findById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateNotDuplicated(Long dateId, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateIdAndTimeIdAndThemeId(dateId, timeId, themeId)) {
            throw new RoomescapeException(ReservationErrorCode.RESERVATION_DUPLICATED);
        }
    }

    private void validateNotPast(ReservationDate reservationDate, ReservationTime reservationTime) {
        if (reservationDate.isPast(reservationTime)) {
            throw new RoomescapeException(ReservationDateErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateNotToday(ReservationDate reservationDate) {
        if (reservationDate.isToday()) {
            throw new RoomescapeException(ReservationDateErrorCode.TODAY_NOT_MODIFIED);
        }
    }

    private void validateModifiable(ReservationDate reservationDate, ReservationTime reservationTime) {
        validateNotPast(reservationDate, reservationTime);
        validateNotToday(reservationDate);
    }

    private void promoteWaitingReservation(Reservation reservation) {
        promoteWaitingReservationBySlot(reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    private void promoteWaitingReservationBySlot(ReservationDate date, ReservationTime time, Theme theme) {
        Optional<WaitingReservation> waitingReservationOpt = waitingReservationRepository.findOldestBySlot(
            date.getId(), time.getId(), theme.getId());
        if (waitingReservationOpt.isPresent()) {
            WaitingReservation waitingReservation = waitingReservationOpt.get();
            reservationRepository.save(
                Reservation.createWithoutId(waitingReservation.getName(), waitingReservation.getDate(),
                    waitingReservation.getTime(), waitingReservation.getTheme()));
            waitingReservationRepository.deleteById(waitingReservation.getId());
        }
    }
}
