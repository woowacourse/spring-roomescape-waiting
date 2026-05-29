package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.WaitingWithRank;
import roomescape.domain.*;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public class WaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<Waiting> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.from(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.create(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return Waiting.from(
                rs.getLong("waiting_id"),
                rs.getString("name"),
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

        ReservationTime reservationTime = ReservationTime.create(
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

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", waiting.name())
                .addValue("date", waiting.waitingDate())
                .addValue("time_id", waiting.waitingTime().id())
                .addValue("theme_id", waiting.waitingTheme().id())
                .addValue("created_at", waiting.createAt());

        Number waitingId = insertExecutor.executeAndReturnKey(params);

        return findById(waitingId.longValue());
    }

    public void delete(Waiting waiting) {
        String sql = """
                DELETE FROM reservation_waiting WHERE id = ?
                """;
        int affected = jdbcTemplate.update(sql, waiting.id());

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약 대기를 찾을 수 없습니다.");
        }
    }

    public Waiting findById(long waitingId) {
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
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("요청한 예약 대기를 찾을 수 없습니다."));
    }

    public List<Waiting> findAllBySlot(Slot slot) {
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
                WHERE waiting.date = ? AND time_id = ? AND theme_id = ?
                ORDER BY created_at;
                """;
        return jdbcTemplate.query(sql, rowMapper, slot.date(), slot.time().id(), slot.theme().id());
    }

    public List<WaitingWithRank> findAllByName(String name) {
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
        return jdbcTemplate.query(sql, withRankRowMapper, name);
    }
}
