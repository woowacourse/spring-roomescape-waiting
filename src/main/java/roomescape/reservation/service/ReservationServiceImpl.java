package roomescape.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.holiday.service.HolidayService;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationSaveServiceDto;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.service.TimeService;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeRepository themeRepository;
    private final HolidayService holidayService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeRepository themeRepository,
            HolidayService holidayService
    ) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeRepository = themeRepository;
        this.holidayService = holidayService;
    }

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    @Override
    public Reservation create(ReservationSaveServiceDto reservation) {
        ReservationTime time = findTime(reservation.timeId());
        Long themeId = reservation.themeId();
        time.validateReservableSchedule();
        validateThemeId(themeId);
        validateNotHoliday(time);
        validateDuplicatedReservation(themeId, time);
        Theme theme = themeRepository.findById(themeId);
        Reservation newReservation = new Reservation(reservation.name(), time).withTheme(theme);
        return reservationRepository.save(newReservation);
    }

    private void validateThemeId(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
        if (!themeRepository.existsById(themeId)) {
            throw new ThemeNotFoundException(themeId);
        }
    }

    private void validateNotHoliday(ReservationTime time) {
        if (holidayService.isHoliday(time.getDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }
    }

    private void validateDuplicatedReservation(Long themeId, ReservationTime time) {
        if (reservationRepository.isDuplicated(themeId, time)) {
            throw new DuplicateReservationException();
        }
    }

    private ReservationTime findTime(Long timeId) {
        if (timeId == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
        return timeService.findById(timeId);
    }

    @Override
    public void cancel(Long id) {
        boolean deleted = reservationRepository.deleteById(id);
        if (!deleted) {
            throw new ReservationNotFoundException(id);
        }
    }

    @Override
    public List<Reservation> getByName(String name) {
        return reservationRepository.findByName(name);
    }

    @Override
    public void cancelForUser(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.getTime().validateNotPastForCancel();
        reservationRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Reservation update(Long id, Long timeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.getTime().validateUpdatableReservation();
        ReservationTime newTime = findTime(timeId);
        newTime.validateReservableSchedule();
        validateDuplicatedReservation(reservation.getTheme().getId(), newTime);
        boolean updated = reservationRepository.update(id, timeId);
        if (!updated) {
            throw new IllegalStateException("예약 수정에 실패했습니다. id: " + id);
        }
        return reservation.withTime(newTime);
    }
}
