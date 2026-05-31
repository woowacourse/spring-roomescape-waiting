package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.exception.*;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Waiting saveWaiting(WaitingRequest request) {
        Reservation reservation = findReservationOrThrow(request.date(), request.timeId(), request.themeId());
        validNotReservedBySelf(reservation, request.name());
        Waiting waiting = Waiting.transientOf(request.name(), request.date(), reservation.getTimeSlot(), reservation.getTheme());

        validDuplicated(waiting);
        validDateTime(waiting.getDate(), waiting.getTimeSlot().getStartAt());

        return waitingRepository.save(waiting);
    }

    @Transactional
    public void removeWaiting(Long id, String userName) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));
        waiting.validateModifiable(userName);
        waitingRepository.deleteById(id);
    }

    private Reservation findReservationOrThrow(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(InvalidWaitingPrerequisiteException::new);
    }

    private void validNotReservedBySelf(Reservation reservation, String userName) {
        if (reservation.getName().equals(userName)) {
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

    private void validDateTime(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new PastTimeException("지난 날짜로 예약 대기를 추가하실 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            throw new PastTimeException("지난 시간으로 예약 대기를 추가하실 수 없습니다.");
        }
    }
}
