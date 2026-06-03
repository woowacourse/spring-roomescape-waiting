package roomescape.time.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcTimeRepository implements TimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert timeInsert;

    public JdbcTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.timeInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_time, end_time FROM reservation_time ORDER BY id",
                new ReservationTimeRowMapper()
        );
    }

    @Override
    public ReservationTime save(LocalDateTime startAt, LocalDateTime endAt) {
        Number id = timeInsert.executeAndReturnKey(
                new MapSqlParameterSource()
                        .addValue("start_time", startAt)
                        .addValue("end_time", endAt)
        );
        return new ReservationTime(id.longValue(), startAt, endAt);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        List<ReservationTime> results = jdbcTemplate.query(
                "SELECT id, start_time, end_time FROM reservation_time WHERE id = ?",
                new ReservationTimeRowMapper(),
                id
        );
        return results.stream().findFirst();
    }

    @Override
    public List<ReservationTime> findByDate(LocalDate date) {
        return jdbcTemplate.query(
                "SELECT id, start_time, end_time FROM reservation_time WHERE start_time >= ? AND start_time < ? ORDER BY start_time",
                new ReservationTimeRowMapper(),
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows = jdbcTemplate.update(
                "DELETE FROM reservation_time WHERE id = ?",
                id
        );
        return affectedRows > 0;
    }

    private static class ReservationTimeRowMapper implements RowMapper<ReservationTime> {
        @Override
        public ReservationTime mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReservationTime(
                    rs.getLong("id"),
                    rs.getObject("start_time", LocalDateTime.class),
                    rs.getObject("end_time", LocalDateTime.class)
            );
        }
    }
}
