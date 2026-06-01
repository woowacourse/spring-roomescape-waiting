package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationAndWaiting;

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

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
    }

    public List<ReservationAndWaiting> findReservationByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<Waiting> waitings = waitingRepository.findByName(name);

        List<ReservationAndWaiting> reservationAndWaitings = new ArrayList<>();

        for (Reservation reservation : reservations) {
            reservationAndWaitings.add(ReservationAndWaiting.fromReservation(reservation));
        }

        for (Waiting waiting : waitings) {
            TimeSlot timeSlot = timeSlotRepository.findById(waiting.getTimeSlotId())
                    .orElseThrow(TimeSlotNotFoundException::new);

            Theme theme = themeRepository.findById(waiting.getThemeId())
                    .orElseThrow(ThemeNotFoundException::new);

            reservationAndWaitings.add(ReservationAndWaiting.fromWaiting(waiting, timeSlot, theme));
        }

        return reservationAndWaitings;
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        validateDuplicatedReservation(date, timeId, themeId);
        Reservation transientReservation = createReservation(name, date, timeId, themeId);
        return reservationRepository.save(transientReservation);
    }

    @Transactional
    public void removeReservation(long id, String name) {
        Reservation reservation = findReservationById(id);
        reservation.validateCancelable(LocalDateTime.now(), name);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void updateReservation(long id, String name, LocalDate date, Long timeId) {
        Reservation nowReservation = findReservationById(id);
        TimeSlot timeSlot = findTimeSlot(timeId);
        Reservation updatedReservation = nowReservation.updateDateAndTime(name, date, timeSlot, LocalDateTime.now());

        if (!nowReservation.hasSameDateAndTime(updatedReservation)) {
            validateDuplicatedReservation(date, timeId, nowReservation.getTheme().getId());
            reservationRepository.update(updatedReservation);
        }
    }

    private Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlot(timeId);
        Theme theme = findTheme(themeId);
        return new Reservation(name, date, timeSlot, theme, LocalDateTime.now());
    }

    private void validateDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, timeId, themeId)) {
            throw new DuplicateReservationException();
        }
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(TimeSlotNotFoundException::new);
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(ThemeNotFoundException::new);
    }
}
