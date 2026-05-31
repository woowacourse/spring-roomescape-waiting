package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationWaitingDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationService reservationService;
    private final Clock clock;

    public ReservationWaitingService(ReservationWaitingDao reservationWaitingDao,
                                     ReservationService reservationService,
                                     Clock clock) {
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @Transactional
    public Reservation saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationService.getTime(timeId);
        Theme theme = reservationService.getTheme(themeId);
        if (!reservationService.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return reservationService.save(name, date, timeId, themeId);
        }
        if (reservationService.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        if (reservationWaitingDao.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        try {
            ReservationWaiting waiting = reservationWaitingDao.saveWaiting(reservation);
            return waiting.reservation();
        } catch (DuplicateKeyException e) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationWaitingDao.findByWaitingId(id).ifPresent(waiting -> {
            waiting.reservation().validateCancellable(LocalDateTime.now(clock));
            reservationWaitingDao.deleteWaiting(id);
        });
    }

    @Transactional(readOnly = true)
    public List<ReservationWaiting> findAllWaitingByName(String username) {
        return reservationWaitingDao.findAllWaitingByName(username);
    }
}
