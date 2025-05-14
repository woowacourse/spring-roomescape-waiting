package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(ReservationDao reservationDao, MemberDao memberDao,
        ReservationTimeDao reservationTimeDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public List<Reservation> findAllReservations() {
        return reservationDao.findAllReservations();
    }

    public List<Reservation> findReservationsByFilters(Long themeId, Long memberId, LocalDate dateFrom,
        LocalDate dateTo) {
        return reservationDao.findReservationsByFilters(themeId, memberId, dateFrom, dateTo);
    }

    public Reservation addReservationAfterNow(Member member, ReservationRequest request) {
        LocalDate date = request.date();
        ReservationTime time = reservationTimeDao.findTimeById(request.timeId());
        validateDateTimeAfterNow(date, time);

        return addReservation(member, request);
    }

    public Reservation addReservation(ReservationRequest request) {
        validateDuplicateReservation(request);

        Member member = memberDao.findMemberById(request.memberId());

        return addReservation(member, request);
    }

    private Reservation addReservation(Member member, ReservationRequest request) {
        validateDuplicateReservation(request);

        ReservationTime time = reservationTimeDao.findTimeById(request.timeId());
        Theme theme = themeDao.findThemeById(request.themeId());

        return reservationDao.addReservation(
            new Reservation(member, request.date(), time, theme));
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationDao.existReservationByDateTimeAndTheme(
            request.date(), request.timeId(), request.themeId())) {
            throw new DuplicatedException("reservation");
        }
    }

    private void validateDateTimeAfterNow(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (date.isBefore(now.toLocalDate()) ||
            (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    public void removeReservation(Long id) {
        reservationDao.removeReservationById(id);
    }
}
