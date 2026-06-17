package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.WaitingReservation;

public interface JpaReservationRepositoryCustom {

    Optional<Reservation> findByIdForUpdate(long reservationId);

    List<WaitingReservation> findWaitingReservationsWithOrderByName(String name);

    boolean updateStatus(Reservation reservation, String expectedStatus);

    void updateThemeSlot(Reservation reservation);

    Optional<Reservation> findFirstPendingByThemeSlotIdForUpdate(Long themeSlotId);
}
