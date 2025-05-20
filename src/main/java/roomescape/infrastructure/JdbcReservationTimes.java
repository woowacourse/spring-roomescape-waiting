package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationTimes implements ReservationTimes {

    private static final RowMapper<ReservationTime> ROW_MAPPER = (resultSet, rowNum) -> ReservationTime.restore(
            resultSet.getString("id"),
            resultSet.getTime("start_at").toLocalTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insert;

    public JdbcReservationTimes(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time");
    }

    @Override
    public void save(ReservationTime reservationTime) {
        insert.execute(Map.of(
                "id", reservationTime.getId().value(),
                "start_at", reservationTime.getStartTime().value()
        ));
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public List<ReservationTime> findAvailableByDateAndThemeId(final LocalDate date, final Id themeId) {
        final String sql = """
                SELECT *
                FROM reservation_time
                WHERE id NOT IN (
                    SELECT rt.id
                    FROM reservation_time AS rt
                    INNER JOIN reservation AS r
                    ON r.time_id = rt.id
                    WHERE r.date = ?
                    AND r.theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, date, themeId.value());
    }

    @Override
    public List<ReservationTime> findNotAvailableByDateAndThemeId(final LocalDate date, final Id themeId) {
        final String sql = """
                SELECT *
                FROM reservation_time
                WHERE id IN (
                    SELECT rt.id
                    FROM reservation_time AS rt
                    INNER JOIN reservation AS r
                    ON r.time_id = rt.id
                    WHERE r.date = ?
                    AND r.theme_id = ?
                )
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, date, themeId.value());
    }

    @Override
    public Optional<ReservationTime> findById(final Id timeId) {
        try {
            final String sql = "SELECT * FROM reservation_time WHERE id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, timeId.value()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existById(final Id timeId) {
        final String sql = "SELECT COUNT(*) FROM reservation_time WHERE id = ? ";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId.value());
        return count != null && count > 0;
    }

    @Override
    public boolean existByTime(final LocalTime createTime) {
        final String sql = "SELECT COUNT(*) FROM reservation_time WHERE start_at = ? ";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, createTime);
        return count != null && count > 0;
    }

    @Override
    public boolean existBetween(final LocalTime startInclusive, final LocalTime endExclusive) {
        final String sql = """
                SELECT COUNT(*) FROM reservation_time
                WHERE start_at >= ? AND start_at < ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, startInclusive, endExclusive);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(final Id timeId) {
        final String sql = "DELETE FROM reservation_time WHERE id = ?";
        jdbcTemplate.update(sql, timeId.value());
    }

}
