package roomescape.holiday.repository;

import java.sql.Date;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.holiday.domain.Holiday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcHolidayRepository implements HolidayRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert holidayInsert;

    public JdbcHolidayRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.holidayInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("holiday")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Holiday save(Holiday holiday) {
        Number id = holidayInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("date", holiday.date()));
        return holiday.withId(id.longValue());
    }

    @Override
    public List<Holiday> findAll() {
        return jdbcTemplate.query(
                """
                        SELECT h.id, h.date
                        FROM holiday h
                        """,
                new HolidayRowMapper()
        );
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM holiday WHERE date = ?)",
                Integer.class,
                Date.valueOf(date)
        );
        return exists != null && exists == 1;
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows = jdbcTemplate.update("DELETE FROM holiday WHERE id = ?", id);
        return affectedRows > 0;
    }

    private static class HolidayRowMapper implements RowMapper<Holiday> {

        @Override
        public Holiday mapRow(ResultSet rs, int rowNum) throws SQLException {
            Holiday holiday = new Holiday(
                    rs.getDate("date").toLocalDate()
            );
            return holiday.withId(rs.getLong("id"));
        }
    }
}
