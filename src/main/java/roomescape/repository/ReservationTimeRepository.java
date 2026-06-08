package roomescape.repository;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;

@Repository
@Transactional(readOnly = true)
public class ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<ReservationTime> reservationTimeRowMapper =
            (resultSet, rowNum) -> new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getObject("start_at", LocalTime.class)
            );

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                reservationTimeRowMapper
        );
    }

    public boolean existsByTimeId(Long timeId) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation_time rt
                            JOIN reservation r ON r.time_id = rt.id
                            WHERE rt.id = ?
                        )
                        """,
                Boolean.class,
                timeId
        );
        return Boolean.TRUE.equals(result);
    }

    public boolean existsByStartAt(LocalTime time) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT *
                            FROM reservation_time
                            WHERE start_at = ?
                        )
                        """,
                Boolean.class,
                time
        );
        return Boolean.TRUE.equals(result);
    }

    @Transactional
    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("start_at", reservationTime.getStartAt());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    public Optional<ReservationTime> findTimeById(Long timeId) {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time WHERE id = ?",
                reservationTimeRowMapper,
                timeId
        ).stream().findFirst();
    }

    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }
}
