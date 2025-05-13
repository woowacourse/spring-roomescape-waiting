package roomescape.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.dto.response.AvailableTimeResponse;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getObject("start_at", LocalTime.class)
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time ORDER BY start_at";
        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> parameter = Map.of("start_at", reservationTime.getStartAt());
        Long newId = simpleJdbcInsert.executeAndReturnKey(parameter).longValue();
        return reservationTime.withId(newId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = :id";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameter);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "select * from reservation_time where id = :id";
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("id", id);
        try {
            ReservationTime foundReservationTime = jdbcTemplate.queryForObject(sql, parameter,
                    RESERVATION_TIME_ROW_MAPPER);
            return Optional.ofNullable(foundReservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AvailableTimeResponse> findByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        String sql = """
                SELECT rt.id, rt.start_at, r.id is not null as already_booked
                FROM reservation_time rt
                LEFT JOIN reservation r
                  ON rt.id = r.time_id
                  AND r.date = :date
                  AND r.theme_id = :themeId
                """;
        SqlParameterSource parameter = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return jdbcTemplate.query(
                sql,
                parameter,
                (rs, rowNum) -> new AvailableTimeResponse(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getBoolean("already_booked")
                )
        );
    }
}
