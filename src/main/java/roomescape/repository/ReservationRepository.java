package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.dao.MemberDao;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ReservationTimeDao;
import roomescape.repository.dao.ThemeDao;
import roomescape.repository.dto.ReservationRowDto;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ReservationRepository {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationRepository(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public List<Reservation> findAllReservations() {
        List<ReservationRowDto> reservations = reservationDao.findAll();
        return makeReservations(reservations);
    }

    public Optional<ReservationTime> findReservationTimeById(long id) {
        return reservationTimeDao.findById(id);
    }

    public Optional<Theme> findThemeById(long id) {
        return themeDao.findById(id);
    }

    public Optional<Member> findMemberById(long id) {
        return memberDao.findById(id);
    }

    public boolean isExistReservationByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return reservationDao.isExistByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    public Reservation saveReservation(Reservation reservation) {
        ReservationRowDto reservationRowDto = ReservationRowDto.from(reservation);
        long id = reservationDao.save(reservationRowDto);
        ReservationRowDto saved = reservationDao.findById(id).orElseThrow(NoSuchElementException::new);
        return makeReservation(saved, id);
    }

    public boolean isExistReservationById(long id) {
        return reservationDao.isExistById(id);
    }

    public void deleteReservationById(long id) {
        reservationDao.deleteById(id);
    }

    public List<ReservationTime> findReservationTimeBooked(LocalDate date, long themeId) {
        List<ReservationRowDto> reservations = reservationDao.findByDateAndThemeId(date, themeId);
        Set<Long> timeIds = extractTimeIds(reservations);
        return makeReservationTimes(timeIds);
    }

    private Set<Long> extractTimeIds(List<ReservationRowDto> reservations) {
        return reservations.stream()
                .map(ReservationRowDto::getTimeId)
                .collect(Collectors.toSet());
    }

    public List<ReservationTime> findReservationTimeNotBooked(LocalDate date, long themeId) {
        List<ReservationTime> result = reservationTimeDao.findAll();
        List<ReservationTime> bookedTimes = findReservationTimeBooked(date, themeId);
        result.removeAll(bookedTimes);
        return result;
    }

    public List<Reservation> findReservationsByMemberIdAndThemeIdAndDate(long memberId, long themeId, LocalDate from, LocalDate to) {
        List<ReservationRowDto> reservations = reservationDao.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
        return makeReservations(reservations);
    }

    private List<ReservationTime> makeReservationTimes(Set<Long> timeIds) {
        List<ReservationTime> result = new ArrayList<>();
        for (long timeId : timeIds) {
            ReservationTime time = reservationTimeDao.findById(timeId).orElseThrow(NoSuchElementException::new);
            result.add(time);
        }
        return result;
    }

    private List<Reservation> makeReservations(List<ReservationRowDto> reservations) {
        List<Reservation> result = new ArrayList<>();
        for (ReservationRowDto reservationDto : reservations) {
            Reservation reservation = makeReservation(reservationDto);
            result.add(reservation);
        }
        return result;
    }

    private Reservation makeReservation(ReservationRowDto reservation, long id) {
        long timeId = reservation.getTimeId();
        long themeId = reservation.getThemeId();
        long memberId = reservation.getMemberId();
        ReservationTime time = reservationTimeDao.findById(timeId).orElseThrow(NoSuchElementException::new);
        Theme theme = themeDao.findById(themeId).orElseThrow(NoSuchElementException::new);
        Member member = memberDao.findById(memberId).orElseThrow(NoSuchElementException::new);
        return new Reservation(id, reservation.getDate(), time, theme, member);
    }

    private Reservation makeReservation(ReservationRowDto reservation) {
        return makeReservation(reservation, reservation.getId());
    }
}
