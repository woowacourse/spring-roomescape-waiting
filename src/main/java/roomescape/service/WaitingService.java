package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.*;
import roomescape.repository.ReservationRepository;
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

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          TimeSlotRepository timeSlotRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional
    public void saveWaiting(WaitingRequest request) {
        Waiting waiting = Waiting.transientOf(request.name(), request.date(), request.timeId(), request.themeId());
        validDuplicated(waiting);
        validReservation(waiting);
        validDateTime(waiting.getDate(), waiting.getTimeSlotId());

        waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));
        waiting.validateModifiable(userName);
        waitingRepository.deleteById(id);
    }

    private void validReservation(Waiting waiting) {
        reservationRepository.findByDateAndTimeIdAndThemeId(waiting.getDate(),
                waiting.getTimeSlotId(), waiting.getThemeId()).ifPresent(reservation -> {
            if (reservation.getName().equals(waiting.getName())) {
                throw new DuplicateReservationException(reservation.getDate().toString(),
                        reservation.getTimeSlot().getId(), reservation.getTheme().getId());
            }
        });
    }

    private void validDuplicated(Waiting waiting) {
        boolean isExists = waitingRepository.isExists(waiting);
        if (isExists) {
            throw new DuplicateWaitingException(waiting);
        }
    }

    private void validDateTime(LocalDate date, Long timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(timeSlotId));

        if (date.isBefore(LocalDate.now())) {
            throw new PastTimeException("지난 날짜로 예약 대기를 추가하실 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && timeSlot.getStartAt().isBefore(LocalTime.now())) {
            throw new PastTimeException("지난 시간으로 예약 대기를 추가하실 수 없습니다.");
        }
    }
}
