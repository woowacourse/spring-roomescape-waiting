package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.common.domain.ReservationSlot;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationIdResponse;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByName(String name);

    void update(Long id, ReservationSlot slot);

    boolean isBooked(ReservationSlot slot);

    boolean isReservedBy(ReservationSlot slot, String name);

    boolean isBookedByOther(ReservationSlot slot, Long id);

    void deleteById(Long id);

    ReservationIdResponse findIdBySlot(LocalDate date, Long themeId, Long timeId);

    void updateStatus(Long reservationId, PaymentStatus status);
}