package roomescape.dao;

import static roomescape.dao.rowmapper.ReservationTimeMapper.RESERVATION_TIME_ROW_MAPPER;
import static roomescape.dao.rowmapper.ReservationTimeMapper.RESERVATION_TIME_STATUS_ROW_MAPPER;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.slot.time.ReservationTime;

@Repository
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("start_at", reservationTime.getStartAt());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    public Optional<ReservationTime> findById(Long timeId) {
        String sql = """
                SELECT id, start_at 
                FROM reservation_time 
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        RESERVATION_TIME_ROW_MAPPER,
                        timeId
                ).stream()
                .findFirst();
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                RESERVATION_TIME_ROW_MAPPER
        );
    }

    public List<TimeQueryResult> findStatuesByThemeIdAndDate(Long id, LocalDate date) {
        return jdbcTemplate.query(
                """
                          SELECT t.id AS time_id,
                                 t.start_at,
                                    CASE 
                                        WHEN r.id IS NULL 
                                        THEN FALSE ELSE TRUE 
                                    END AS reserved
                          FROM reservation_time t
                          LEFT JOIN reservation r ON t.id = r.time_id
                              AND r.theme_id = ?
                              AND r.date = ?
                          ORDER BY t.start_at
                        """,
                RESERVATION_TIME_STATUS_ROW_MAPPER,
                id,
                date
        );
    }

    public boolean existsById(Long id) {
        Boolean result = jdbcTemplate.queryForObject("""
                        SELECT EXISTS(
                            SELECT 1
                            FROM reservation_time
                            WHERE id = ?
                        )
                        """,
                Boolean.class,
                id
        );
        return Boolean.TRUE.equals(result);
    }

    public void delete(Long id) {
        String sql = """
                DELETE FROM reservation_time WHERE id = ?
                """;

        jdbcTemplate.update(sql, id);
    }
}
