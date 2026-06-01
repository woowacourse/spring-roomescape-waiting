package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.command.ReservationCommand;
import roomescape.service.command.ReservationUpdateCommand;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private ReservationTime findTimeReference(Long timeId) {
        try {
            return reservationTimeDao.findById(timeId);
        } catch (ResourceNotFoundException e) {
            throw new InvalidReferenceException("존재하지 않는 예약 시간입니다.");
        }
    }

    private Theme findThemeReference(Long themeId) {
        try {
            return themeDao.findById(themeId);
        } catch (ResourceNotFoundException e) {
            throw new InvalidReferenceException("존재하지 않는 테마입니다.");
        }
    }

    public Reservation create(ReservationCommand command) {
        Slot slot = Slot.from(
                Schedule.from(
                        command.date(),
                        findTimeReference(command.timeId())),
                findThemeReference(command.themeId())
        );

        if (reservationDao.findBySlot(slot).isPresent()) {
            throw new DuplicateException("해당 날짜와 시간에 이미 예약이 존재합니다.");
        }

        return reservationDao.save(Reservation.create(command.name(), slot, LocalDateTime.now(clock)));
    }

    public void deleteByAdmin(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        reservationDao.delete(reservation);
    }

    public void cancelByUser(Long reservationId, UserName name) {
        Reservation reservation = reservationDao.findById(reservationId);
        reservation.validateOwnedBy(name);
        reservation.validateCancelable(LocalDateTime.now(clock));
        reservationDao.delete(reservation);
    }

    public Reservation updateByUser(Long reservationId, UserName name, ReservationUpdateCommand command) {
        Reservation current = reservationDao.findById(reservationId);
        current.validateOwnedBy(name);

        Slot slot = Slot.from(
                Schedule.from(
                        command.date(),
                        findTimeReference(command.timeId())),
                current.getReservationTheme()
        );

        boolean isDuplicate = reservationDao.findBySlot(slot)
                .filter(existing -> existing.getId() != reservationId)
                .isPresent();

        if (isDuplicate) {
            throw new DuplicateException("변경하려는 시간에 이미 다른 예약이 존재합니다.");
        }

        return reservationDao.updateDateAndTime(current.withSlot(slot, LocalDateTime.now(clock)));
    }
}
