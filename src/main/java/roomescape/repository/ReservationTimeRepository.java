package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

@Repository
public class ReservationTimeRepository {
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER =
            (rs, rowNum) -> RepositoryRowMapper.reservationTimeRowMapper(rs);
    private static final String EXISTS_BY_ID = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation_time
                    WHERE id = ?
                    )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime time) {
        Map<String, Object> params = Map.of(
                "start_at", time.getStartAt());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return time.withId(generatedKey);
    }

    public List<ReservationTime> findAll() {
        String sql = "select id AS time_id, start_at from reservation_time";
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }

    public Optional<ReservationTime> findById(long id) {
        String sql = "select id AS time_id, start_at from reservation_time where id = ?";
        List<ReservationTime> result = jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, id);
        return result.stream().findFirst();
    }

    public List<ReservationTime> findByDateAndTheme(LocalDate date, long themeId) {
        String sql = """
                SELECT rt.id AS time_id, rt.start_at
                FROM reservation_time AS rt
                WHERE rt.id NOT IN (
                    SELECT s.time_id
                    FROM slot s
                    INNER JOIN reservation r ON r.slot_id = s.id
                    WHERE s.date = ? AND s.theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER, date, themeId);
    }

    public void delete(long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsById(long reservationTimeId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_ID, Boolean.class, reservationTimeId));
    }
}
