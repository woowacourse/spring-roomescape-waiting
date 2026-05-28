package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.*;
import roomescape.service.dto.Page;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao, Clock clock) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        return reservationDao.save(reservation);
    }

    @Transactional
    public Reservation update(long id, LocalDate date, long timeId) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        ReservationTime time = validateReservationTime(timeId);
        Reservation updated = reservation.withUpdated(date, time, LocalDateTime.now(clock));
        if (reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, reservation.getTheme().getId())) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        return reservationDao.update(updated);
    }

    @Transactional
    public void delete(long id) {
        reservationDao.findById(id).ifPresent(reservation -> {
            reservation.validateCancellable(LocalDateTime.now(clock));
            reservationDao.delete(id);
        });
    }

    public List<Reservation> findAllByName(String username) {
        return reservationDao.findByName(username);
    }

    public Page<Reservation> findAllWithCount(int page, int size) {
        List<Reservation> reservations = reservationDao.findAll(page, size);
        long totalCount = reservationDao.count();
        return new Page<>(reservations, totalCount);
    }

    @Transactional
    public Reservation saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        if (!reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationConflictException("예약 가능한 시간입니다. 일반 예약 API를 이용해주세요.");
        }
        if (reservationDao.existsReservationByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        if (reservationDao.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        return reservationDao.saveWaiting(reservation);
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationDao.findByWaitingId(id).ifPresent(reservation -> {
            reservation.validateCancellable(LocalDateTime.now(clock));
            reservationDao.deleteWaiting(id);
        });
    }

    public List<ReservationWaiting> findAllWaitingByName(String username) {
        return reservationDao.findAllWaitingByName(username);
    }

    public List<MyReservation> getMyReservations(String name) {
        List<MyReservation> reservations = findAllByName(name).stream()
                .map(MyReservation::reserved)
                .toList();
        List<MyReservation> waitings = findAllWaitingByName(name).stream()
                .map(waiting -> MyReservation.waiting(waiting.reservation(), waiting.waitingNumber()))
                .toList();
        return Stream.concat(reservations.stream(), waitings.stream())
                .sorted(Comparator
                        .comparing((MyReservation r) -> r.reservation().getDate())
                        .thenComparing(r -> r.reservation().getTime().getStartAt())
                        .thenComparing(MyReservation::reservationType))
                .toList();
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
