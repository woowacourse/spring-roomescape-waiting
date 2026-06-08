package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByCustomerNameAndAfterNow(final String customerName) {
        return reservationRepository.findAllByCustomerNameAndReservationDateTimeAfter(
            new CustomerName(customerName),
            LocalDateTime.now(clock)
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationTimesWithStatus> findReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, themeId);
    }

    @Transactional(readOnly = true)
    public boolean existsBySlot(final LocalDate date, final long reservationTimeId, final long themeId) {
        return reservationRepository.existsBySlot(date, reservationTimeId, themeId);
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(ReservationNotFoundException::new);
    }

    public Reservation create(
        final String customerName,
        final LocalDate reservationDate,
        final ReservationTime time,
        final Theme theme
    ) {
        final Reservation reservation = Reservation.create(
            customerName,
            reservationDate,
            time,
            theme,
            LocalDateTime.now(clock)
        );
        return saveReservation(reservation);
    }

    public void promote(
        final CustomerName customerName,
        final LocalDate reservationDate,
        final ReservationTime time,
        final Theme theme
    ) {
        final Reservation promotedReservation = Reservation.promote(
            customerName,
            reservationDate,
            time,
            theme
        );
        saveReservation(promotedReservation);
    }

    public Reservation updateByCustomer(final Long reservationId, final LocalDate date, final ReservationTime time) {
        final Reservation originReservation = getReservation(reservationId);
        originReservation.validateModifiableByCustomer(LocalDate.now(clock));

        return updateSchedule(date, time, originReservation);
    }

    public Reservation updateByAdmin(final Long reservationId, final LocalDate date, final ReservationTime time) {
        final Reservation originReservation = getReservation(reservationId);

        return updateSchedule(date, time, originReservation);
    }

    public void cancel(final Long reservationId) {
        final Reservation reservation = getReservation(reservationId);
        reservation.validateCancelableByCustomer(LocalDate.now(clock));

        deleteReservation(reservation.getId());
    }

    public void deleteById(final Long reservationId) {
        deleteReservation(reservationId);
    }

    private Reservation updateSchedule(final LocalDate date, final ReservationTime time,
                                       final Reservation originReservation) {
        final Reservation updatedReservation = originReservation.changeSchedule(
            date,
            time,
            LocalDateTime.now(clock)
        );

        return updateReservation(updatedReservation);
    }

    private Reservation saveReservation(final Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException exception) {
            throw new ReservationAlreadyExistsException(exception);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationOptionChangedException(exception);
        }
    }

    private Reservation updateReservation(final Reservation reservation) {
        try {
            final boolean updated = reservationRepository.update(reservation);

            if (!updated) {
                throw new ReservationNotFoundException();
            }
            return reservation;
        } catch (DuplicateKeyException exception) {
            throw new ReservationAlreadyExistsException(exception);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationOptionChangedException(exception);
        }
    }

    private void deleteReservation(final Long reservationId) {
        final boolean deleted = reservationRepository.deleteById(reservationId);

        if (!deleted) {
            throw new ReservationNotFoundException();
        }
    }
}
