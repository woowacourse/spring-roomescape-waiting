package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationtime.AvailableReservationTimeDto;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<ReservationTime> rowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        LocalTime startAt = rs.getObject("start_at", LocalTime.class);

        return new ReservationTime(id, startAt);
    };

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";

        try {
            ReservationTime reservationTime = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.of(reservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<AvailableReservationTimeDto> findAvailableReservationTimes(LocalDate date, Long themeId) {
        String sql = """
                    SELECT
                        rt.id,
                        rt.start_at,
                        COUNT(r.id) > 0 AS already_booked
                    FROM reservation_time AS rt
                    LEFT JOIN reservation AS r
                    ON r.time_id = rt.id AND r.date = ? AND r.theme_id = ?
                    GROUP BY rt.id, rt.start_at
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long id = rs.getLong("id");
            LocalTime startAt = rs.getObject("start_at", LocalTime.class);
            boolean alreadyBooked = rs.getBoolean("already_booked");

            return new AvailableReservationTimeDto(id, startAt, alreadyBooked);
        }, date, themeId);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE id = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }
}
