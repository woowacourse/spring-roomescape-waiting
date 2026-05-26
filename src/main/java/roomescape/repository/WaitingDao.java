package roomescape.repository;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Repository
public class WaitingDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<Waiting> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.create(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.create(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return Waiting.create(
                rs.getLong("waiting_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                reservationTime,
                theme,
                rs.getObject("created_at", LocalDateTime.class)
        );
    };

    public WaitingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    public Waiting save(String name, LocalDate date, long timeId, long themeId, LocalDateTime createAt) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("time_id", timeId)
                .addValue("theme_id", themeId)
                .addValue("created_at", createAt);

        Number waitingId = insertExecutor.executeAndReturnKey(params);

        return findById(waitingId.longValue());
    }

    public void delete(long waitingId) {
        String sql = """
                DELETE FROM waiting WHERE id = ?
                """;
        int affected = jdbcTemplate.update(sql, waitingId);

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
                FROM waiting as waiting
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
}
