package roomescape.reservationtime.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
public class ReservationTimeDao {
    private static final RowMapper<ReservationTime> ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        );
        return reservationTime;
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime insert(ReservationTime reservationTime) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start_at", reservationTime.getStartAt());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);
        return new ReservationTime(generatedId.longValue(), reservationTime.getStartAt());
    }

    public List<ReservationTime> selectAll() {
        String sql = """
                SELECT id, 
                       start_at
                FROM reservation_time""";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public Optional<ReservationTime> selectById(Long reservationTimeId) {
        String sql = """
                SELECT id, 
                       start_at
                FROM reservation_time
                WHERE id = ?""";

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationTimeId));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    public void delete(long reservationTimeId) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = ?""";
        jdbcTemplate.update(sql, reservationTimeId);
    }
}
