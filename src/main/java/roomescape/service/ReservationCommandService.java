package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private ReservationTime findTimeReference(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }

    private Reservation findReservation(long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다."));
    }

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = findTimeReference(timeId);
        Theme theme = findThemeReference(themeId);
        Slot slot = new Slot(date, time, theme);

        slot.validateNotPast(LocalDateTime.now(clock));
        if (reservationDao.findBySlot(slot).isPresent()) {
            throw new DuplicateException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }
        return reservationDao.save(new Reservation(null, name, slot));
    }

    public void delete(long reservationId) {
        reservationDao.deleteById(reservationId);
    }

    public void cancel(long reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateCancelable(LocalDateTime.now(clock));
        reservationDao.deleteById(reservationId);
    }

    public Reservation update(long reservationId, LocalDate newDate, long newTimeId) {
        ReservationTime newTime = findTimeReference(newTimeId);
        Reservation current = findReservation(reservationId);
        Slot newSlot = new Slot(newDate, newTime, current.slot().theme());

        newSlot.validateNotPast(LocalDateTime.now(clock));
        boolean isDuplicate = reservationDao.findBySlot(newSlot)
                .filter(existing -> existing.id() != reservationId)
                .isPresent();
        if (isDuplicate) {
            throw new DuplicateException("변경하려는 시간에 이미 다른 예약이 존재합니다.");
        }
        return reservationDao.update(current.withSlot(newSlot));
    }
}
