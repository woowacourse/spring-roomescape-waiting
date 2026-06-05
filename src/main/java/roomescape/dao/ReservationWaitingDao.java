package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.WaitingWithRank;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationWaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<ReservationWaiting> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.from(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.from(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return ReservationWaiting.from(
                rs.getLong("waiting_id"),
                UserName.from(rs.getString("name")),
                Slot.from(
                        Schedule.from(
                                rs.getObject("date", LocalDate.class),
                                reservationTime
                        ),
                        theme
                ),
                rs.getObject("created_at", LocalDateTime.class)
        );
    };

    private final RowMapper<WaitingWithRank> withRankRowMapper = (rs, rowNum) -> {
        Theme theme = Theme.from(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.from(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return new WaitingWithRank(
                rs.getLong("waiting_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                reservationTime,
                theme,
                rs.getInt("waiting_rank")
        );
    };

    public ReservationWaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Long create(ReservationWaiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.getUserNameValue())
                .addValue("date", waiting.getWaitingDate())
                .addValue("time_id", waiting.getWaitingTime().getId())
                .addValue("theme_id", waiting.getWaitingTheme().getId())
                .addValue("created_at", waiting.createAt());

        Number waitingId = insertExecutor.executeAndReturnKey(params);

        return waitingId.longValue();
    }

    public Optional<ReservationWaiting> findById(Long waitingId) {
        String sql = """
                SELECT
                    waiting.id as waiting_id,
                    waiting.name,
                    waiting.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description,
                    waiting.created_at as created_at
                FROM reservation_waiting as waiting
                INNER JOIN reservation_time as time
                ON waiting.time_id = time.id
                INNER JOIN theme as theme
                ON waiting.theme_id = theme.id
                WHERE waiting.id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, waitingId)
                .stream()
                .findFirst();
    }

    public boolean existsBySlotAndName(Slot slot, UserName name) {
        String sql = """
            SELECT COUNT(1)
            FROM reservation_waiting
            WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId(),
                name.getName()
        );

        return count != null && count > 0;
    }

    public List<WaitingWithRank> findAllByName(UserName name) {
        String sql = """
                SELECT
                    ranked_waiting.id AS waiting_id,
                    ranked_waiting.name,
                    ranked_waiting.date,
                    time.id AS time_id,
                    time.start_at AS time_value,
                    theme.id AS theme_id,
                    theme.name AS theme_name,
                    theme.thumbnail_url AS thumbnail_url,
                    theme.description AS theme_description,
                    ranked_waiting.waiting_rank
                FROM (
                    SELECT *,
                           ROW_NUMBER() OVER (PARTITION BY date, time_id, theme_id ORDER BY created_at ASC) AS waiting_rank
                    FROM reservation_waiting
                ) AS ranked_waiting
                INNER JOIN reservation_time AS time ON ranked_waiting.time_id = time.id
                INNER JOIN theme ON ranked_waiting.theme_id = theme.id
                WHERE ranked_waiting.name = ?
                """;
        return jdbcTemplate.query(sql, withRankRowMapper, name.getName());
    }

    public List<ReservationWaiting> findAllBySlot(Slot slot) {
        String sql = """
                SELECT
                    waiting.id as waiting_id,
                    waiting.name,
                    waiting.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description,
                    waiting.created_at as created_at
                FROM reservation_waiting as waiting
                INNER JOIN reservation_time as time ON waiting.time_id = time.id
                INNER JOIN theme as theme ON waiting.theme_id = theme.id
                WHERE waiting.date = ? AND waiting.time_id = ? AND waiting.theme_id = ?
                ORDER BY waiting.created_at ASC 
                """;

        return jdbcTemplate.query(
                sql,
                rowMapper,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId()
        );
    }

    public void delete(ReservationWaiting waiting) {
        String sql = """
                DELETE FROM reservation_waiting WHERE id = ?
                """;
        jdbcTemplate.update(sql, waiting.getId());
    }
}
