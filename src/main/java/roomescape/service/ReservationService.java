package roomescape.service;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Rank;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationResult;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final SlotRepository slotRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              SlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public ReservationResult reserve(ReservationCreateRequest request, LocalDateTime now) {
        ReservationTime reservationTime = findReservationTimeByTimeId(request.getTimeId());
        Theme theme = findThemeByThemeId(request.getThemeId());

        validateIsDuplicateNameReservation(request.getTimeId(), request.getThemeId(), request.getDate(),
                request.getName());
        Status status = determineStatus(request.getTimeId(), request.getThemeId(), request.getDate());

        Slot slot = null;
        try {
            slot = slotRepository.findByDateAndTimeAndTheme(request.getDate(), reservationTime.getId(),
                    theme.getId()).orElseGet(() -> slotRepository.save(
                    Slot.create(new ReservationDate(request.getDate()), reservationTime, theme)));

        } catch (DataIntegrityViolationException e) {
            slot = slotRepository.findByDateAndTimeAndTheme(request.getDate(), reservationTime.getId(), theme.getId())
                    .get();
            status = Status.WAITING;
        }

        Reservation reservation = Reservation.reserve(new ReservationName(request.getName()), slot, status, now);
        Reservation saved = reservationRepository.save(reservation);

        Reservations reservations = new Reservations(reservationRepository.findByTimeAndThemeAndDate(
                saved.getTime(), saved.getTheme(), saved.getDate()));

        Rank rank = reservations.rankOf(saved);

        return new ReservationResult(rank, saved);
    }

    public ReservationResult find(long reservationId) {
        Reservation reservation = findReservationById(reservationId);

        Reservations reservations = new Reservations(reservationRepository.findByTimeAndThemeAndDate(
                reservation.getTime(), reservation.getTheme(), reservation.getDate()));

        Rank rank = reservations.rankOf(reservation);

        return new ReservationResult(rank, reservation);
    }

    public List<ReservationResult> findList(String name) {
        List<Reservation> reservations = findListByName(name);
        List<ReservationResult> reservationResults = new ArrayList<>();

        for (Reservation reservation : reservations) {
            Reservations sameScheduleReservations = new Reservations(reservationRepository.findByTimeAndThemeAndDate(
                    reservation.getTime(), reservation.getTheme(), reservation.getDate()));

            Rank rank = sameScheduleReservations.rankOf(reservation);

            ReservationResult result = new ReservationResult(rank, reservation);
            reservationResults.add(result);
        }

        return reservationResults;
    }

    private List<Reservation> findListByName(String name) {
        if (name == null) {
            return reservationRepository.findAll();
        }
        return reservationRepository.findAllByName(name);
    }

    @Transactional
    public ReservationResult update(ReservationUpdateRequest request, long id, LocalDateTime now) {
        Reservation originReservation = findReservationById(id);
        originReservation.ensureNotPast(now);

        ReservationDate reservationDateToUpdate = new ReservationDate(request.getDate());
        ReservationTime reservationTimeToUpdate = findReservationTimeByTimeId(request.getTimeId());

        validateIsDuplicateNameReservation(request.getTimeId(), request.getThemeId(), request.getDate(),
                request.getName());

        Slot slotToUpdate = findOrCreateSlot(reservationDateToUpdate, reservationTimeToUpdate,
                originReservation.getTheme());

        Status status = determineStatus(request.getTimeId(), request.getThemeId(), request.getDate());
        Reservation target = Reservation.reserve(originReservation.getName(), slotToUpdate, status, now);

        Reservation updated = reservationRepository.update(id, target);

        reservationRepository.findFirstWaitingByTimeAndThemeAndDate(
                originReservation.getTime().getId(),
                originReservation.getTheme().getId(),
                originReservation.getDate().getValue()
        ).ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), Status.APPROVED));

        Reservations reservations = new Reservations(reservationRepository.findByTimeAndThemeAndDate(
                updated.getTime(), updated.getTheme(), updated.getDate()));

        Rank rank = reservations.rankOf(updated);

        return new ReservationResult(rank, updated);
    }

    @Transactional
    public void cancel(long reservationId, LocalDateTime now) {
        Reservation reservation = findReservationById(reservationId);
        reservation.ensureNotPast(now);

        Status cancelledStatus = reservation.getStatus();
        reservationRepository.deleteById(reservationId);

        if (cancelledStatus == Status.APPROVED) {
            reservationRepository.findFirstWaitingByTimeAndThemeAndDate(
                    reservation.getTime().getId(),
                    reservation.getTheme().getId(),
                    reservation.getDate().getValue()
            ).ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), Status.APPROVED));
        }
    }

    private Slot findOrCreateSlot(ReservationDate date, ReservationTime time, Theme theme) {
        return slotRepository.findByDateAndTimeAndTheme(date.getValue(), time.getId(), theme.getId())
                .orElseGet(() -> slotRepository.save(Slot.create(date, time, theme)));
    }

    private Status determineStatus(long timeId, long themeId, LocalDate date) {
        if (reservationRepository.existsApprovedByTimeAndThemeAndDate(timeId, themeId, date)) {
            return Status.WAITING;
        }
        return Status.APPROVED;
    }

    private ReservationTime findReservationTimeByTimeId(long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme findThemeByThemeId(long themeId) {
        return themeRepository.findById(themeId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateIsDuplicateNameReservation(long timeId, long themeId, LocalDate date, String name) {
        if (reservationRepository.existsByTimeAndThemeAndDateAndName(timeId, themeId, date, name)) {
            throw new RoomEscapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Reservation findReservationById(long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND));
    }
}
