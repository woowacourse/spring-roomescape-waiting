package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface JpaReservationRepositoryCustom {

    Optional<Reservation> findByIdForUpdate(long reservationId);

    boolean updateStatus(Reservation reservation, String expectedStatus);

    void updateThemeSlot(Reservation reservation);
}
