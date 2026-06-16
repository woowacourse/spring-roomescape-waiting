package roomescape.domain.waiting;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Waiting> waitingRowMapper = (rs, rowNum) -> Waiting.of(
            rs.getLong("waiting_id"),
            rs.getString("name"),
            rs.getDate("date").toLocalDate(),
            ReservationTime.of(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime(),
                    rs.getTime("time_finish_at").toLocalTime()
            ),
            Theme.of(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_image_url"),
                    rs.getLong("theme_price")
            )
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
        return Waiting.of(id, waiting.getName(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
    }

    public boolean existsById(Long id) {
        String query = "SELECT COUNT(*) FROM waiting WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM waiting WHERE id = ?", id);
    }

    public List<Waiting> findAllBySlot(ReservationSlot slot) {
        String query = """
                SELECT w.id AS waiting_id, w.name, w.date,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM waiting w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.id
                """;
        return jdbcTemplate.query(query, waitingRowMapper,
                slot.getDate(), slot.getTime().getId(), slot.getTheme().getId());
    }

    public List<Waiting> findAllBySlotForUpdate(ReservationSlot slot) {
        String query = """
                SELECT w.id AS waiting_id, w.name, w.date,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM waiting w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.date = ? AND w.time_id = ? AND w.theme_id = ?
                ORDER BY w.id
                FOR UPDATE
                """;
        return jdbcTemplate.query(query, waitingRowMapper,
                slot.getDate(), slot.getTime().getId(), slot.getTheme().getId());
    }


    public List<Waiting> findByName(String name) {
        String query = """
                SELECT w.id AS waiting_id, w.name, w.date,
                       rt.id AS time_id, rt.start_at AS time_start_at, rt.finish_at AS time_finish_at,
                       th.id AS theme_id, th.name AS theme_name, th.description AS theme_description,
                       th.image_url AS theme_image_url, th.price AS theme_price
                FROM waiting w
                JOIN reservation_time rt ON w.time_id = rt.id
                JOIN theme th ON w.theme_id = th.id
                WHERE w.name = ?
                ORDER BY w.date DESC, w.id
                """;
        return jdbcTemplate.query(query, waitingRowMapper, name);
    }
}