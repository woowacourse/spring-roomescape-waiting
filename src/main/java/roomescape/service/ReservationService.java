package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.User;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.UserDao;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final UserDao userDao;

    public ReservationService(ReservationDao reservationDao,
                              ReservationTimeDao reservationTimeDao,
                              ThemeDao themeDao,
                              UserDao userDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.userDao = userDao;
    }

    public List<Reservation> findAllReservations() {
        return reservationDao.getAllReservations();
    }

    public List<Reservation> filterReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return reservationDao.searchReservation(themeId, memberId, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationRequest request, User user) {
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId(),
                request.themeId());
        Theme theme = themeDao.findThemeById(request.themeId());
        Reservation reservation = new Reservation(request.date(), reservationTime, theme, user);
        return reservationDao.addReservation(reservation);
    }

    public Reservation addReservation(AdminReservationRequest request) {
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId(), request.themeId());
        Theme theme = themeDao.findThemeById(request.themeId());
        User user = userDao.findUserById(request.memberId())
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 사용자가 존재하지 않습니다.".formatted(request.memberId())));
        Reservation reservation = new Reservation(request.date(), reservationTime, theme, user);
        return reservationDao.addReservation(reservation);
    }

    private ReservationTime findReservationTime(LocalDate date, long timeId, long themeId) {
        ReservationTime reservationTime = reservationTimeDao.findReservationById(timeId);
        validateReservationDateTimeBeforeNow(date, reservationTime.getStartAt());
        validateDuplicatedReservation(date, themeId, timeId);
        return reservationTime;
    }

    private void validateReservationDateTimeBeforeNow(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("현재(%s) 이전 시간으로 예약할 수 없습니다.".formatted(now));
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long themeId, Long timeId) {
        long countReservation = reservationDao.countReservationByDateAndTimeId(date, timeId, themeId);
        if (countReservation > 0) {
            throw new DuplicatedException("이미 해당 시간에 예약이 존재합니다.");
        }
    }

    public void deleteReservation(long id) {
        validateExistReservation(id);
        reservationDao.deleteReservation(id);
    }

    private void validateExistReservation(long id) {
        long count = reservationDao.countReservationById(id);
        if (count <= 0) {
            throw new NotFoundException("해당 id:[%s] 값으로 예약된 내역이 존재하지 않습니다.".formatted(id));
        }
    }
}
