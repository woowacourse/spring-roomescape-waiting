package roomescape.time.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.AvailableTime;
import roomescape.time.ReservationTime;

@Repository
public class TimeDao {
    private static final RowMapper<ReservationTime> timeRowMapper = (rs, rowNum) ->
            new ReservationTime(rs.getLong("id")
                    , rs.getTime("start_at").toLocalTime());

    private static final RowMapper<AvailableTime> availableTimeRowMapper = (rs, rowNum) ->
            new AvailableTime(
                    rs.getTime("start_at").toLocalTime(),
                    rs.getBoolean("is_available")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public TimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> selectAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    public ReservationTime selectById(Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        return jdbcTemplate.queryForObject(sql, timeRowMapper, id);
    }

    public ReservationTime insert(ReservationTime time) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new ReservationTime(id, time.getStartAt());
    }

    public void delete(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
