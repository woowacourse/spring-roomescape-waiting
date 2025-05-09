package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidInputException;
import roomescape.exception.NotCorrectDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(ReservationDao reservationDao,
                              ReservationTimeDao reservationTimeDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public Reservation createReservationAfterNow(ReservationRequest request, Member member) {
        LocalDate date = request.date();
        ReservationTime time = reservationTimeDao.findById(request.timeId());
        validateDateAndTime(date, time);

        return createReservation(request, member);
    }

    private void validateDateAndTime(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (date.isBefore(now.toLocalDate()) ||
                (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new NotCorrectDateTimeException("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.");
        }
    }

    public Reservation createReservation(ReservationRequest reservationRequest, Member member) {
        LocalDate date = reservationRequest.date();
        Long timeId = reservationRequest.timeId();
        Long themeId = reservationRequest.themeId();
        validateDuplicateReservation(date, timeId, themeId);
        ReservationTime time = reservationTimeDao.findById(timeId);
        Theme theme = themeDao.findById(themeId);
        return reservationDao.add(new Reservation(null, member.getName(), date, time, theme));
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationDao.existByDateTimeAndTheme(date, timeId, themeId)) {
            throw new DuplicateReservationException();
        }
    }

    public List<Reservation> findAllReservations() {
        return reservationDao.findAll();
    }

    public void deleteReservationById(Long id) {
        if (reservationDao.deleteById(id) == 0) {
            throw new InvalidInputException("존재하지 않는 예약 id이다.");
        }
    }
}
