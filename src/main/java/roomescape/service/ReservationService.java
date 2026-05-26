package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Time;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.ThemeSlotRepository;
import roomescape.repository.TimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ThemeSlotRepository themeSlotRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeRepository timeRepository,
            ThemeRepository themeRepository,
            ThemeSlotRepository themeSlotRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.themeSlotRepository = themeSlotRepository;
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, Long reservationTimeId, Long themeId) {
        validateBeforeDate(date);
        validateIsExistBy(date, reservationTimeId, themeId);

        Theme theme = getThemeOrElseThrow(themeId);
        Time time = getTimeOrElseThrow(reservationTimeId);
        validateDateTime(date, time);

        Reservation reservation = reservationRepository.save(new Reservation(name, date, time, theme));
        themeSlotRepository.update(new ThemeSlot(theme, date, time, true));
        return reservation;
    }

    @Transactional
    public void removeReservation(long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        reservationRepository.deleteById(reservationId);
        themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
    }

    public Reservation findReservation(long reservationId) {
        return getReservationOrElseThrow(reservationId);
    }

    public List<Reservation> findReservationBy(String name) {
        return reservationRepository.findByName(name);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrElseThrow(reservationId);
        reservation.cancel();
        reservationRepository.updateStatus(reservation);
        themeSlotRepository.update(new ThemeSlot(reservation.getTheme(), reservation.getDate(), reservation.getTime(), false));
    }

    @Transactional
    public Reservation modifyReservation(Long reservationId, LocalDate date, Long timeId, Long themeId) {
        Time time = getTimeOrElseThrow(timeId);
        Theme theme = getThemeOrElseThrow(themeId);
        Reservation reservation = getReservationOrElseThrow(reservationId);

        validateIsExistBy(date, timeId, themeId);
        validateDateTime(date, time);

        Reservation updateReservation = new Reservation(
                reservationId,
                reservation.getName(),
                date,
                time,
                theme,
                reservation.getReservationStatus()
        );
        reservationRepository.updateDateAndTimeAndTheme(updateReservation);
        return updateReservation;
    }

    @NonNull
    private Theme getThemeOrElseThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_NOT_FOUND));
    }

    @NonNull
    private Time getTimeOrElseThrow(Long reservationTimeId) {
        return timeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new CustomException(ErrorCode.TIME_NOT_FOUND));
    }

    @NonNull
    private Reservation getReservationOrElseThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateBeforeDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_ALLOWED_DATE);
        }
    }

    private void validateIsExistBy(LocalDate date, Long reservationTimeId, Long themeId) {
        if (reservationRepository.isExistBy(themeId, date, reservationTimeId)) {
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_EXIST);
        }
    }

    private void validateDateTime(LocalDate date, Time time) {
        if (date.equals(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new CustomException(ErrorCode.RESERVATION_TIME_OUT);
        }
    }
}
