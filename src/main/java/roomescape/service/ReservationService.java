package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.SlotDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final SlotDao slotDao;
    private final WaitingDao waitingDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, SlotDao slotDao, WaitingDao waitingDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.slotDao = slotDao;
        this.waitingDao = waitingDao;
    }

    @Transactional
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
        return slotDao.findOrCreate(new Slot(date, reservationTime, theme));
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

    @Transactional
    public ReservationResponse update(long reservationId, UpdateReservationRequest request, LocalDateTime currentDateTime) {
        Reservation reservation = getReservation(reservationId);
        Slot previousSlot = reservation.getSlot();
        validateModifiable(reservation, currentDateTime);

        ReservationTime reservationTime = getTime(request.timeId());
        validateNotPastDateTime(request.date(), reservationTime, currentDateTime);
        validateUniqueReservationForUpdate(reservation, request.date(), reservationTime);

        Slot newSlot = createSlot(request.date(), reservationTime, reservation.getTheme());
        validateSlotChanged(previousSlot, newSlot);
        Reservation updatedReservation = reservation.updateReservation(newSlot);
        reservationDao.update(updatedReservation);

        promoteFirstWaiting(previousSlot);
        return ReservationResponse.from(updatedReservation);
    }

    private void validateModifiable(Reservation reservation, LocalDateTime currentDateTime) {
        if (reservation.isNotModifiableAt(currentDateTime)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_CANCEL_DEADLINE_PASSED);
        }
    }

    private void validateNotPastDateTime(LocalDate date, ReservationTime time, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateUniqueReservationForUpdate(Reservation reservation,
                                                    LocalDate date, ReservationTime reservationTime) {
        boolean exists = reservationDao.existsByThemeAndDateAndTimeAndIdNot(
                reservation.getTheme().getId(), date,
                reservationTime.getId(), reservation.getId());
        if (exists) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateSlotChanged(Slot previousSlot, Slot newSlot) {
        if (previousSlot.equals(newSlot)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }
    }

    private void promoteFirstWaiting(Slot previousSlot) {
        waitingDao.findFirstBySlot(previousSlot.getId()).ifPresent(waiting -> {
            reservationDao.save(new Reservation(previousSlot, waiting.getName()));
            waitingDao.delete(waiting.getId());
        });
    }

    @Transactional
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
