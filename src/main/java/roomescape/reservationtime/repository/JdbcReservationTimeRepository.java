package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> timeRowMapper = (resultSet, rowNum) -> {
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        final LocalTime time = LocalTime.parse(resultSet.getString("start_at"), timeFormatter);
        return new ReservationTime(resultSet.getLong("id"), time);
    };

    private static final RowMapper<AvailableReservationTimeResponse> availableTimeRowMapper = (resultSet, rowNum) ->
            new AvailableReservationTimeResponse(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime(),
                    resultSet.getBoolean("alreadyBooked")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("reservation_time")
                .usingGeneratedKeyColumns("id")
                .usingColumns("start_at");
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        final Long newId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(newId, reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    @Override
    public List<AvailableReservationTimeResponse> findAllAvailable(LocalDate date, Long themeId) {
        String sql = """
                select
                    rt.id,
                    rt.start_at,
                    (r.id is not null) as alreadyBooked
                from reservation_time rt
                left join reservation r
                    on rt.id = r.time_id
                    and r.date = ?
                    and r.theme_id = ?
                order by rt.start_at
                """;

        return jdbcTemplate.query(sql, availableTimeRowMapper, date, themeId);
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "delete from reservation_time where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        final String sql = "select id, start_at from reservation_time where id = ?";
        return jdbcTemplate.query(sql, timeRowMapper, id)
                .stream()
                .findFirst();
    }
}
