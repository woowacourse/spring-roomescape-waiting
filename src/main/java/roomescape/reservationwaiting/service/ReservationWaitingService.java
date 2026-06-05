package roomescape.reservationwaiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(
            final ReservationWaitingRepository reservationWaitingRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public ReservationWaiting save(String name, LocalDate date, Long themeId, Long timeId) {

        if (reservationWaitingRepository.existsByDateAndThemeIdAndTimeIdAndName(date, themeId, timeId, name)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(date, themeId, timeId, name, LocalDateTime.now());
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }

    public void deleteById(Long id) {
        reservationWaitingRepository.deleteById(id);
    }

    public void deleteByIdAndName(Long waitingId, String name) {
        int affectedRowCount = reservationWaitingRepository.deleteByIdAndName(waitingId, name);

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "본인의 대기만 취소할 수 있습니다.");
        }
    }

    public Optional<ReservationWaiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId) {
        return reservationWaitingRepository.findFirstWaiting(date, themeId, timeId);
    }
}
