package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.domain.reservationStatus.ReservationStatus;

public interface JpaReservationRepository extends
        JpaRepository<Reservation, Long>,
        ReservationRepository,
        JpaReservationRepositoryCustom {

    @Override
    default boolean existsConfirmedByThemeSlotId(long themeSlotId) {
        return existsByThemeSlot_IdAndReservationStatus(themeSlotId, ConfirmedStatus.getInstance());
    }

    boolean existsByThemeSlot_IdAndReservationStatus(long themeSlotId, ReservationStatus reservationStatus);

    @Override
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.themeSlot.theme.id = :themeId
            """)
    boolean existsByThemeId(@Param("themeId") long themeId);

    @Override
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.themeSlot.time.id = :timeId
            """)
    boolean existsByTimeId(@Param("timeId") long timeId);

    @Override
    default boolean existsByThemeSlotIdAndMemberName(String name, Long themeSlotId) {
        return existsByNameAndThemeSlot_IdAndReservationStatusNot(
                name,
                themeSlotId,
                CancelledStatus.getInstance()
        );
    }

    boolean existsByNameAndThemeSlot_IdAndReservationStatusNot(
            String name,
            Long themeSlotId,
            ReservationStatus reservationStatus
    );

    @Override
    default Optional<Reservation> findFirstPendingByThemeSlotId(Long themeSlotId) {
        return findFirstByThemeSlot_IdAndReservationStatusOrderByIdAsc(
                themeSlotId,
                PendingStatus.getInstance()
        );
    }

    Optional<Reservation> findFirstByThemeSlot_IdAndReservationStatusOrderByIdAsc(
            Long themeSlotId,
            ReservationStatus reservationStatus
    );
}
