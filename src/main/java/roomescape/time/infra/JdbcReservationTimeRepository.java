package roomescape.time.infra;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
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
        String sql = "INSERT INTO reservation_time(start_at) "
                + "VALUES(:startAt)";

        SqlParameterSource params = new BeanPropertySqlParameterSource(reservationTime);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return ReservationTime.restore(generatedId, reservationTime.getStartAt(), reservationTime.isActive());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at, is_active FROM reservation_time WHERE is_active = 1 ORDER BY start_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at, is_active FROM reservation_time WHERE id=:id AND is_active = 1";
        List<ReservationTime> results = jdbcTemplate.query(sql, Map.of("id", id), rowMapper);
        return results.stream().findFirst();
    }

    @Override
    public boolean existsByStartAt(LocalTime time) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at=:startAt AND is_active = 1)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("startAt", time), Boolean.class));
    }

    @Override
    public int delete(ReservationTime time) {
        String sql = "UPDATE reservation_time SET is_active=:active WHERE id=:id";
        return jdbcTemplate.update(sql, Map.of("id", time.getId(), "active", time.isActive()));
    }
}
