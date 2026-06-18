package roomescape.repository.history;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
@Primary
public class JpaMyHistoryRepositoryAdapter implements MyHistoryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<MyHistory> findByUserName(final String name) {
        String sql = """
                SELECT 'RESERVATION' AS status,
                       r.id AS reservation_id,
                       NULL AS waiting_id,
                       r.name AS history_name,
                       s.date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url,
                       rt.id AS time_id,
                       rt.start_at,
                       CAST(NULL AS TIMESTAMP) AS requested_at
                FROM reservation r
                INNER JOIN reservation_slot s ON r.slot_id = s.id
                INNER JOIN theme t ON s.theme_id = t.id
                INNER JOIN reservation_time rt ON s.time_id = rt.id
                WHERE r.name = :name

                UNION ALL

                SELECT 'WAITING' AS status,
                       r.id AS reservation_id,
                       rw.id AS waiting_id,
                       rw.name AS history_name,
                       s.date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url,
                       rt.id AS time_id,
                       rt.start_at,
                       rw.requested_at
                FROM reservation_waiting rw
                INNER JOIN reservation_slot s ON rw.slot_id = s.id
                INNER JOIN reservation r ON r.slot_id = s.id
                INNER JOIN theme t ON s.theme_id = t.id
                INNER JOIN reservation_time rt ON s.time_id = rt.id
                WHERE rw.name = :name
                ORDER BY date, start_at, status
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("name", name)
                .getResultList();

        return rows.stream()
                .map(this::toMyHistory)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyWaitingOrder> findWaitingOrdersByReservationIds(final List<Long> reservationIds) {
        if (reservationIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", Collections.nCopies(reservationIds.size(), "?"));
        String sql = """
                SELECT r.id AS reservation_id,
                       rw.slot_id,
                       rw.id AS waiting_id,
                       rw.requested_at
                FROM reservation_waiting rw
                INNER JOIN reservation r ON rw.slot_id = r.slot_id
                WHERE r.id IN (%s)
                """.formatted(placeholders);

        var query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < reservationIds.size(); i++) {
            query.setParameter(i + 1, reservationIds.get(i));
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(this::toMyWaitingOrder)
                .toList();
    }

    private MyHistory toMyHistory(final Object[] row) {
        return new MyHistory(
                toLong(row[1]),
                toNullableLong(row[2]),
                (String) row[0],
                (String) row[3],
                toLocalDate(row[4]),
                Theme.of(
                        toLong(row[5]),
                        (String) row[6],
                        (String) row[7],
                        (String) row[8]
                ),
                ReservationTime.of(
                        toLong(row[9]),
                        toLocalTime(row[10])
                ),
                toNullableLocalDateTime(row[11])
        );
    }

    private MyWaitingOrder toMyWaitingOrder(final Object[] row) {
        return new MyWaitingOrder(
                toLong(row[0]),
                toLong(row[1]),
                toLong(row[2]),
                toLocalDateTime(row[3])
        );
    }

    private Long toLong(final Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("지원하지 않는 숫자 타입입니다.");
    }

    private Long toNullableLong(final Object value) {
        if (value == null) {
            return null;
        }
        return toLong(value);
    }

    private LocalDate toLocalDate(final Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        throw new IllegalArgumentException("지원하지 않는 날짜 타입입니다.");
    }

    private java.time.LocalTime toLocalTime(final Object value) {
        if (value instanceof java.time.LocalTime localTime) {
            return localTime;
        }
        if (value instanceof Time time) {
            return time.toLocalTime();
        }
        throw new IllegalArgumentException("지원하지 않는 시간 타입입니다.");
    }

    private LocalDateTime toLocalDateTime(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new IllegalArgumentException("지원하지 않는 날짜시간 타입입니다.");
    }

    private LocalDateTime toNullableLocalDateTime(final Object value) {
        return toLocalDateTime(value);
    }
}
