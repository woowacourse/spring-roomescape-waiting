package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.TimeSlot;
import roomescape.dto.request.TimeSlotRequest;

@Repository
public class TimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<TimeSlot> rowMapper =
            (resultSet, rowNum) -> new TimeSlot(
                    resultSet.getLong("id"),
                    resultSet.getString("start_at")
            );

    public TimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingColumns("start_at")
                .usingGeneratedKeyColumns("id");
    }

    public List<TimeSlot> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public TimeSlot findById(Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public Long create(TimeSlotRequest timeSlotRequest) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("start_at", timeSlotRequest.startAt());
        return jdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    public void delete(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean isExist(LocalTime localTime) {
        String sql = "select exists(select 1 from reservation_time where start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, localTime);
    }
}
