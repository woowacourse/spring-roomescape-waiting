package roomescape.time.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.Time;
import roomescape.time.TimeDao;

@Repository
public class TimeJdbcDao implements TimeDao {
    private static final RowMapper<Time> ROW_MAPPER = (resultSet, rowNum) -> new Time(
            resultSet.getLong("time_id"),
            LocalTime.parse(resultSet.getString("start_at"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public TimeJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("times")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Time> findAll() {
        String sql = """
                SELECT
                    id AS time_id,
                    start_at
                FROM times
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Optional<Time> findById(Long id) {
        String sql = """
                SELECT
                    id AS time_id,
                    start_at
                FROM times
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        return jdbcTemplate.query(sql, params, ROW_MAPPER).stream()
                .findFirst();
    }

    @Override
    public Time insert(Time time) {
        SqlParameterSource params = new MapSqlParameterSource("start_at", time.getStartAt());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Time(id, time.getStartAt());
    }

    @Override
    public Time update(Time time) {
        String sql = """
                UPDATE times
                SET start_at = :startAt
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startAt", time.getStartAt())
                .addValue("id", time.getId());
        jdbcTemplate.update(sql, params);
        return time;
    }

    @Override
    public boolean delete(Long id) {
        String sql = """
                DELETE FROM times
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        return jdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM times
                    WHERE id = :id
                )
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM times WHERE start_at = :startAt
                )
                """;
        SqlParameterSource params = new MapSqlParameterSource("startAt", startAt);

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }
}
