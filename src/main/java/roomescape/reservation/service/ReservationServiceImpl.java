package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.holiday.service.HolidayService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
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
    public Reservation create(ReservationSaveServiceDto request) {
        ReservationTime time = findTime(request.timeId());
        Long themeId = request.themeId();
        time.validateReservableSchedule();
        validateThemeId(themeId);
        validateNotHoliday(time);
        Theme theme = themeRepository.findById(themeId);
        Status status = Status.RESERVED;
        if (isDuplicatedReservation(themeId, time)) {
            status = Status.WAITING;
        }
        Reservation newReservation = new Reservation(request.name(),
                time,
                theme,
                status,
                LocalDateTime.now());
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

    private boolean isDuplicatedReservation(Long themeId, ReservationTime time) {
        return reservationRepository.isDuplicated(themeId, time);
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
        if (isDuplicatedReservation(reservation.getTheme().getId(), newTime)) {
            throw new DuplicateReservationException();
        }
        boolean updated = reservationRepository.update(id, timeId);
        if (!updated) {
            throw new IllegalStateException("예약 수정에 실패했습니다. id: " + id);
        }
        return reservation.withTime(newTime);
    }
}
