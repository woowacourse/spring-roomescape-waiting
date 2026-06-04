package roomescape.repository;

import roomescape.domain.Reservation;
import roomescape.domain.WaitingReservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByIdForUpdate(long id);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    boolean existsConfirmedByThemeSlotId(long themeSlotId);

    List<Reservation> findByName(String name);

    List<WaitingReservation> findWaitingReservationsWithOrderByName(String name);

    void updateStatus(Reservation reservation);

    void updateThemeSlot(Reservation reservation);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeSlotIdAndMemberName(String name, Long themeSlotId);

    Optional<Reservation> findFirstPendingByThemeSlotId(Long themeSlotId);
}
