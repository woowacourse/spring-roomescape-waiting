package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final SlotDao slotDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, SlotDao slotDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.slotDao = slotDao;
    }

    public ReservationResponse create(ReservationRequest request, LocalDateTime currentDateTime) {
        ReservationTime reservationTime = getTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        Slot slot = createSlot(request.date(), reservationTime, theme);
        Reservation reservation = request.toReservation(slot, currentDateTime);
        validateUniqueReservation(theme.getId(), reservation.getDate(), reservationTime.getId());

        Reservation savedReservation = reservationDao.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private Slot createSlot(LocalDate date, ReservationTime reservationTime, Theme theme) {
        Optional<Slot> dateAndTimeAndTheme = slotDao.findByDateAndTimeAndTheme(date, reservationTime.getId(), theme.getId());
        return dateAndTimeAndTheme.orElseGet(() -> slotDao.save(new Slot(date, reservationTime, theme)));
    }

    private void validateUniqueReservation(long themeId, LocalDate date, long timeId) {
        boolean exists = reservationDao.existsByThemeAndDateAndTime(themeId, date, timeId);
        if (exists) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeException(ThemeErrorCode.THEME_NOT_FOUND));
    }

    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationDao.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        List<Reservation> reservations = reservationDao.findAllByName(name);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse update(long reservationId, ReservationRequest request, LocalDateTime currentDateTime) {
        Reservation reservation = getReservation(reservationId);
        validateModifiable(reservation, currentDateTime);

        ReservationTime reservationTime = getTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        validateNotPastDateTime(request.date(), reservationTime, currentDateTime);
        validateUniqueReservationForUpdate(reservationId, theme, request.date(), reservationTime);

        Slot slot = createSlot(request.date(), reservationTime, theme);
        Reservation updatedReservation = new Reservation(reservationId, slot, request.name());
        reservationDao.update(updatedReservation);
        return ReservationResponse.from(updatedReservation);
    }

    private void validateNotPastDateTime(LocalDate date, ReservationTime time, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateModifiable(Reservation reservation, LocalDateTime currentDateTime) {
        if (reservation.isNotModifiableAt(currentDateTime)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_CANCEL_DEADLINE_PASSED);
        }
    }

    private void validateUniqueReservationForUpdate(long reservationId, Theme theme,
                                                    LocalDate date, ReservationTime reservationTime) {
        boolean exists = reservationDao.existsByThemeAndDateAndTimeAndIdNot(
                theme.getId(), date,
                reservationTime.getId(), reservationId);
        if (exists) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    public void delete(long reservationId, LocalDateTime currentDateTime) {
        Reservation reservation = getReservation(reservationId);
        validateModifiable(reservation, currentDateTime);
        reservationDao.delete(reservationId);
    }

    private Reservation getReservation(long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }
}
