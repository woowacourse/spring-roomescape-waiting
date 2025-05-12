package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.global.constant.GlobalConstant;
import roomescape.member.dao.MemberDao;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.dao.ThemeDao;
import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.global.exception.InvalidInputException;
import roomescape.reservation.exception.NotCorrectDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public Reservation createReservationAfterNow(ReservationCreateRequest request, Member member) {
        LocalDate date = request.date();
        ReservationTime time = reservationTimeDao.findById(request.timeId());
        validateDateAndTime(date, time);
        return createReservation(request, member);
    }

    private void validateDateAndTime(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(GlobalConstant.TIME_ZONE));
        if (date.isBefore(now.toLocalDate()) ||
                (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new NotCorrectDateTimeException("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.");
        }
    }

    public Reservation createReservation(AdminReservationCreateRequest adminReservationCreateRequest) {
        Member member = memberDao.findById(adminReservationCreateRequest.memberId());
        return generateReservation(
                adminReservationCreateRequest.date(),
                adminReservationCreateRequest.timeId(),
                adminReservationCreateRequest.themeId(),
                member);
    }

    public Reservation createReservation(ReservationCreateRequest reservationCreateRequest, Member member) {
        return generateReservation(
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId(),
                member);
    }

    private Reservation generateReservation(LocalDate date, Long timeId, Long themeId, Member member) {
        validateDuplicateReservation(date, timeId, themeId);
        ReservationTime time = reservationTimeDao.findById(timeId);
        Theme theme = themeDao.findById(themeId);
        return reservationDao.add(new Reservation(null, member, date, time, theme));
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationDao.existByDateTimeAndTheme(date, timeId, themeId)) {
            throw new DuplicateReservationException();
        }
    }

    public List<Reservation> findAllReservations() {
        return reservationDao.findAll();
    }

    public List<Reservation> findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        validateAtLeastOneFilterProvided(memberId, themeId, startDate, endDate);
        return reservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(memberId, themeId, startDate, endDate);
    }

    private void validateAtLeastOneFilterProvided(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate) {
        if (memberId == null && themeId == null && startDate == null && endDate == null) {
            throw new InvalidInputException("필터링할 조건을 하나 이상 입력하라.");
        }
    }

    public void deleteReservationById(Long id) {
        if (reservationDao.deleteById(id) == 0) {
            throw new InvalidInputException("존재하지 않는 예약 id이다.");
        }
    }
}
