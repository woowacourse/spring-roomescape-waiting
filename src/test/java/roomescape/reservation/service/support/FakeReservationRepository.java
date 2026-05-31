package roomescape.reservation.service.support;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeReservationRepository implements ReservationRepository  {

    private final List<Reservation> reservations = new ArrayList<>();
    private Reservation savedReservation;
    private RuntimeException saveException;
    private RuntimeException updateException;

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(final Long reservationId) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(reservationId))
                .findFirst();
    }

    @Override
    public Reservation save(final Reservation newReservation) {
        if (saveException != null) {
            throw saveException;
        }

        savedReservation = newReservation;
        Reservation savedReservationWithId = Reservation.of(
                1L,
                newReservation.getCustomerName(),
                newReservation.getSlot()
        );
        reservations.add(savedReservationWithId);
        return savedReservationWithId;
    }

    @Override
    public boolean update(final Reservation updatedReservation) {
        if (updateException != null) {
            throw updateException;
        }

        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId().equals(updatedReservation.getId())) {
                reservations.set(i, updatedReservation);
                return true;
            }
        }

        return false;
    }

    public boolean removeById(final Long reservationId) {
        return reservations.removeIf(reservation -> reservation.getId().equals(reservationId));
    }

    @Override
    public List<ReservationTimesWithStatus> findReservationTimeStatusesByDateAndThemeId(final LocalDate date, final Long themeId) {
        return List.of();
    }

    @Override
    public List<Reservation> findAllByCustomerNameAndReservationDateTimeAfter(final String customerName, final LocalDateTime now) {
        return reservations.stream()
                .filter(reservation -> reservation.getCustomerName().equals(customerName))
                .filter(reservation -> LocalDateTime.of(
                        reservation.getDate(),
                        reservation.getTime().getStartAt()).isAfter(now)
                )
                .toList();
    }

    public Reservation savedReservation() {
        return savedReservation;
    }

    public void add(final Reservation reservation) {
        reservations.add(reservation);
    }

    public void failToSaveByDuplicatedReservation() {
        saveException = new DuplicateKeyException("duplicated reservation");
    }

    public void failToSaveByChangedOption() {
        saveException = new DataIntegrityViolationException("changed reservation option");
    }

    public void failToUpdateByDuplicatedReservation() {
        updateException = new DuplicateKeyException("duplicated reservation");
    }

    public void failToUpdateByChangedOption() {
        updateException = new DataIntegrityViolationException("changed reservation option");
    }
}
