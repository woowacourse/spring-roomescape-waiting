package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationTime save(final LocalTime time) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", time);
        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return new ReservationTime(id, time);
    }

    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time";
        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new ReservationTime(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                ));
    }

    public void deleteById(final Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationTime> findById(final Long id) {
        String sql = "select * from reservation_time where id = ?";

        ReservationTime reservationTime;
        try {
            reservationTime = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) ->
                    new ReservationTime(
                            resultSet.getLong("id"),
                            LocalTime.parse(resultSet.getString("start_at"))
                    ), id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        return Optional.of(Objects.requireNonNull(reservationTime));
    }

    public boolean existByStartAt(final LocalTime startAt) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }

    public List<AvailableReservationTime> findAllAvailableReservationTimes(final LocalDate date, final Long themeId) {
        String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    r.id is NOT NULL as is_reserved
                FROM
                    reservation_time rt
                LEFT JOIN
                    reservation r ON r.time_id = rt.id AND r.date = ? AND r.theme_id = ? 
                GROUP BY
                    rt.id
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new AvailableReservationTime(
                        new ReservationTime(
                                resultSet.getLong("id"),
                                LocalTime.parse(resultSet.getString("start_at"))
                        ),
                        resultSet.getBoolean("is_reserved")
                ), date, themeId);
    }
}
