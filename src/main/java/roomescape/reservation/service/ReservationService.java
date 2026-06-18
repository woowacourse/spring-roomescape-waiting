package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations() {
        reservationRepository.findById(1L).get().getTime().getStartAt();
        return null;
//        return reservationRepository.findAll();
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
    public Optional<Reservation> findBySlot(final LocalDate date, final long timeId, final long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(ReservationNotFoundException::new);
    }

    @Transactional
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
        return reservationRepository.save(reservation);
    }

    @Transactional
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
        reservationRepository.save(promotedReservation);
    }

    @Transactional
    public Reservation updateByCustomer(final Long reservationId, final LocalDate date, final ReservationTime time) {
        final Reservation originReservation = getReservation(reservationId);
        originReservation.validateModifiableByCustomer(LocalDate.now(clock));

        return updateSchedule(date, time, originReservation);
    }

    @Transactional
    public Reservation updateByAdmin(final Long reservationId, final LocalDate date, final ReservationTime time) {
        final Reservation originReservation = getReservation(reservationId);

        return updateSchedule(date, time, originReservation);
    }

    @Transactional
    public void cancel(final Long reservationId) {
        final Reservation reservation = getReservation(reservationId);
        cancel(reservation);
    }

    @Transactional
    public void cancel(final Reservation reservation) {
        reservation.validateCancelableByCustomer(LocalDate.now(clock));
        deleteReservation(reservation.getId());
    }

    @Transactional
    public void deleteById(final Long reservationId) {
        deleteReservation(reservationId);
    }

    private Reservation updateSchedule(
        final LocalDate date,
        final ReservationTime time,
        final Reservation originReservation
    ) {
        final Reservation updatedReservation = originReservation.changeSchedule(
            date,
            time,
            LocalDateTime.now(clock)
        );
        return updateReservation(updatedReservation);
    }

    private Reservation updateReservation(final Reservation reservation) {
        final boolean updated = reservationRepository.update(reservation);
        if (!updated) {
            throw new ReservationNotFoundException();
        }
        return reservation;
    }

    private void deleteReservation(final Long reservationId) {
        final boolean deleted = reservationRepository.deleteById(reservationId);
        if (!deleted) {
            throw new ReservationNotFoundException();
        }
    }
}
