package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeRepository;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeJdbcRepository(JdbcTemplate jdbcTemplate,
                                         @Qualifier("reservationTimeJdbcInsert") SimpleJdbcInsert simpleJdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = simpleJdbcInsert;
    }

    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time";
        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new ReservationTime(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                ));
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<ReservationTime> findById(Long id) {
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

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = "SELECT 1 FROM reservation_time WHERE start_at = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), startAt);
        return !result.isEmpty();
    }

    @Override
    public List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(LocalDate date, Long themeId) {
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
                new AvailableReservationTimeResponse(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at")),
                        resultSet.getBoolean("is_reserved")
                ), date, themeId);
    }
}
