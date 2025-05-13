package roomescape.infrastructure.jdbc;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.TimeRepository;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;

@Repository
public class JdbcTimeRepository implements TimeRepository {

    private final static RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER =
            (rs, rowNum) -> ReservationTime.of(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long save(ReservationTime reservationTime) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> params = Map.of(
                "start_at", Time.valueOf(reservationTime.getStartAt())
        );

        Number key = simpleJdbcInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    @Override
    public List<ReservationTime> findAll() {
        String findAllSql = "SELECT id, start_at FROM reservation_time";

        return jdbcTemplate.query(findAllSql, RESERVATION_TIME_ROW_MAPPER);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String selectOneSql = "SELECT id, start_at FROM reservation_time WHERE id=?";
        try {
            ReservationTime time = jdbcTemplate.queryForObject(selectOneSql, RESERVATION_TIME_ROW_MAPPER,
                    id);
            return Optional.of(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String deleteSql = "DELETE FROM reservation_time WHERE id=?";

        return jdbcTemplate.update(deleteSql, id) > 0;
    }

    @Override
    public List<TimeDataWithBookingInfo> getTimesWithBookingInfo(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    t.id,
                    t.start_at,
                    CASE WHEN COUNT(r.id) > 0 THEN TRUE ELSE FALSE END AS already_booked
                FROM
                    reservation_time t
                LEFT JOIN
                    reservation r
                    ON t.id = r.time_id
                    AND r.date = ?
                    AND r.theme_id = ?
                GROUP BY
                    t.id, t.start_at
                ORDER BY
                    t.start_at;
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new TimeDataWithBookingInfo(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getBoolean("already_booked")
                ), date, themeId);
    }
}
