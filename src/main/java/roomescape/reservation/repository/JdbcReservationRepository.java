package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNumber) -> Reservation.load(
        resultSet.getLong("reservation_id"),
        resultSet.getString("name"),
        ReservationDate.load(
            resultSet.getLong("date_id"),
            resultSet.getDate("date").toLocalDate(),
            resultSet.getBoolean("date_is_active")
        ),
        ReservationTime.load(
            resultSet.getLong("time_id"),
            resultSet.getTime("start_at").toLocalTime(),
            resultSet.getBoolean("time_is_active")
        ),
        Theme.load(
            resultSet.getLong("theme_id"),
            resultSet.getString("theme_name"),
            resultSet.getString("description"),
            resultSet.getString("thumbnail_url"),
            resultSet.getBoolean("is_active")
        ),
        ReservationStatus.valueOf(resultSet.getString("status")),
        resultSet.getLong("waiting_order")
    );

    public JdbcReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
            .withTableName("reservation")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name,
                r.status,
                r.waiting_order,
                d.id as date_id,
                d.date,
                d.is_active as date_is_active,
                t.id as time_id,
                t.start_at,
                t.is_active as time_is_active,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description,
                th.thumbnail_url,
                th.is_active
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name,
                r.status,
                r.waiting_order,
                d.id as date_id,
                d.date,
                d.is_active as date_is_active,
                t.id as time_id,
                t.start_at,
                t.is_active as time_is_active,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description,
                th.thumbnail_url,
                th.is_active
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            WHERE r.id = :id
            """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(sql, params, reservationRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Reservation> findFirstWaitingByDateTimeAndThemeId(Long dateId, Long timeId,
        Long themeId) {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name,
                r.status,
                r.waiting_order,
                d.id as date_id,
                d.date,
                d.is_active as date_is_active,
                t.id as time_id,
                t.start_at,
                t.is_active as time_is_active,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description,
                th.thumbnail_url,
                th.is_active
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            WHERE r.status = 'WAITING'
            AND date_id = :dateId
            AND time_id = :timeId
            AND theme_id = :themeId
            AND (
                d.date > CURRENT_DATE 
                OR (d.date = CURRENT_DATE AND t.start_at > CURRENT_TIME)
            )
            ORDER BY r.waiting_order ASC
            LIMIT 1
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, params,
                reservationRowMapper);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllActiveByDateTimeAndThemeId(Long dateId, Long timeId,
        Long themeId) {
        String sql = """
            SELECT
                r.id AS reservation_id,
                r.name,
                r.status,
                r.waiting_order,
                d.id AS date_id,
                d.date,
                d.is_active as date_is_active,
                t.id AS time_id,
                t.start_at,
                t.is_active as time_is_active,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description,
                th.thumbnail_url,
                th.is_active
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            WHERE r.date_id = :dateId
            AND r.time_id = :timeId
            AND r.theme_id = :themeId
            AND r.status in ('RESERVED','WAITING')
            ORDER BY d.date DESC , t.start_at ASC
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);

        return jdbcTemplate.query(sql, params, reservationRowMapper);
    }

    @Override
    public Long findNextWaitingOrderBySlot(Long dateId, Long timeId, Long themeId) {
        String sql = """
            SELECT coalesce(max(r.waiting_order), 0) + 1
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            WHERE r.date_id = :dateId
            AND r.time_id = :timeId
            AND r.theme_id = :themeId
            AND r.status in ('WAITING')
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("dateId", dateId)
            .addValue("timeId", timeId)
            .addValue("themeId", themeId);

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", reservation.getName())
            .addValue("date_id", reservation.getDate().getId())
            .addValue("time_id", reservation.getTime().getId())
            .addValue("theme_id", reservation.getTheme().getId())
            .addValue("status", reservation.getStatus().name())
            .addValue("waiting_order", reservation.getWaitingOrder());
        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Reservation.load(
            savedId,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getStatus(),
            reservation.getWaitingOrder()
        );
    }

    @Override
    public boolean updateStatusAndWaitingOrder(Reservation reservation) {
        String sql = """
            UPDATE reservation
            SET status = :status,
                waiting_order = :waitingOrder
            WHERE id = :id
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", reservation.getId())
            .addValue("status", reservation.getStatus().name())
            .addValue("waitingOrder", reservation.getWaitingOrder());
        int updatedCount = jdbcTemplate.update(sql, params);
        return updatedCount > 0;
    }

    @Override
    public boolean updateScheduleAndStatus(Reservation reservation) {
        String sql = """
            UPDATE reservation
            SET date_id = :dateId,
                time_id = :timeId,
                status = :status,
                waiting_order = :waitingOrder
            WHERE id = :id
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("dateId", reservation.getDate().getId())
            .addValue("timeId", reservation.getTime().getId())
            .addValue("status", reservation.getStatus().name())
            .addValue("waitingOrder", reservation.getWaitingOrder())
            .addValue("id", reservation.getId());

        int updatedCount = jdbcTemplate.update(sql, params);
        return updatedCount > 0;
    }

    public List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName) {
        String sql = """
            SELECT
                r.id,
                r.name,
                d.date,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.thumbnail_url AS theme_thumbnail_url,
                r.status,
                CASE 
                    WHEN r.status = 'WAITING'
                        AND(
                            d.date > CURRENT_DATE
                            OR (d.date = CURRENT_DATE AND t.start_at > CURRENT_TIME)
                        )
                    THEN (
                    SELECT COUNT(*) + 1 
                    FROM reservation wait
                    WHERE wait.date_id = r.date_id
                        AND wait.time_id = r.time_id
                        AND wait.theme_id = r.theme_id
                        AND wait.status = 'WAITING'
                        AND wait.waiting_order < r.waiting_order
                    )
                    ELSE NULL
                END AS waiting_turn
            FROM reservation r
            INNER JOIN reservation_date d ON r.date_id = d.id
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            WHERE r.name = :memberName
            ORDER BY r.id DESC
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("memberName", memberName);

        return jdbcTemplate.query(sql, params, reservationWithWaitingTurnRowMapper);
    }

    private final RowMapper<ReservationWithWaitingTurn> reservationWithWaitingTurnRowMapper =
        (rs, rowNum) -> new ReservationWithWaitingTurn(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getObject("date", LocalDate.class),
            rs.getObject("start_at", LocalTime.class),
            rs.getLong("theme_id"),
            rs.getString("theme_name"),
            rs.getString("theme_thumbnail_url"),
            ReservationStatus.valueOf(rs.getString("status")),
            rs.getObject("waiting_turn", Long.class)
        );

}
