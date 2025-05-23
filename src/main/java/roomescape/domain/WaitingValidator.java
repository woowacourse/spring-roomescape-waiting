package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.UnAvailableReservationException;

@Component
public class WaitingValidator {

    private final Clock clock;

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingValidator(Clock clock, ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.clock = clock;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public void validateCanWaiting(Waiting waiting) {
        validateNotPast(waiting);
        validateAlreadyWaiting(waiting);
    }

    private void validateNotPast(Waiting waiting) {
        if (waiting.isPast(clock)) {
            throw new UnAvailableReservationException("지난 날짜와 시간에 대한 대기는 불가능합니다.");
        }
    }

    private void validateAlreadyWaiting(Waiting waiting) {
        boolean existsBooking = hasAlreadyBooked(
                waiting.getMember().getId(),
                waiting.getTheme().getId(),
                waiting.getTime().getId(),
                waiting.getDate());
        if (existsBooking) {
            throw new UnAvailableReservationException("이미 동일한 시간에 대기가 존재합니다.");
        }
    }

    private boolean hasAlreadyBooked(Long memberId, Long themeId, Long timeId, LocalDate date) {
        boolean hasReservation = reservationRepository.hasAlreadyReserved(memberId, themeId, timeId, date);
        boolean hasWaiting = waitingRepository.hasAlreadyWaited(memberId, themeId, timeId, date);

        return hasReservation || hasWaiting;
    }
}
