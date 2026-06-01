package roomescape.time.infra;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> ReservationTime.restore(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime(),
            resultSet.getBoolean("is_active")
    );

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        String sql = """
                INSERT INTO reservation_time(start_at, is_active)
                VALUES(:startAt, :isActive)
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startAt", reservationTime.getStartAt())
                .addValue("isActive", reservationTime.isActive());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return ReservationTime.restore(generatedId, reservationTime.getStartAt(), reservationTime.isActive());
    }

    @Override
    public List<ReservationTime> findAll(int page, int size) {
        String sql = """
                SELECT id, start_at, is_active
                FROM reservation_time
                WHERE is_active = 1
                ORDER BY start_at ASC
                LIMIT :size OFFSET :offset
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("size", size)
                .addValue("offset", page * size);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<ReservationTime> findAllActive() {
        String sql = """
                SELECT id, start_at, is_active
                FROM reservation_time
                WHERE is_active = 1
                ORDER BY start_at ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        try {
            String sql = """
                    SELECT id, start_at, is_active
                    FROM reservation_time
                    WHERE id = :id
                    """;
            ReservationTime time = jdbcTemplate.queryForObject(sql, Map.of("id", id), rowMapper);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsActiveByStartAt(LocalTime time) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_time
                    WHERE start_at = :startAt AND is_active = 1
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("startAt", time), Boolean.class));
    }

    @Override
    public void update(ReservationTime time) {
        String sql = """
                UPDATE reservation_time
                SET start_at = :startAt,
                    is_active = :active
                WHERE id = :id
                """;
        jdbcTemplate.update(sql, Map.of(
                        "id", time.getId(),
                        "startAt", time.getStartAt(),
                        "active", time.isActive()
                )
        );
    }
}
