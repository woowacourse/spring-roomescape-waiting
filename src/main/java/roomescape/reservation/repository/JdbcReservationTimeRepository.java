package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.dto.response.ReservationTimeResponse.AvailableReservationTimeResponse;
import roomescape.reservation.entity.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getObject("start_at", LocalTime.class)
    );

    @Override
    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (:start_at)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);

        return new ReservationTime(
                keyHolder.getKey().longValue(),
                time.getStartAt()
        );
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time ORDER BY start_at";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<AvailableReservationTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    r.id IS NOT NULL as alreadyBooked
                FROM reservation_time rt
                LEFT JOIN (
                    SELECT id, time_id
                    FROM reservation
                    WHERE date = :date AND theme_id = :themeId
                ) r ON r.time_id = rt.id
                ORDER BY rt.start_at
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date.toString())
                .addValue("themeId", themeId);

        return jdbcTemplate.query(sql, params, (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            LocalTime startAt = resultSet.getObject("start_at", LocalTime.class);
            boolean alreadyBooked = resultSet.getBoolean("alreadyBooked");

            return new AvailableReservationTimeResponse(
                    id,
                    startAt.toString(),
                    alreadyBooked
            );
        });
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            ReservationTime time = jdbcTemplate.queryForObject(sql, params, rowMapper);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int updated = jdbcTemplate.update(sql, params);
        return updated > 0;
    }
}
