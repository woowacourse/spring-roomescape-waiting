package roomescape.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public class ReservationTimeDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<ReservationTime> timeRowMapper = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime()
    );

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("SELECT id, start_at FROM reservation_time", timeRowMapper);
    }

    public Optional<ReservationTime> findById(long id) {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time WHERE id = ?", timeRowMapper, id).stream().findFirst();
    }

    public boolean existsByStartAt(LocalTime startAt) {
        return Objects.requireNonNullElse(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_time WHERE start_at = ?", Integer.class, startAt), 0) > 0;
    }

    public ReservationTime save(LocalTime startAt) {
        long id = jdbcInsert.executeAndReturnKey(Map.of("start_at", startAt)).longValue();
        return new ReservationTime(id, startAt);
    }

    public int delete(long id) {
        return jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }
}
