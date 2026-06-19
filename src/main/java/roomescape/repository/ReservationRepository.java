package roomescape.repository;

import roomescape.domain.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByOrderId(String orderId);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean existsByThemeSlotId(long themeSlotId);

    List<Reservation> findByName(String name);

    List<Reservation> findByThemeSlotAndPending(Long themeSlotId);

    void updateStatus(Reservation reservation);

    void updateThemeSlot(Reservation reservation);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);

    Optional<Reservation> findRecentReservationByThemeSlot(Long themeSlotId);
}
