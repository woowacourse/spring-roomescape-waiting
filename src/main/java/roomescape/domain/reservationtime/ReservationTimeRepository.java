package roomescape.domain.reservationtime;


import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowColumn) -> ReservationTime.of(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime(),
            resultSet.getTime("finish_at").toLocalTime()
    );

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime time) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt())
                .addValue("finish_at", time.getFinishAt());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return ReservationTime.of(id, time.getStartAt(), time.getFinishAt());
    }

    public List<ReservationTime> findAll() {
        String query = "select * from reservation_time";
        return jdbcTemplate.query(query, rowMapper);
    }

    public Optional<ReservationTime> findById(Long id) {
        String query = "select * from reservation_time where id = ?";
        return jdbcTemplate.query(query, rowMapper, id)
                .stream()
                .findFirst();
    }

    public void deleteById(Long id) {
        String query = "delete from reservation_time where id = ?";
        jdbcTemplate.update(query, id);
    }

    public boolean existsById(Long id) {
        String query = """
                SELECT COUNT(*)
                FROM reservation_time
                WHERE id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByStartAt(LocalTime startAt) {
        String query = """
                SELECT COUNT(*)
                FROM reservation_time
                WHERE start_at = ?
                """;
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, startAt);
        return count != null && count > 0;
    }
}
