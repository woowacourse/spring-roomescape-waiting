package roomescape.dao;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.dto.AvailableReservationTimeResult;
import roomescape.dao.mapper.AvailableReservationTimeMapper;
import roomescape.dao.mapper.ReservationTimeRowMapper;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;

@Repository
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final ReservationTimeRowMapper rowMapper;
    private final AvailableReservationTimeMapper availableReservationTimeMapper;

    public ReservationTimeDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource, final ReservationTimeRowMapper rowMapper,
                              final AvailableReservationTimeMapper availableReservationTimeMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = rowMapper;
        this.availableReservationTimeMapper = availableReservationTimeMapper;
    }

    public ReservationTime create(final ReservationTime reservationTime) {
        final SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAtAsString());
        final long id = jdbcInsert.executeAndReturnKey(params)
                .longValue();
        return ReservationTime.from(id, reservationTime.getStartAtAsString());
    }

    public boolean isExistByStartAt(final String startAt) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }

    public Optional<ReservationTime> find(final Long id) {
        final String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (final EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<ReservationTime> getAll() {
        final String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<AvailableReservationTimeResult> getAvailable(final ReservationDate date, final Long themeId) {
        final String sql = """
                SELECT
                t.id AS time_id,
                r.id IS NOT NULL AS booked,
                t.start_at AS start_at
                FROM reservation_time AS t
                LEFT OUTER JOIN reservation AS r
                ON t.id = r.time_id AND r.theme_id = ? AND r.date = ?;
                """;
        return jdbcTemplate.query(sql, availableReservationTimeMapper, themeId, date.asString());
    }

    public boolean delete(final long id) {
        final String sql = "DELETE FROM reservation_time WHERE id = ?";
        return jdbcTemplate.update(sql, id) == 1 ? Boolean.TRUE : Boolean.FALSE;
    }
}
