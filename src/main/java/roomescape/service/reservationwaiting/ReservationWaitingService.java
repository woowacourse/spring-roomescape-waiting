package roomescape.service.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAvailabilityPolicy;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationAvailabilityPolicy reservationAvailabilityPolicy;

    public ReservationWaitingService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final ReservationAvailabilityPolicy reservationAvailabilityPolicy
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationAvailabilityPolicy = reservationAvailabilityPolicy;
    }

    public ReservationWaiting save(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        String waitingName = validateName(name);
        Reservation reservation = reservationRepository.findByDateAndThemeIdAndTimeId(date, themeId, timeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));

        validateWaitableName(reservation, waitingName);

        LocalDateTime requestedAt = LocalDateTime.now();
        validateWaitableReservation(reservation, requestedAt);

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(
                reservation,
                waitingName,
                requestedAt
        );
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }

    private String validateName(final String name) {
        try {
            return ReservationName.from(name).value();
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
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

    private void validateWaitableReservation(final Reservation reservation, final LocalDateTime requestedAt) {
        try {
            reservationAvailabilityPolicy.validateWaitable(reservation, requestedAt);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, exception.getMessage());
        }
    }

    public void deleteByIdAndName(final Long waitingId, final String name) {
        String waitingName = validateName(name);
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(waitingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                        "삭제된 대기 데이터가 없습니다."
                ));

        if (!reservationWaiting.hasName(waitingName)) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "삭제된 대기 데이터가 없습니다."
            );
        }

        int affectedRowCount = reservationWaitingRepository.deleteById(waitingId);

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "삭제된 대기 데이터가 없습니다.");
        }
    }
}
