package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ThemeSlot;

@Repository
@SuppressWarnings("unchecked")
public class JpaReservationRepositoryImpl implements JpaReservationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Reservation> findByIdForUpdate(long reservationId) {
        List<?> ids = entityManager.createNativeQuery("""
                        SELECT id
                        FROM reservation
                        WHERE id = :reservationId
                        FOR UPDATE
                        """)
                .setParameter("reservationId", reservationId)
                .getResultList();
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(entityManager.find(Reservation.class, reservationId));
    }

    @Override
    public boolean updateStatus(Reservation reservation, String expectedStatus) {
        Reservation managedReservation = entityManager.find(Reservation.class, reservation.getId());
        if (managedReservation == null) {
            return false;
        }
        if (managedReservation.getReservationStatusName().equals(expectedStatus)) {
            managedReservation.changeStatus(reservation.getReservationStatus());
            return true;
        }
        return managedReservation.getReservationStatusName().equals(reservation.getReservationStatusName());
    }

    @Override
    public void updateThemeSlot(Reservation reservation) {
        Reservation managedReservation = entityManager.find(Reservation.class, reservation.getId());
        if (managedReservation == null) {
            return;
        }
        ThemeSlot themeSlot = entityManager.find(ThemeSlot.class, reservation.getThemeSlotId());
        managedReservation.changeThemeSlot(themeSlot);
    }

}
