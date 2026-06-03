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
import roomescape.domain.WaitingLine;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotOwnerException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationAndWaiting;
import roomescape.service.dto.WaitingWithNumber;

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
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    public List<ReservationAndWaiting> findReservationAndWaitingByName(String name) {
        List<ReservationAndWaiting> reservationAndWaitings = new ArrayList<>();

        reservationRepository.findByName(name).stream()
                .map(ReservationAndWaiting::fromReservation)
                .forEach(reservationAndWaitings::add);

        waitingRepository.findByName(name).stream()
                .map(this::createWaitingWithNumber)
                .map(ReservationAndWaiting::fromWaiting)
                .forEach(reservationAndWaitings::add);

        return reservationAndWaitings;
    }

    @Transactional
    public Reservation saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Reservation reservation = createReservation(name, date, timeId, themeId);
        validateDuplicatedReservation(date, timeId, themeId);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void removeReservation(long id, String requestName) {
        Reservation reservation = findReservationById(id);
        validateReservationOwner(reservation, requestName);
        reservation.validateCancelable(LocalDateTime.now());
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void updateReservation(long id, String requestName, LocalDate date, Long timeId) {
        Reservation nowReservation = findReservationById(id);
        validateReservationOwner(nowReservation, requestName);

        TimeSlot timeSlot = findTimeSlot(timeId);
        Reservation updatedReservation = nowReservation.updateDateAndTime(date, timeSlot, LocalDateTime.now());

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
            throw new DuplicateException("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
        }
    }

    private void validateReservationOwner(Reservation reservation, String requestName) {
        if (!reservation.isOwner(requestName)) {
            throw new NotOwnerException();
        }
    }

    private WaitingWithNumber createWaitingWithNumber(Waiting waiting) {
        WaitingLine waitingLine = new WaitingLine(waitingRepository.findByDateAndTimeAndTheme(
                waiting.getDate(),
                waiting.getTimeSlot().getId(),
                waiting.getTheme().getId()
        ));
        return new WaitingWithNumber(waiting, waitingLine.findWaitingNumber(waiting));
    }

    private TimeSlot findTimeSlot(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 시간대를 찾을 수 없습니다."));
    }

    private Theme findTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
    }
}
