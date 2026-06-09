package roomescape.time.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

@Repository
public class ReservationTimeDao {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime()
    );

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationTime save(ReservationTime reservationTime) {
        String sql = """
                INSERT INTO reservation_time (start_at)
                VALUES (?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    public Optional<ReservationTime> findById(Long id) {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                sql,
                RESERVATION_TIME_ROW_MAPPER,
                id
        ).stream().findFirst();
    }


    public List<ReservationTime> findAll() {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                """;

        return jdbcTemplate.query(sql, RESERVATION_TIME_ROW_MAPPER);
    }

    public List<AvailableTimeQueryResult> queryAvailableTimes(Long themeId, LocalDate date) {
        String sql = """
                SELECT t.id, t.start_at,
                       CASE WHEN r.id IS NOT NULL THEN true ELSE false END AS already_booked
                FROM reservation_time t 
                LEFT JOIN reservation r
                ON t.id = r.time_id
                AND r.theme_id = ?
                AND r.reservation_date = ?
                """;

        RowMapper<AvailableTimeQueryResult> availableTimeRowMapper = (rs, rowNum) ->
                new AvailableTimeQueryResult(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime(),
                        rs.getBoolean("already_booked")
                );

        return jdbcTemplate.query(sql, availableTimeRowMapper, themeId, date);
    }

    public void delete(ReservationTime time) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = ?
                """;

        int affected = jdbcTemplate.update(sql, time.getId());
        if (affected == 0) {
            throw new NotFoundException(TimeErrorCode.TIME_NOT_FOUND);
        }
    }
}
