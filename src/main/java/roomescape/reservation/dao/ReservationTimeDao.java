package roomescape.reservation.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class ReservationTimeDao {

    private static final RowMapper<ReservationTime> ROW_MAPPER = (resultSet, rowNum) -> new ReservationTime(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime findById(final Long timeId) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, timeId);
    }

    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    public List<ReservationTime> findByStartAt(final LocalTime startAt) {
        String sql = "SELECT * FROM reservation_time WHERE start_at = ?";

        return jdbcTemplate.query(sql, ROW_MAPPER, startAt);
    }

    public ReservationTimeInfosResponse findByDateAndThemeId(final LocalDate date, final Long themeId) {
        String sql = """
                SELECT
                    rt.id,
                    rt.start_at,
                    CASE WHEN r.time_id IS NOT NULL THEN 'True' ELSE 'False' END AS is_reserved
                FROM reservation_time rt
                LEFT JOIN reservation r ON rt.id = r.time_id AND r.date = ? AND r.theme_id = ?;
                """;

        List<ReservationTimeInfoResponse> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            long timeId = rs.getLong("id");
            LocalTime startAt = rs.getTime("start_at").toLocalTime();
            boolean isReserved = rs.getBoolean("is_reserved");

            return new ReservationTimeInfoResponse(timeId, startAt, isReserved);
        }, date, themeId);

        return new ReservationTimeInfosResponse(results);
    }

    public ReservationTime insert(final ReservationTime requestReservationTime) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", requestReservationTime.getStartAt());
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, requestReservationTime.getStartAt());
    }


    public int deleteById(final Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
