package roomescape.reservationwaiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationwaiting.ReservationWaiting;
import roomescape.reservationwaiting.repository.JpaReservationWaitingRepository;
import roomescape.theme.Theme;

@Service
public class ReservationWaitingService {
    private final JpaReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(
            final JpaReservationWaitingRepository jpaReservationWaitingRepository
    ) {
        this.reservationWaitingRepository = jpaReservationWaitingRepository;
    }

    public ReservationWaiting save(String name, LocalDate date, Theme theme, ReservationTime time) {

        if (reservationWaitingRepository.existsByDateAndThemeIdAndTimeIdAndName(date, theme.getId(), time.getId(), name)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(date, theme, time, name, LocalDateTime.now());
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }

    @Transactional
    public void deleteById(Long id) {

        if (!reservationWaitingRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "이미 대기가 삭제되었습니다."
            );
        }

        reservationWaitingRepository.deleteById(id);

    }

    @Transactional
    public void deleteByIdAndName(Long waitingId, String name) {

        if (!reservationWaitingRepository.existsByIdAndName(waitingId, name)) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "본인의 대기만 취소할 수 있습니다.");
        }

        reservationWaitingRepository.deleteByIdAndName(waitingId, name);

    }

    public Optional<ReservationWaiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId) {
        return reservationWaitingRepository.findFirstByDateAndThemeIdAndTimeIdOrderByRequestAtAsc(date, themeId, timeId);
    }
}
