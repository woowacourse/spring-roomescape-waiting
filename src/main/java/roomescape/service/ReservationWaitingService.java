package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservatinWaiting.ReservationWaiting;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ReservationAlreadyExistException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationWaitingQueryingDao;
import roomescape.repository.ReservationWaitingUpdatingDao;
import roomescape.repository.ThemeQueryingDao;

public class ReservationWaitingService {

    private final ReservationWaitingUpdatingDao reservationWaitingUpdatingDao;
    private final ReservationWaitingQueryingDao reservationWaitingQueryingDao;
    private final ReservationQueryingDao reservationQueryingDao;
    private final ReservationTimeQueryingDao reservationTimeQueryingDao;
    private final ThemeQueryingDao themeQueryingDao;

    public ReservationWaitingService(ReservationWaitingUpdatingDao reservationWaitingUpdatingDao, ReservationWaitingQueryingDao reservationWaitingQueryingDao, ReservationQueryingDao reservationQueryingDao, ReservationTimeQueryingDao reservationTimeQueryingDao, ThemeQueryingDao themeQueryingDao) {
        this.reservationWaitingUpdatingDao = reservationWaitingUpdatingDao;
        this.reservationWaitingQueryingDao = reservationWaitingQueryingDao;
        this.reservationQueryingDao = reservationQueryingDao;
        this.reservationTimeQueryingDao = reservationTimeQueryingDao;
        this.themeQueryingDao = themeQueryingDao;
    }

    public ReservationWaitingResponse create(ReservationWaitingRequest reservationWaitingReq) {
        ReservationTime reservationTimeById = reservationTimeQueryingDao.findReservationTimeById(reservationWaitingReq.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(reservationWaitingReq.timeId()));
        Theme themeById = themeQueryingDao.findThemeById(reservationWaitingReq.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(reservationWaitingReq.themeId()));

        if (reservationWaitingReq.date().isBefore(LocalDate.now())) {
            throw new ExpiredDateTimeException();
        }

        if (reservationWaitingReq.date().isEqual(LocalDate.now()) && reservationTimeById.getStartAt().isBefore(LocalTime.now())) {
            throw new ExpiredDateTimeException();
        }

        Reservation reservation = getReservationByThemeAndDateAndTime(reservationWaitingReq.themeId(), reservationWaitingReq.date(), reservationWaitingReq.timeId());
        reservation.validateDuplicatedReservationByName(reservationWaitingReq.name());

        if(reservationWaitingQueryingDao.isExistByNameAndDateAndTimeIdAndThemeId(reservationWaitingReq.name(), reservationWaitingReq.date(), reservationWaitingReq.timeId(), reservationWaitingReq.timeId())) {
            throw new InvalidInputException("이미 해당 예약에 대기열이 존재합니다.");
        }

        ReservationWaiting reservationWaiting = reservationWaitingReq.to(reservationTimeById, themeById);
        Long id = reservationWaitingUpdatingDao.create(reservationWaiting);

        return ReservationWaitingResponse.from(reservationWaiting.withReservationWaitingId(id));
    }

    private Reservation getReservationByThemeAndDateAndTime(Long themeId, LocalDate date, Long timeId) {
        return reservationQueryingDao.findReservationByThemeAndDateAndTime(themeId, date, timeId).stream()
                .findFirst()
                .orElseThrow(ReservationAlreadyExistException::new);
    }
}
