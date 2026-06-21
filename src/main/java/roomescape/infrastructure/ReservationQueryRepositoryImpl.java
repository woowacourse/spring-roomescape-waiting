package roomescape.infrastructure;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import roomescape.domain.repository.ReservationRepository;
import roomescape.dto.ReservationResponse;

@Repository
public class ReservationQueryRepositoryImpl implements ReservationRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ReservationResponse> findByUserName(String username) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT rv.id AS reservation_id,
                       rv.name AS name,
                       rv.status AS status,
                       rs.date AS date,
                       th.name AS theme_name,
                       th.description AS theme_description,
                       th.thumbnail_url AS theme_thumbnail,
                       t.start_at AS time_value,
                       CASE
                           WHEN rv.status = 'RESERVED' THEN 0
                           WHEN rv.status = 'CANCELED' THEN 0
                           ELSE (
                               SELECT COUNT(*)
                               FROM reservation rv2
                               WHERE rv2.reservation_slot_id = rv.reservation_slot_id
                                 AND rv2.status = 'WAITING'
                                 AND (
                                     rv2.updated_at < rv.updated_at
                                     OR (rv2.updated_at = rv.updated_at AND rv2.id < rv.id)
                                 )
                           ) + 1
                       END AS waiting_order
                FROM reservation AS rv
                INNER JOIN reservation_slot AS rs ON rv.reservation_slot_id = rs.id
                INNER JOIN reservation_time AS t ON rs.time_id = t.id
                INNER JOIN theme AS th ON rs.theme_id = th.id
                WHERE rv.name = :username
                """)
                .setParameter("username", username)
                .getResultList();

        return rows.stream()
                .map(row -> new ReservationResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Date) row[3]).toLocalDate(),
                        (String) row[4],
                        (String) row[5],
                        (String) row[6],
                        ((Time) row[7]).toLocalTime(),
                        ((Number) row[8]).intValue()
                ))
                .toList();
    }
}