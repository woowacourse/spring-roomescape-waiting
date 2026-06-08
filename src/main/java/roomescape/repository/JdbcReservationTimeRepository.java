package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (resultSet, rowNum) ->
            ReservationTime.load(resultSet.getLong("id"), resultSet.getTime("start_at").toLocalTime());

    private static final String BASE_SELECT= "select id, start_at from reservation_time";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime save(ReservationTime time) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return time.withId(generatedKey);
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(BASE_SELECT, RESERVATION_TIME_ROW_MAPPER);
    }

    public Optional<ReservationTime> findById(long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        List<ReservationTime> result = jdbcTemplate.query(BASE_SELECT + " where id = :id", param, RESERVATION_TIME_ROW_MAPPER);
        return result.stream().findFirst();
    }

    public List<ReservationTime> findByDateAndTheme(LocalDate date, long themeId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        String sql = """
                SELECT rt.id, rt.start_at
                FROM reservation_time AS rt
                WHERE rt.id NOT IN (
                    SELECT s.time_id
                    FROM slot s
                    INNER JOIN reservation r ON r.slot_id = s.id
                    WHERE s.date = :date AND s.theme_id = :themeId AND r.status = 'APPROVED'
                )
                """;
        return jdbcTemplate.query(sql, params, RESERVATION_TIME_ROW_MAPPER);
    }

    public void delete(long id) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", id);
        String sql = "delete from reservation_time where id = :id";

        jdbcTemplate.update(sql, param);
    }

    public boolean existsById(long reservationTimeId) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", reservationTimeId);
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM reservation_time WHERE id = :id)", param, Boolean.class));
    }
}
