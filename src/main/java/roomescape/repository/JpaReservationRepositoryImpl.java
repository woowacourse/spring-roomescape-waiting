package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.ThemeSlot;
import roomescape.domain.WaitingReservation;

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
    public List<WaitingReservation> findWaitingReservationsWithOrderByName(String name) {
        String sql = """
                SELECT
                    r_id,
                    name,
                    status,
                    date,
                    t_id,
                    start_at,
                    theme_id,
                    theme_name,
                    theme_description,
                    theme_thumbnail_url,
                    waiting_order
                FROM (
                    SELECT
                        r.id AS r_id,
                        r.name,
                        r.status,
                        ts.date,
                        t.id AS t_id,
                        t.start_at,
                        theme.id AS theme_id,
                        theme.name AS theme_name,
                        theme.description AS theme_description,
                        theme.thumbnail_url AS theme_thumbnail_url,
                        ROW_NUMBER() OVER (PARTITION BY ts.id ORDER BY r.id ASC) AS waiting_order
                    FROM
                        reservation r
                            INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
                            INNER JOIN time t ON ts.time_id = t.id
                            INNER JOIN theme theme ON ts.theme_id = theme.id
                    WHERE r.status = 'PENDING'
                ) waiting_reservation
                WHERE name = :name
                ORDER BY r_id
                """;

        return entityManager.createNativeQuery(sql)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .map(this::toWaitingReservation)
                .toList();
    }

    private WaitingReservation toWaitingReservation(Object row) {
        Object[] values = (Object[]) row;
        return new WaitingReservation(
                toLong(values[0]),
                (String) values[1],
                toLocalDate(values[3]),
                new roomescape.domain.Time(toLong(values[4]), toLocalTime(values[5])),
                new Theme(
                        toLong(values[6]),
                        (String) values[7],
                        (String) values[8],
                        (String) values[9]
                ),
                (String) values[2],
                ((Number) values[10]).intValue()
        );
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

    @Override
    public Optional<Reservation> findFirstPendingByThemeSlotIdForUpdate(Long themeSlotId) {
        List<?> ids = entityManager.createNativeQuery("""
                        SELECT r.id
                        FROM reservation r
                        WHERE r.theme_slot_id = :themeSlotId
                        AND r.status = :status
                        ORDER BY r.id
                        LIMIT 1
                        FOR UPDATE
                        """)
                .setParameter("themeSlotId", themeSlotId)
                .setParameter("status", "PENDING")
                .getResultList();
        return ids.stream()
                .findFirst()
                .map(this::toLong)
                .map(id -> entityManager.find(Reservation.class, id));
    }

    private Long toLong(Object value) {
        return ((Number) value).longValue();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return ((Date) value).toLocalDate();
    }

    private LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        return ((Time) value).toLocalTime();
    }
}
