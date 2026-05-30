package roomescape.service;

import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.*;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.Booking;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeSlotRepository timeSlotRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    public List<Booking> findReservationByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<Waiting> waitings = waitingRepository.findByName(name);

        List<Booking> bookings = new ArrayList<>();

        for (Reservation reservation : reservations) {
            bookings.add(Booking.fromReservation(reservation));
        }

        for (Waiting waiting : waitings) {
            TimeSlot timeSlot = timeSlotRepository.findById(waiting.getTimeSlotId())
                    .orElseThrow(() -> new TimeSlotNotFoundException(waiting.getTimeSlotId()));

            Theme theme = themeRepository.findById(waiting.getThemeId())
                    .orElseThrow(() -> new ThemeNotFoundException(waiting.getThemeId()));

            bookings.add(Booking.fromWaiting(waiting, timeSlot, theme));
        }

        return bookings;
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Reservation transientReservation = createTransientWithValidField(name, date, timeId, themeId);
        validDuplicatedReservation(transientReservation);
        return reservationRepository.save(transientReservation);
    }

    @Transactional
    public void removeReservation(long reservationId, String userName) {
        existsAndModifiableReservation(reservationId, userName);
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void putReservation(long id, String userName, String name, LocalDate date, Long timeId, Long themeId) {
        existsAndModifiableReservation(id, userName);
        Reservation transientReservation = createTransientWithValidField(name, date, timeId, themeId);
        Reservation reservation = new Reservation(
                id,
                transientReservation.getName(),
                transientReservation.getDate(),
                transientReservation.getTimeSlot(),
                transientReservation.getTheme()
        );
        validDuplicatedReservation(reservation);
        reservationRepository.update(reservation);
    }

    @Transactional
    public void patchReservation(long id, String userName, String name, LocalDate date, Long timeId, Long themeId) {
        Reservation reservation = findReservationById(id);
        reservation.validateModifiable(userName);
        validNotPast(reservation.getDate(), reservation.getTimeSlot().getStartAt());
        reservation.reschedule(
                name,
                date,
                findOptionalTime(timeId),
                findOptionalTheme(themeId)
        );
        validDuplicatedReservation(reservation);
        reservationRepository.update(reservation);
    }

    private void existsAndModifiableReservation(long id, String userName) {
        Reservation existingReservation = findReservationById(id);
        existingReservation.validateModifiable(userName);
        validNotPast(existingReservation.getDate(), existingReservation.getTimeSlot().getStartAt());
    }

    private void validNotPast(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now()))) {
            throw new PastReservationControlException();
        }
    }

    private Reservation createTransientWithValidField(String name, LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        validDateTime(date, timeSlot.getStartAt());
        return Reservation.transientOf(name, date, timeSlot, theme);
    }

    private void validDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new PastTimeException("지난 날짜로 예약하실 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new PastTimeException("지난 시간으로 예약하실 수 없습니다.");
        }
    }

    private void validDuplicatedReservation(Reservation reservation) {
        reservationRepository.findByDateAndTimeIdAndThemeId(
                        reservation.getDate(),
                        reservation.getTimeSlot().getId(),
                        reservation.getTheme().getId()
                )
                .filter(existing -> reservation.getId() == null || !reservation.getId().equals(existing.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateReservationException(
                            existing.getDate().toString(),
                            existing.getTimeSlot().getId(),
                            existing.getTheme().getId()
                    );
                });
    }

    private TimeSlot findOptionalTime(Long id) {
        return Optional.ofNullable(id).map(this::findTimeSlot).orElse(null);
    }

    private Theme findOptionalTheme(Long id) {
        return Optional.ofNullable(id).map(this::findTheme).orElse(null);
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id).orElseThrow(() -> new TimeSlotNotFoundException(id));
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id).orElseThrow(() -> new ThemeNotFoundException(id));
    }
}
