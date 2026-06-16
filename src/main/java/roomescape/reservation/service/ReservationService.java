package roomescape.reservation.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dao.ReservationDao;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.WaitingForPromotion;
import roomescape.waiting.dao.ReservationWaitingDao;

import java.time.LocalDate;
import java.util.List;

import static roomescape.global.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;
    private final ReservationWaitingDao reservationWaitingDao;

    public ReservationService(ReservationDao reservationDao, ThemeDao themeDao, TimeDao timeDao, ReservationWaitingDao reservationWaitingDao) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
        this.reservationWaitingDao = reservationWaitingDao;
    }

    public List<Reservation> findAll() {
        return reservationDao.selectAll();
    }

    public Reservation findById(Long id) {
        return reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));
    }

    public List<Reservation> findAllByName(String name) {
        return reservationDao.selectByName(name);
    }

    @Transactional
    public Reservation add(String name, Long themeId, LocalDate date, Long timeId) {
        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_TIME_NOT_FOUND));

        validateDateTime(date, time);
        validateThemeExists(themeId);

        if (reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)) {
            throw new RoomescapeException(RESERVATION_ALREADY_EXISTS);
        }

        Reservation newReservation = new Reservation(name, themeId, date, time, ReservationStatus.PENDING);
        try {
            return reservationDao.insert(newReservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(RESERVATION_ALREADY_EXISTS);
        }
    }

    @Transactional
    public Reservation modifyDateTimeByName(Long id, String name, Long themeId, LocalDate date, Long timeId) {
        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_TIME_NOT_FOUND));

        validateDateTime(date, time);
        validateThemeExists(themeId);

        if (reservationDao.existsByThemeIdAndDateAndTimeId(themeId, date, timeId)) {
            throw new RoomescapeException(RESERVATION_ALREADY_EXISTS);
        }

        Long originReservationId = reservationDao.lockById(id)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        Reservation originReservation = reservationDao.selectById(originReservationId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name);

        Reservation updated;
        try {
            updated = reservationDao.updateDateTimeById(id, date, timeId)
                    .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(RESERVATION_ALREADY_EXISTS);
        }

        promoteFirstWaiting(originReservation.getThemeId(), originReservation.getDate(), originReservation.getTime());

        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        Long originReservationId = reservationDao.lockById(id)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        Reservation originReservation = reservationDao.selectById(originReservationId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        reservationDao.deleteById(id);
        promoteFirstWaiting(originReservation.getThemeId(), originReservation.getDate(), originReservation.getTime());
    }

    @Transactional
    public void deleteByIdIfNameMatches(Long id, String name) {
        Long originReservationId = reservationDao.lockById(id)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        Reservation originReservation = reservationDao.selectById(originReservationId)
                .orElseThrow(() -> new RoomescapeException(RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name);
        validateDateTime(originReservation.getDate(), originReservation.getTime());

        reservationDao.deleteById(id);
        promoteFirstWaiting(originReservation.getThemeId(), originReservation.getDate(), originReservation.getTime());
    }

    private void promoteFirstWaiting(Long themeId, LocalDate date, ReservationTime time) {
        reservationWaitingDao.lockFirstByThemeAndDateAndTime(themeId, date, time)
                        .ifPresent(waitingId -> {
                            WaitingForPromotion waiting = reservationWaitingDao.selectFirstByThemeAndDateAndTime(themeId, date, time)
                                    .orElseThrow(() -> new RoomescapeException(RESERVATION_WAITING_NOT_FOUND));
                            reservationWaitingDao.deleteById(waiting.id());
                            reservationDao.insert(waiting.toReservation());
                        });
    }

    private void validateDateTime(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomescapeException(PAST_RESERVATION);
        }

        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new RoomescapeException(PAST_RESERVATION);
        }
    }

    private void validateThemeExists(Long themeId) {
        if (!themeDao.existsById(themeId)) {
            throw new RoomescapeException(THEME_NOT_FOUND);
        }
    }
}
