package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservedTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> TIME_ROW_MAPPER = timeRowMapper();
    private static final RowMapper<ReservedTime> RESERVED_TIME_ROW_MAPPER = reservedTimeRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<ReservationTime> timeRowMapper() {
        return (resultSet, rowNum) -> ReservationTime.of(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
        );
    }

    private static RowMapper<ReservedTime> reservedTimeRowMapper() {
        return (resultSet, rowNum) -> new ReservedTime(
                ReservationTime.of(
                        resultSet.getLong("reservation_time_id"),
                        resultSet.getTime("start_at").toLocalTime()
                ),
                resultSet.getObject("reservation_id", Long.class) != null
        );
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return ReservationTime.of(
                generatedKey,
                reservationTime.getStartAt()
        );
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = """
                    SELECT id,
                           start_at
                    FROM reservation_time
                    WHERE id = :id
                """;

        List<ReservationTime> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("id", id),
                TIME_ROW_MAPPER
        );
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = """
                    SELECT id,
                           start_at
                    FROM reservation_time
                """;

        return jdbcTemplate.query(sql, TIME_ROW_MAPPER);
    }

    @Override
    public List<ReservedTime> findReservedTimes(LocalDate date, Long themeId) {
        String sql = """
                    SELECT rt.id AS reservation_time_id,
                           rt.start_at AS start_at,
                           r.id AS reservation_id
                    FROM reservation_time AS rt
                    LEFT JOIN slot AS s
                      ON s.time_id = rt.id
                      AND s.date = :date
                      AND s.theme_id = :theme_id
                    LEFT JOIN reservation AS r
                      ON r.slot_id = s.id
                      AND r.status = 'CONFIRMED'
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("theme_id", themeId);

        return jdbcTemplate.query(sql, params, RESERVED_TIME_ROW_MAPPER);
    }

    @Override
    public void delete(Long id) {
        String sql = """
                    DELETE FROM reservation_time
                    WHERE id = :id
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = """
                    SELECT EXISTS (
                      SELECT 1
                      FROM reservation_time
                      WHERE start_at = :start_at
                    )
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("start_at", startAt), Boolean.class)
        );
    }
}
