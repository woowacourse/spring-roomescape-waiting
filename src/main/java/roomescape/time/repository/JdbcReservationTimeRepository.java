package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) -> ReservationTime.load(
        resultSet.getLong("id"),
        resultSet.getTime("start_at").toLocalTime(),
        resultSet.getBoolean("is_active")
    );

    public JdbcReservationTimeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
            .withTableName("reservation_time")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), reservationTimeRowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id=:id";

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(sql, params, reservationTimeRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("start_at", reservationTime.getStartAt())
            .addValue("is_active", reservationTime.isActive());
        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return ReservationTime.load(savedId, reservationTime.getStartAt(),
            reservationTime.isActive());
    }

    @Override
    public boolean updateStatus(ReservationTime reservationTime) {
        String sql = """
            UPDATE reservation_time 
            SET is_active = :is_active
            WHERE id = :id
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", reservationTime.getId())
            .addValue("is_active", reservationTime.isActive());

        int updateCount = jdbcTemplate.update(sql, params);
        return updateCount > 0;
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "SELECT COUNT(*) FROM reservation_time WHERE start_at = :start_at";
        MapSqlParameterSource params = new MapSqlParameterSource("start_at", startAt);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<ReservationTime> findAvailableByDateIdAndThemeId(Long dateId, Long themeId) {
        String sql = """
            SELECT rt.*
            FROM reservation_time rt
            WHERE rt.is_active = true
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("date_id", dateId)
            .addValue("theme_id", themeId);

        return jdbcTemplate.query(sql, params, reservationTimeRowMapper);
    }
}
