package roomescape.service;

import roomescape.common.exception.ReservationErrorCode;
import roomescape.common.exception.ReservationTimeErrorCode;
import roomescape.common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ThemeErrorCode;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Status;
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

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        boolean hasApproved = reservationRepository.existsApprovedByTimeAndThemeAndDate(
                request.getTimeId(), request.getThemeId(), request.getDate());
        Status status = hasApproved ? Status.WAITING : Status.APPROVED;

        Reservation reservation = Reservation.reserve(
                new ReservationName(request.getName()),
                new ReservationDate(request.getDate()), reservationTime,
                theme,
                now, status);
        return reservationRepository.save(reservation);
    }

    public Reservation find(long id) {
        return findReservationById(id);
    }

    public List<Reservation> findList(String name) {
        return findByNameOrAll(name);
    }

    private List<Reservation> findByNameOrAll(String name) {
        if (name != null) {
            return reservationRepository.findAllByName(name);
        }
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation reservation = findReservationById(id);

        ReservationDate newDate = new ReservationDate(request.getDate());
        ReservationTime newTime = findReservationTimeByTimeId(request.getTimeId());
        Theme newTheme = findThemeByThemeId(request.getThemeId());

        validateIsDuplicateReservation(request.getTimeId(), request.getThemeId(), request.getDate(), request.getName());

        boolean hasApprovedInNewSlot = reservationRepository.existsApprovedByTimeAndThemeAndDate(
                newTime.getId(), newTheme.getId(), newDate.getDate());
        Status newStatus = hasApprovedInNewSlot ? Status.WAITING : Status.APPROVED;

        Reservation target = Reservation.reserve(new ReservationName(request.getName()), newDate, newTime,
                newTheme, now, newStatus);

        Reservation updated = reservationRepository.update(id, target);

        if (reservation.isApproved()) {
            reservationRepository.findFirstWaitingByTimeAndThemeAndDate(
                            reservation.getTime(), reservation.getTheme(), reservation.getDate())
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }

        return updated;
    }

    @Transactional
    public void cancel(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);

        if (!reservation.isSameName(name)) {
            throw new RoomEscapeException(ReservationErrorCode.UNAUTHORIZED_SAME_NAME);
        }

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            reservationRepository.findFirstWaitingByTimeAndThemeAndDate(
                            reservation.getTime(), reservation.getTheme(), reservation.getDate())
                    .ifPresent(waiting -> reservationRepository.updateStatusById(waiting.getId(), Status.APPROVED));
        }
    }

    private ReservationTime findReservationTimeByTimeId(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme findThemeByThemeId(long id) {
        return themeRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
    }

    private void validateIsDuplicateReservation(long timeId, long themeId, LocalDate date, String name) {
        if (reservationRepository.existsByTimeAndThemeAndDateAndName(timeId, themeId, date, name)) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Reservation findReservationById(long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }
}
