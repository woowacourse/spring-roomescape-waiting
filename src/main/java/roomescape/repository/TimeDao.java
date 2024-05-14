package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.dto.TimeSlotRequest;

@Repository
public class TimeDao {
    private static final RowMapper<ReservationTime> rowMapper =
            (resultSet, rowNum) -> new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getString("start_at")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public TimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingColumns("start_at")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<ReservationTime> findById(final Long id) {
        String sql = "select id, start_at from reservation_time where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));

        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Long create(TimeSlotRequest timeSlotRequest) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("start_at", timeSlotRequest.startAt());
        return jdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    public void delete(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean isExist(LocalTime localTime) {
        String sql = "select count(*) from reservation_time where start_at = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, localTime) != 0;
    }

    public List<ReservationTime> findByDateAndThemeId(final LocalDate date, final Long themeId) {
        String sql = """
                SELECT
                    t.id as time_id,
                    t.start_at as time_value,
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id where date = ? and theme_id = ?
                """;
        return jdbcTemplate.query(sql, (rowMapper, rowNumber) ->
                        new ReservationTime(rowMapper.getLong("time_id"), LocalTime.parse(rowMapper.getString("time_value"))),
                date, themeId);
    }
}
