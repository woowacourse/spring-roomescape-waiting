package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public ReservationSlot upsert(ReservationSlot reservationSlot) {
        return findByDateAndTimeAndTheme(reservationSlot)
                .orElseGet(() -> insertOrFind(reservationSlot));
    }

    private ReservationSlot insertOrFind(ReservationSlot reservationSlot) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO reservation_slot (date, time_id, theme_id)
                    VALUES (:date, :timeId, :themeId)
                    """, slotParameterSource(reservationSlot));
        } catch (DuplicateKeyException ignored) {
        }

        return findByDateAndTimeAndTheme(reservationSlot)
                .orElseThrow();
    }

    private Optional<ReservationSlot> findByDateAndTimeAndTheme(ReservationSlot reservationSlot) {
        return jdbcTemplate.query("""
                        SELECT
                            s.id AS slot_id,
                            s.date,
                            t.id AS time_id,
                            t.start_at,
                            t.deleted_at AS time_deleted_at,
                            th.id AS theme_id,
                            th.name AS theme_name,
                            th.description AS theme_description,
                            th.thumbnail AS theme_thumbnail,
                            th.deleted_at AS theme_deleted_at
                        FROM reservation_slot s
                        INNER JOIN reservation_time t
                            ON s.time_id = t.id
                        INNER JOIN theme th
                            ON s.theme_id = th.id
                        WHERE s.date = :date
                          AND s.time_id = :timeId
                          AND s.theme_id = :themeId
                        """,
                slotParameterSource(reservationSlot),
                reservationSlotRowMapper
        ).stream().findFirst();
    }

    @Override
    public Optional<ReservationSlot> findByIdWithLock(Long id) {
        List<Long> lockedIds = jdbcTemplate.queryForList("""
                SELECT id
                FROM reservation_slot
                WHERE id = :id
                FOR UPDATE
                """, new MapSqlParameterSource("id", id), Long.class);

        if (lockedIds.isEmpty()) {
            return Optional.empty();
        }
        return findById(id);
    }

    private Optional<ReservationSlot> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT
                            s.id AS slot_id,
                            s.date,
                            t.id AS time_id,
                            t.start_at,
                            t.deleted_at AS time_deleted_at,
                            th.id AS theme_id,
                            th.name AS theme_name,
                            th.description AS theme_description,
                            th.thumbnail AS theme_thumbnail,
                            th.deleted_at AS theme_deleted_at
                        FROM reservation_slot s
                        INNER JOIN reservation_time t
                            ON s.time_id = t.id
                        INNER JOIN theme th
                            ON s.theme_id = th.id
                        WHERE s.id = :id
                        """,
                new MapSqlParameterSource("id", id),
                reservationSlotRowMapper
        ).stream().findFirst();
    }

    private final RowMapper<ReservationSlot> reservationSlotRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime(),
                toLocalDateTime(resultSet.getTimestamp("time_deleted_at"))
        );

        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail"),
                toLocalDateTime(resultSet.getTimestamp("theme_deleted_at"))
        );

        return ReservationSlot.of(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    };

    private MapSqlParameterSource slotParameterSource(ReservationSlot reservationSlot) {
        return new MapSqlParameterSource()
                .addValue("date", Date.valueOf(reservationSlot.getDate()))
                .addValue("timeId", reservationSlot.getTimeId())
                .addValue("themeId", reservationSlot.getThemeId());
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

}
