package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
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
@Transactional
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Slot slot = new Slot(date, findTimeReference(timeId), findThemeReference(themeId));
        Reservation reservation = Reservation.forNew(member, slot);

        slot.validateNotPast(now);
        validateNoDuplicate(slot);

        try {
            return reservationDao.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public void delete(long reservationId) {
        reservationDao.deleteById(reservationId);
    }

    public void cancel(long reservationId, String name) {
        Member member = new Member(name);
        Reservation reservation = findReservation(reservationId);

        reservation.validateOwnedBy(member);
        reservation.validateNotStarted(LocalDateTime.now(clock));

        reservationDao.deleteById(reservationId);
    }

    public Reservation update(long reservationId, String name, LocalDate newDate, long newTimeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Reservation oldReservation = findReservation(reservationId);
        Slot newSlot = new Slot(newDate, findTimeReference(newTimeId), oldReservation.slot().theme());

        oldReservation.validateOwnedBy(member);
        oldReservation.validateNotStarted(now);
        newSlot.validateNotPast(now);
        validateNoDuplicateExcluding(newSlot, reservationId);

        try {
            return reservationDao.update(oldReservation.withSlot(newSlot));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    private void validateNoDuplicate(Slot slot) {
        if (reservationDao.existsBySlot(slot)) {
            throw new DuplicateException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    private void validateNoDuplicateExcluding(Slot slot, long excludedId) {
        reservationDao.findBySlot(slot)
                .filter(existing -> existing.id() != excludedId)
                .ifPresent(existing -> {
                    throw new DuplicateException("해당 시간에 이미 예약이 존재합니다.");
                });
    }

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
}