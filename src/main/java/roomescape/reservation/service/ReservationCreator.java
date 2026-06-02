package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.retry.RetryOnException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.exception.RetryableReservationCreateException;
import roomescape.reservation.service.validator.ReservationValidator;

import java.time.Clock;
import java.time.LocalDateTime;

import static roomescape.reservation.domain.Status.CONFIRMED;

@Service
@RequiredArgsConstructor
public class ReservationCreator {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final Clock clock;

    @Transactional
    @RetryOnException(retryOn = RetryableReservationCreateException.class)
    public Reservation createReservation(String guestName, ReservationSlot reservationSlot) {
        Status status = determineState(reservationSlot);
        Reservation reservation = Reservation.create(
                guestName, reservationSlot, status, LocalDateTime.now(clock));
        reservationValidator.validateCreate(reservation);

        return reservationRepository.save(reservation);
    }

    private Status determineState(ReservationSlot reservationSlot) {
        if (!reservationRepository.existsBySlotAndStatusConfirmed(reservationSlot)) {
            return CONFIRMED;
        }
        return Status.WAITING;
    }
}
