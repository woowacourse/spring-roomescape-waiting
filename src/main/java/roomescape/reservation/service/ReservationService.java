package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.reservation.dao.ReservationDao;
import roomescape.payment.service.PaymentService;
import roomescape.theme.Theme;
import roomescape.theme.dao.ThemeDao;
import roomescape.time.ReservationTime;
import roomescape.time.dao.TimeDao;
import roomescape.waiting.ReservationWaiting;
import roomescape.waiting.dao.ReservationWaitingDao;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;
    private final TimeDao timeDao;
    private final ReservationWaitingDao reservationWaitingDao;
    private final PaymentService paymentService;

    public ReservationService(ReservationDao reservationDao, ThemeDao themeDao, TimeDao timeDao,
                              ReservationWaitingDao reservationWaitingDao, PaymentService paymentService) {
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
        this.timeDao = timeDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.paymentService = paymentService;
    }

    public List<Reservation> findAll() {
        return reservationDao.selectAll();
    }

    public Reservation findById(Long id) {
        return reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<MyReservation> findAllByName(String name) {
        List<MyReservation> myReservations = new ArrayList<>();
        Map<Long, Theme> themes = new HashMap<>();

        List<Reservation> reservations = reservationDao.selectByName(name);
        for (Reservation reservation : reservations) {
            Theme theme = getThemeById(reservation.getThemeId(), themes);
            myReservations.add(new MyReservation(reservation, theme,
                    paymentService.findOrderByReservationId(reservation.getId()).orElse(null)));
        }

        List<ReservationWaiting> reservationWaitings = reservationWaitingDao.selectByName(name);
        for (ReservationWaiting reservationWaiting : reservationWaitings) {
            Theme theme = getThemeById(reservationWaiting.getThemeId(), themes);
            myReservations.add(new MyReservation(reservationWaiting, theme));
        }

        return myReservations;
    }

    @Transactional
    public Reservation add(String name, Long themeId, LocalDate date, Long timeId) {
        validateThemeExists(themeId);
        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Reservation newReservation = new Reservation(name, themeId, date, time);
        newReservation.validateDateTime(date, time, ErrorCode.CANNOT_RESERVE_PAST_DATETIME);

        List<Reservation> reservedList = reservationDao.selectByThemeIdAndDate(themeId, date);
        for (Reservation reserved : reservedList) {
            validateReserved(timeId, reserved.getTime());
        }

        return reservationDao.insert(newReservation);
    }

    @Transactional
    public Reservation modifyDateTimeByName(Long id, String name, Long themeId, LocalDate date, Long timeId) {
        Reservation originReservation = reservationDao.selectById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        originReservation.validateSameName(name, ErrorCode.CANNOT_MODIFY_OTHER_RESERVATION);

        ReservationTime time = timeDao.selectById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
        originReservation.validateDateTime(date, time, ErrorCode.CANNOT_RESERVE_PAST_DATETIME);
        validateThemeExists(themeId);

        List<Reservation> reservedList = reservationDao.selectByThemeIdAndDate(themeId, date);
        for (Reservation reserved : reservedList) {
            validateReserved(timeId, reserved.getTime());
        }

        return reservationDao.updateDateTimeById(id, date, timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public void deleteById(Long id) {
        reservationDao.deleteById(id);
    }

    @Transactional
    public void deleteByIdIfNameMatches(Long id, String name) {
        Reservation reservation = reservationDao.selectByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.validateSameName(name, ErrorCode.CANNOT_DELETE_OTHER_RESERVATION);
        reservation.validateDateTime(reservation.getDate(), reservation.getTime(),
                ErrorCode.CANNOT_DELETE_PAST_RESERVATION);

        Optional<ReservationWaiting> firstWaiting = reservationWaitingDao.selectFirstWaitingForUpdate(reservation);
        if (firstWaiting.isEmpty()) {
            reservationDao.deleteById(id);
            return;
        }

        Reservation approvedReservation = reservation.approve(firstWaiting.get());
        reservationDao.updateNameByThemeIdAndDateAndTimeId(approvedReservation)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationWaitingDao.deleteById(firstWaiting.get().getId());
        paymentService.createReservationOrder(approvedReservation.getId());
    }

    private Theme getThemeById(Long themeId, Map<Long, Theme> themes) {
        return themes.computeIfAbsent(themeId, id -> themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND)));
    }

    private void validateReserved(Long timeId, ReservationTime reservedTime) {
        if (timeId.equals(reservedTime.getId())) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateThemeExists(Long themeId) {
        themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_NOT_FOUND));
    }
}
