package roomescape.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWaitingDto;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                    r.last_modified_at AS last_modified_at,
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.id = :id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), reservationRowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<ReservationWaitingDto> findWaitingById(Long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM (
                            SELECT
                                r.id AS reservation_id,
                                r.guest_name,
                                r.date,
                                r.status AS status,
                                r.last_modified_at AS last_modified_at,

                                t.id AS time_id,
                                t.start_at,
                                t.deleted_at AS time_deleted_at,

                                th.id AS theme_id,
                                th.name AS theme_name,
                                th.description AS theme_description,
                                th.thumbnail AS theme_thumbnail,
                                th.deleted_at AS theme_deleted_at,

                                ROW_NUMBER() OVER (
                                    PARTITION BY r.date, t.id, th.id, r.status
                                    ORDER BY r.last_modified_at
                                ) AS wait_number

                            FROM reservation r
                            INNER JOIN reservation_time t
                                ON r.time_id = t.id
                            INNER JOIN theme th
                                ON r.theme_id = th.id
                        ) x
                        WHERE x.reservation_id = :id
                        """,
                new MapSqlParameterSource("id", id), reservationWaitingDtoRowMapper
        ).stream().findFirst();
    }

    @Override
    public List<Reservation> findAllByStatusCanceledNot(int page, int size) {
        return jdbcTemplate.query("""
                SELECT
                    r.id AS reservation_id,
                    r.guest_name,
                    r.date,
                    r.status AS status,
                    r.last_modified_at AS last_modified_at,
                    
                    t.id AS time_id,
                    t.start_at,
                    t.deleted_at AS time_deleted_at,
                    
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    th.deleted_at AS theme_deleted_at
                FROM reservation r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme th
                    ON r.theme_id = th.id
                WHERE r.status != 'CANCELED'
                ORDER BY r.date, t.start_at
                LIMIT :size OFFSET :offset
                """,
                new MapSqlParameterSource()
                        .addValue("size", size)
                        .addValue("offset", (page - 1) * size),
                reservationRowMapper);
    }

    @Override
    public List<ReservationWaitingDto> findWaitingAllByGuestName(String guestName) {
        return jdbcTemplate.query("""
                SELECT *
                FROM (
                    SELECT
                        r.id AS reservation_id,
                        r.guest_name,
                        r.date,
                        r.status AS status,
                        r.last_modified_at AS last_modified_at,

                        t.id AS time_id,
                        t.start_at,
                        t.deleted_at AS time_deleted_at,

                        th.id AS theme_id,
                        th.name AS theme_name,
                        th.description AS theme_description,
                        th.thumbnail AS theme_thumbnail,
                        th.deleted_at AS theme_deleted_at,

                        ROW_NUMBER() OVER (
                            PARTITION BY r.date, t.id, th.id, r.status
                            ORDER BY r.last_modified_at
                        ) AS wait_number
                    FROM reservation r
                    INNER JOIN reservation_time t
                        ON r.time_id = t.id
                    INNER JOIN theme th
                        ON r.theme_id = th.id
                ) x
                WHERE x.guest_name = :guestName
                """,
                new MapSqlParameterSource("guestName", guestName),
                reservationWaitingDtoRowMapper);

    }

    @Override
    public Optional<Reservation> findBySlotAndStatusWaitingAndWaitingNumberIsOne(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM (
                            SELECT
                                r.id AS reservation_id,
                                r.guest_name,
                                r.date,
                                r.status AS status,
                                r.last_modified_at AS last_modified_at,
                                        
                                t.id AS time_id,
                                t.start_at,
                                t.deleted_at AS time_deleted_at,
                                        
                                th.id AS theme_id,
                                th.name AS theme_name,
                                th.description AS theme_description,
                                th.thumbnail AS theme_thumbnail,
                                th.deleted_at AS theme_deleted_at,
                                        
                                ROW_NUMBER() OVER (
                                    PARTITION BY r.date, t.id, th.id, r.status
                                    ORDER BY r.last_modified_at
                                ) AS wait_number
                            FROM reservation r
                            INNER JOIN reservation_time t
                                ON r.time_id = t.id
                            INNER JOIN theme th
                                ON r.theme_id = th.id
                        ) x
                        WHERE date = :date
                          AND time_id = :timeId
                          AND theme_id = :themeId
                          AND status = 'WAITING'
                          AND wait_number = 1
                        """,
                        new MapSqlParameterSource()
                                .addValue("date", Date.valueOf(date))
                                .addValue("timeId", timeId)
                                .addValue("themeId", themeId),
                        reservationRowMapper)
                .stream().findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update("""
                        INSERT INTO reservation (guest_name, date, time_id, theme_id, status, last_modified_at)
                        VALUES (:guestName, :date, :timeId, :themeId, :status, :lastModifiedAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("guestName", reservation.getGuestName())
                        .addValue("date", Date.valueOf(reservation.getDate()))
                        .addValue("timeId", reservation.getTime().getId())
                        .addValue("themeId", reservation.getTheme().getId())
                        .addValue("status", reservation.getStatus().toString())
                        .addValue("lastModifiedAt", Timestamp.valueOf(reservation.getLastModifiedAt())),
                keyHolder,
                new String[]{"id"});

        return reservation.withId(keyHolder.getKey().longValue());
    }

    @Override
    public boolean updateDateAndTimeAndStatus(
            Long id, LocalDate date, Long timeId, Status status, LocalDateTime lastModifiedAt) {
        String sql = """
                UPDATE reservation
                SET date = :date, time_id = :timeId, status = :status, last_modified_at = :lastModifiedAt
                WHERE id = :id
                """;

        int count = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("date", Date.valueOf(date))
                .addValue("timeId", timeId)
                .addValue("status", status.toString())
                .addValue("lastModifiedAt", Timestamp.valueOf(lastModifiedAt))
                .addValue("id", id));
        return count == 1;
    }

    @Override
    public boolean updateStatus(Long id, Status status) {
        String sql = "UPDATE reservation SET status = :status WHERE id = :id";
        int count = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("status", status.toString())
                .addValue("id", id));
        return count == 1;
    }

    @Override
    public boolean cancelById(Long id) {
        int rowCount = jdbcTemplate.update("""
                UPDATE reservation
                SET cancel_token = :id, status = 'CANCELED'
                WHERE id = :id
                """, new MapSqlParameterSource("id", id));

        return rowCount == 1;
    }

    @Override
    public boolean existsBySlotAndGuestNameExceptCanceled(
            LocalDate date, Long timeId, Long themeId, String guestName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND guest_name = :guestName
                  AND status != 'CANCELED'
                """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", timeId)
                        .addValue("themeId", themeId)
                        .addValue("guestName", guestName),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySlotAndStatusConfirmed(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'CONFIRMED';
                """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", timeId)
                        .addValue("themeId", themeId),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySlotExceptReservation(LocalDate date, Long timeId, Long themeId, Long excludedId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                  AND time_id = :timeId
                  AND theme_id = :themeId
                  AND status = 'CONFIRMED'
                  AND id != :excludedId
                """,
                new MapSqlParameterSource()
                        .addValue("date", Date.valueOf(date))
                        .addValue("timeId", timeId)
                        .addValue("themeId", themeId)
                        .addValue("excludedId", excludedId),
                Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE time_id = :timeId AND status != 'CANCELED'
                """, new MapSqlParameterSource("timeId", timeId), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE theme_id = :themeId AND status != 'CANCELED'
                """, new MapSqlParameterSource("themeId", themeId), Integer.class);
        return count != null && count > 0;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
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

        return Reservation.of(
                resultSet.getLong("reservation_id"),
                resultSet.getString("guest_name"),
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme,
                Status.from(resultSet.getString("status")),
                toLocalDateTime(resultSet.getTimestamp("last_modified_at"))
        );
    };

    private final RowMapper<ReservationWaitingDto> reservationWaitingDtoRowMapper = (resultSet, rowNum) -> {
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

        return ReservationWaitingDto.from(Reservation.of(
                        resultSet.getLong("reservation_id"),
                        resultSet.getString("guest_name"),
                        resultSet.getDate("date").toLocalDate(),
                        reservationTime,
                        theme,
                        Status.from(resultSet.getString("status")),
                        toLocalDateTime(resultSet.getTimestamp("last_modified_at"))
                ),
                resultSet.getLong("wait_number")
        );
    };

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
