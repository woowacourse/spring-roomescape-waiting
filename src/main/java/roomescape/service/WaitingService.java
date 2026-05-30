package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.*;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          TimeSlotRepository timeSlotRepository, ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public void saveWaiting(WaitingRequest request) {
        TimeSlot timeSlot = findTimeSlot(request.timeId());
        Theme theme = findTheme(request.themeId());
        Waiting waiting = Waiting.transientOf(request.name(), request.date(), timeSlot, theme);

        validDuplicated(waiting);
        validReservation(waiting);
        validDateTime(waiting.getDate(), timeSlot);

        waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));
        waiting.validateModifiable(userName);
        waitingRepository.deleteById(id);
    }

    private TimeSlot findTimeSlot(Long timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(timeSlotId));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException(themeId));
    }

    private void validReservation(Waiting waiting) {
        Reservation reservation = findReservationOrThrow(waiting);
        validateOwnership(reservation, waiting);
    }

    private Reservation findReservationOrThrow(Waiting waiting) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                waiting.getDate(),
                waiting.getTimeSlot().getId(),
                waiting.getTheme().getId()
        ).orElseThrow(InvalidWaitingPrerequisiteException::new);
    }

    private void validateOwnership(Reservation reservation, Waiting waiting) {
        if (reservation.getName().equals(waiting.getName())) {
            throw new DuplicateReservationException(
                    reservation.getDate().toString(),
                    reservation.getTimeSlot().getId(),
                    reservation.getTheme().getId()
            );
        }
    }

    private void validDuplicated(Waiting waiting) {
        if (waitingRepository.isExists(waiting)) {
            throw new DuplicateWaitingException(waiting);
        }
    }

    private void validDateTime(LocalDate date, TimeSlot timeSlot) {
        if (date.isBefore(LocalDate.now())) {
            throw new PastTimeException("지난 날짜로 예약 대기를 추가하실 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && timeSlot.getStartAt().isBefore(LocalTime.now())) {
            throw new PastTimeException("지난 시간으로 예약 대기를 추가하실 수 없습니다.");
        }
    }
}
