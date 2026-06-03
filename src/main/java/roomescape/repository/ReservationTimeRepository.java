package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.KeyGenerationException;

import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    public ReservationTime save(final ReservationTime newReservationTime) {
        final String sql = """
                INSERT INTO reservation_time (start_at, end_at)
                VALUES (:startAt, :endAt)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("startAt", Time.valueOf(newReservationTime.getStartAt()))
                .addValue("endAt", Time.valueOf(newReservationTime.getEndAt()));

        jdbcTemplate.update(sql, param, keyHolder);

        final long newTimeId = keyHolder.getKey().longValue();
        return newReservationTime.withId(newTimeId);
    }

    public List<ReservationTime> findAll() {
        final String sql = """
                SELECT id, start_at, end_at
                FROM reservation_time
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper())
                .stream()
                .toList();
    }

    public Optional<ReservationTime> findById(final Long timeId) {
        final String sql = """
                SELECT id, start_at, end_at
                FROM reservation_time
                WHERE id = :id
                """;

        try {
            MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", timeId);
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, param, rowMapper());
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean delete(final Long timeId) {
        final String sql = """
                DELETE FROM reservation_time
                WHERE id = :id
                """;

        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", timeId);

        return jdbcTemplate.update(sql, param) > 0;
    }

    private RowMapper<ReservationTime> rowMapper() {
        return (rs, rowNum) -> ReservationTime.createWithId(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime(),
                rs.getTime("end_at").toLocalTime()
        );
    }
}
