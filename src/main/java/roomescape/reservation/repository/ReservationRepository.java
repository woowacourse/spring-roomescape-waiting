package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long reservationId);

    Reservation save(Reservation newReservation);

    boolean update(Reservation updatedReservation);

    boolean confirm(Long reservationId);

    boolean deletePendingById(Long reservationId);

    boolean deleteByIdAndSlotId(Long reservationId, Long slotId);

    List<ReservationTimesWithStatus> findReservationTimeStatusesByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
            String customerName,
            String customerEmail,
            LocalDateTime now
    );
}
