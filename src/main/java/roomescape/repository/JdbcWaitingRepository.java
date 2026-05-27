package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Waiting> getWaitingRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(
                    resultSet.getLong("reservation_time_id"),
                    LocalTime.parse(resultSet.getString("time_value"))
            );

            Theme theme = Theme.of(resultSet.getLong("reservation_theme_id"),
                    resultSet.getString("reservation_theme_name"),
                    resultSet.getString("reservation_theme_description"),
                    resultSet.getString("reservation_theme_image_url")
            );

            return Waiting.of(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getDate("date").toLocalDate(),
                    time,
                    theme,
                    resultSet.getLong("waiting_number"));
        };
    }

    @Override
    public Waiting save(Waiting waiting) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(waiting)).longValue();

        return Waiting.of(
                generatedKey,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                waiting.getWaitingNumber());
    }

    @Override
    public Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime,
            Theme theme) {
        String sql = """
                    SELECT MAX(waiting_number)
                    FROM waiting
                    WHERE date = :date
                      AND time_id = :time_id
                      AND theme_id = :theme_id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("time_id", reservationTime.getId())
                .addValue("theme_id", theme.getId());

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, Long.class));
    }

    @Override
    public List<Waiting> findByName(String name) {
        String sql = """
                    select w.id as id,  
                    w.name, w.date, w.waiting_number,
                    
                    t.id as reservation_time_id,
                    t.start_at as time_value,
                    
                    th.id as reservation_theme_id,
                    th.name as reservation_theme_name,
                    th.description as reservation_theme_description,
                    th.image_url as reservation_theme_image_url
                    
                    from waiting as w
                     
                    inner join reservation_time as t
                    on w.time_id = t.id 
                    
                    inner join theme as th
                    on w.theme_id = th.id
                    
                    where w.name = :name
                """;

        Map<String, Object> params = Map.of("name", name);

        return jdbcTemplate.query(sql, params, getWaitingRowMapper());
    }
    @Override
    public boolean existsByNameAndDateAndTimeAndTheme(String name, LocalDate date,
            ReservationTime time, Theme theme) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM waiting
                    WHERE name = :name
                      AND date = :date
                      AND time_id = :time_id
                      AND theme_id = :theme_id
                )
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, params, Boolean.class)
        );
    }

    @Override
    public void delete(Long id) {
        String sql = """
                DELETE FROM waiting 
                WHERE id = :id
                """;
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }
}
