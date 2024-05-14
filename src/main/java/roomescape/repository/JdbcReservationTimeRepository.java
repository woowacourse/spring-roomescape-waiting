package roomescape.repository;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;

@Repository
public class JdbcReservationTimeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<ReservationTime> rowMapper;

    public JdbcReservationTimeRepository(DataSource dataSource, RowMapper<ReservationTime> rowMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = rowMapper;
    }

    public List<ReservationTime> findAllReservationTimes() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public ReservationTime insertReservationTime(ReservationTime reservationTime) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        long savedId = jdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return findReservationTimeById(savedId);
    }

    public void deleteReservationTimeById(long id) {
        String sql = "DELETE FROM reservation_time WHERE id = :id";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameterSource);
    }

    public boolean isTimeExistsByStartTime(String startAt) {
        String sql = "SELECT 1 FROM reservation_time WHERE start_at = :startAt";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("startAt", startAt);
        List<Integer> result = jdbcTemplate.queryForList(sql, parameterSource, Integer.class);
        return !result.isEmpty();
    }

    public boolean isTimeExistsByTimeId(long timeId) {
        String sql = "SELECT 1 FROM reservation_time WHERE id = :timeId";
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("timeId", timeId);
        List<Integer> result = jdbcTemplate.queryForList(sql, parameterSource, Integer.class);
        return !result.isEmpty();
    }

    public ReservationTime findReservationTimeById(long savedId) {
        String sql = """
                SELECT 
                t.id, 
                start_at 
                FROM reservation_time AS t 
                WHERE t.id = :savedId;
                """;
        SqlParameterSource paramMap = new MapSqlParameterSource().addValue("savedId", savedId);
        return jdbcTemplate.query(sql, paramMap, rowMapper).get(0);
    }

    public List<ReservationTime> findReservedTimeByThemeAndDate(String date, long themeId) {
        String sql = """
                SELECT 
                t.id AS time_id, 
                t.start_at AS time_value
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id 
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.date = :date
                AND r.theme_id = :themeId
                """;

        SqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return jdbcTemplate.query(sql, paramMap, rowMapper);
    }
}
