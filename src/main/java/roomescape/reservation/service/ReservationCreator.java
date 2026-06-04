package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.retry.RetryOnException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.exception.RetryableReservationCreateException;
import roomescape.reservation.service.validator.ReservationValidator;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
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
    public Reservation createReservation(String guestName, LocalDate date, ReservationTime time, Theme theme) {
        Status status = determineState(date, time.getId(), theme.getId());
        Reservation reservation = Reservation.create(
                guestName, date, time, theme, status, LocalDateTime.now(clock));
        reservationValidator.validateCreate(reservation);

        return reservationRepository.save(reservation);
    }

    private Status determineState(LocalDate date, Long timeId, Long themeId) {
        if (!reservationRepository.existsBySlotAndStatusConfirmed(date, timeId, themeId)) {
            return CONFIRMED;
        }
        return Status.WAITING;
    }
}
