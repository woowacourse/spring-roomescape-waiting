package roomescape.service.reservationwaiting;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationWaitingService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.clock = clock;
    }

    public ReservationWaiting save(String name, LocalDate date, Long themeId, Long timeId) {
        Long reservationId = findReservationId(date, themeId, timeId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(
                reservation,
                name,
                LocalDateTime.now(clock)
        );

        validateWaitableName(reservation, nonIdReservationWaiting.getName());

        try {
            return reservationWaitingRepository.save(nonIdReservationWaiting);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }
    }

    private void validateWaitableName(final Reservation reservation, final String waitingName) {
        if (reservation.getName().equals(waitingName)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 예약한 사람은 같은 예약에 대기할 수 없습니다."
            );
        }

        if (reservationWaitingRepository.existsByReservationIdAndName(reservation.getId(), waitingName)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }
    }

    private Long findReservationId(final LocalDate date, final Long themeId, final Long timeId) {
        try {
            return reservationRepository.findReservationIdByDateAndThemeIdAndTimeId(date, themeId, timeId);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_NOT_FOUND,
                    "예약 정보가 없으면 대기 생성이 불가능합니다."
            );
        }
    }

    public void deleteByIdAndName(Long waitingId, String name) {
        int affectedRowCount = reservationWaitingRepository.deleteByIdAndName(waitingId, name);

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "삭제된 대기 데이터가 없습니다.");
        }
    }
}
