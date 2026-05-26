package roomescape.domain.waiting;

import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@Repository
public class WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Waiting> rowMapper = (resultSet, rowNum) -> Waiting.of(
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
}
