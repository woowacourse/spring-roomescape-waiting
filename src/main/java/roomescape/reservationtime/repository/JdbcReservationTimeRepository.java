package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowColumn) -> ReservationTime.restore(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime(),
            resultSet.getTime("finish_at").toLocalTime()
    );

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt())
                .addValue("finish_at", time.getFinishAt());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return ReservationTime.restore(id, time.getStartAt(), time.getFinishAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        String query = "select * from reservation_time ORDER BY start_at ASC";
        return jdbcTemplate.query(query, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String query = "select * from reservation_time where id = ?";
        return jdbcTemplate.query(query, rowMapper, id).stream().findFirst();
    }

    @Override
    public List<ReservationTime> findAvailableByDateAndThemeId(LocalDate date, Long themeId) {
        String query = """
                SELECT rt.id, rt.start_at, rt.finish_at
                FROM reservation_time rt
                LEFT JOIN reservation r ON rt.id = r.time_id AND r.date = ? AND r.theme_id = ?
                WHERE r.id IS NULL
                ORDER BY rt.start_at ASC
                """;
        return jdbcTemplate.query(query, rowMapper, date, themeId);
    }

    @Override
    public boolean existsReservationByTimeId(Long timeId) {
        String query = "select count(*) from reservation where time_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, timeId);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String query = "delete from reservation_time where id = ?";
        jdbcTemplate.update(query, id);
    }
}
