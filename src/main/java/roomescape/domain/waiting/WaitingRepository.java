package roomescape.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.dto.TimeSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.dto.MyWaitingResult;

@Repository
public class WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Waiting> waitingRowMapper = (resultSet, rowNum) -> Waiting.of(
        resultSet.getLong("waiting_id"),
        resultSet.getString("name"),
        resultSet.getDate("date").toLocalDate(),
        ReservationTime.of(
            resultSet.getLong("time_id"),
            resultSet.getTime("time_start_at").toLocalTime(),
            resultSet.getTime("time_finish_at").toLocalTime()
        ),
        Theme.of(
            resultSet.getLong("theme_id"),
            resultSet.getString("theme_name"),
            resultSet.getString("theme_description"),
            resultSet.getString("theme_image_url")
        )
    );

    private final RowMapper<MyWaitingResult> myWaitingResultRowMapper = (resultSet, rowNum) -> new MyWaitingResult(
        resultSet.getLong("waiting_id"),
        resultSet.getString("name"),
        resultSet.getDate("date").toLocalDate(),
        resultSet.getTime("time_start_at").toLocalTime(),
        resultSet.getString("theme_name"),
        resultSet.getInt("waiting_number")
    );

    public WaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("waiting")
            .usingGeneratedKeyColumns("id");
    }

    public Waiting save(Waiting waiting) {
        SqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("name", waiting.getName())
            .addValue("date", waiting.getDate())
            .addValue("time_id", waiting.getTime().getId())
            .addValue("theme_id", waiting.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Waiting.of(id, waiting.getName(), waiting.getDate(), waiting.getTime(),
            waiting.getTheme());
    }

    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, Long timeId, Long themeId, String name) {
        String query = """
            SELECT COUNT(*)
            FROM waiting
            WHERE date = ? AND time_id = ? AND theme_id = ? AND name = ?
            """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId, name);
        return count != null && count > 0;
    }

    public boolean existsById(Long id) {
        String query = """
            SELECT COUNT(*)
            FROM waiting
            WHERE id = ?
            """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        String query = "delete from waiting where id = ?";
        jdbcTemplate.update(query, id);
    }

    public List<MyWaitingResult> findByName(String name) {
        String query = """
            SELECT ranked.waiting_id, ranked.name, ranked.date,
                   t.start_at AS time_start_at,
                   th.name AS theme_name,
                   ranked.waiting_number
            FROM (
                SELECT
                    w.id AS waiting_id,
                    w.name,
                    w.date,
                    w.time_id,
                    w.theme_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY w.theme_id, w.time_id, w.date
                        ORDER BY w.id
                    ) AS waiting_number
                FROM waiting w
            ) ranked
            JOIN reservation_time t ON ranked.time_id = t.id
            JOIN theme th ON ranked.theme_id = th.id
            WHERE ranked.name = ?
            ORDER BY ranked.date DESC, ranked.waiting_id
            """;
        return jdbcTemplate.query(query, myWaitingResultRowMapper, name);
    }

    public Optional<Waiting> findById(Long id) {
        String query = """
            SELECT w.id AS waiting_id, w.name, w.date,
                   t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                   th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                   th.image_url AS theme_image_url
            FROM waiting w
            JOIN reservation_time t ON w.time_id = t.id
            JOIN theme th ON w.theme_id = th.id
            WHERE w.id = ?
            """;

        return jdbcTemplate.query(query, waitingRowMapper, id).stream()
            .findFirst();
    }

    public Optional<Waiting> findFirstByTimeSlotForUpdate(TimeSlot timeSlot) {
        String query = """
            SELECT w.id AS waiting_id, w.name, w.date,
                   t.id AS time_id, t.start_at AS time_start_at, t.finish_at AS time_finish_at,
                   th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                   th.image_url AS theme_image_url
            FROM waiting w
            JOIN reservation_time t ON w.time_id = t.id
            JOIN theme th ON w.theme_id = th.id
            WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
            ORDER BY w.id
            LIMIT 1
            FOR UPDATE OF w
            """;

        return jdbcTemplate.query(query, waitingRowMapper,
                timeSlot.date(),
                timeSlot.timeId(),
                timeSlot.themeId())
            .stream()
            .findFirst();
    }
}
