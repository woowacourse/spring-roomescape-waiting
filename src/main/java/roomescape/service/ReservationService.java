package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateRequest request, LocalDateTime now) {
        ReservationTime reservationTime = findReservationTimeByTimeId(request.getTimeId());
        Theme theme = findThemeByThemeId(request.getThemeId());

        Reservation reservation = Reservation.reserve(new ReservationName(request.getName()),
                new ReservationDate(request.getDate()), reservationTime, theme, now);

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        return reservationRepository.save(reservation);
    }

    public Reservation find(long reservationId) {
        return findReservationById(reservationId);
    }

    public List<Reservation> findList(String name) {
        if (name != null) {
            return reservationRepository.findAllByName(name);
        }
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation reservation = findReservationById(id);
        reservation.ensureNotPast(now);

        ReservationDate reservationDate = new ReservationDate(request.getDate());
        ReservationTime reservationTime = findReservationTimeByTimeId(request.getTimeId());

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        Reservation target = Reservation.reserve(reservation.getName(), reservationDate, reservationTime,
                reservation.getTheme(), now);
        target.ensureNotPast(now);

        return reservationRepository.update(id, target);
    }

    @Transactional
    public void cancel(long reservationId, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);
        reservation.ensureNotPast(now);

        reservationRepository.deleteById(reservationId);
    }

    private ReservationTime findReservationTimeByTimeId(long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme findThemeByThemeId(long themeId) {
        return themeRepository.findById(themeId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateIsDuplicateReservation(long timeId, long themeId, LocalDate date, String name) {
        if (reservationRepository.existsByTimeAndThemeAndDateAndName(timeId, themeId, date, name)) {
            throw new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Reservation findReservationById(long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
