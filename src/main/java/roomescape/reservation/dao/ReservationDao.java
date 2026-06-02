package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.MyReservation;
import roomescape.reservation.Reservation;
import roomescape.time.ReservationTime;

@Repository
public class ReservationDao {
    private static final RowMapper<Reservation> rowMapper = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getLong("theme_id"),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime())
            );

    private final RowMapper<MyReservation> combineRowMapper = (rs, rowNum) ->
            new MyReservation(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("theme_name"),
                    rs.getDate("date").toLocalDate(),
                    rs.getTime("start_at").toLocalTime(),
                    rs.getString("resource_type"),
                    rs.getString("status"),
                    rs.getObject("waiting_number", Long.class)
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> selectAll() {
        String sql =
                "select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id "
                        + "from reservation r "
                        + "inner join reservation_time t "
                        + "on r.time_id = t.id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Reservation> selectById(Long id) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.id = ?
                        """;
        List<Reservation> reservations = jdbcTemplate.query(sql, rowMapper, id);
        return reservations.stream().findFirst();
    }

    public List<Reservation> selectByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.theme_id = ?
                        and r.date = ?
                        """;
        return jdbcTemplate.query(sql, rowMapper, themeId, date);
    }

    public List<Reservation> selectByTimeId(Long timeId) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.time_id = ?
                        """;
        return jdbcTemplate.query(sql, rowMapper, timeId);
    }

    public List<Long> selectTimeIdByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql =
                """
                        select time_id
                        from reservation
                        where theme_id = ?
                        and date = ?
                        """;
        return jdbcTemplate.queryForList(sql, Long.class, themeId, date);
    }

    public List<Reservation> selectByName(String name) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.name = ?
                        """;
        return jdbcTemplate.query(sql, rowMapper, name);
    }

    public List<MyReservation> selectAllCombinedByName(String name) {
        String sql = """
                SELECT *
                FROM (
                    SELECT
                        r.id AS id,
                        r.name AS name,
                        t.name AS theme_name,
                        r.date AS date,
                        rt.start_at AS start_at,
                        'reservation' AS resource_type,
                        '예약 확정' AS status,
                        CAST(NULL AS BIGINT) AS waiting_number
                    FROM reservation r
                    JOIN theme t ON r.theme_id = t.id
                    JOIN reservation_time rt ON r.time_id = rt.id
                    WHERE r.name = ?
                
                    UNION ALL
                
                    SELECT
                        ranked.id AS id,
                        ranked.name AS name,
                        ranked.theme_name AS theme_name,
                        ranked.date AS date,
                        ranked.start_at AS start_at,
                        ranked.resource_type AS resource_type,
                        ranked.status AS status,
                        ranked.waiting_number AS waiting_number
                    FROM (
                        SELECT
                            w.id AS id,
                            w.name AS name,
                            t.name AS theme_name,
                            w.date AS date,
                            rt.start_at AS start_at,
                            'waiting' AS resource_type,
                            '대기중' AS status,
                            ROW_NUMBER() OVER (
                                PARTITION BY w.theme_id, w.date, w.time_id
                                ORDER BY w.created_at, w.id
                            ) AS waiting_number
                        FROM reservation_waiting w
                        JOIN theme t ON w.theme_id = t.id
                        JOIN reservation_time rt ON w.time_id = rt.id
                    ) ranked
                    WHERE ranked.name = ?
                )
                ORDER BY date, start_at, resource_type
                """;

        return jdbcTemplate.query(sql, combineRowMapper, name, name);
    }

    public Reservation insert(Reservation reservation) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("theme_id", reservation.getThemeId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(id, reservation.getName(), reservation.getThemeId(), reservation.getDate(),
                reservation.getTime());
    }

    public Optional<Reservation> updateDateTimeById(Long id, LocalDate date, Long timeId) {
        String sql = "update reservation set date = ?, time_id = ? where id = ?";
        jdbcTemplate.update(sql, date, timeId, id);

        return selectById(id);
    }

    public boolean existsByThemeIdAndAfterDate(Long themeId, LocalDate now) {
        String sql = """
                select count(*)
                from reservation
                where theme_id = ?
                and date >= ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId, now);
        return count > 0;
    }

    public boolean existsByNameAndDateAndThemeIdAndTimeId(String name, Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT EXISTS (
                                SELECT 1
                                    FROM reservation
                                    WHERE name = ? AND theme_id = ? AND date = ? AND time_id = ?
                            )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name, themeId, date, timeId) == Boolean.TRUE;
    }

    public boolean notExistsByDateAndThemeIdAndTimeId(Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT EXISTS (
                                SELECT 1
                                    FROM reservation
                                    WHERE  theme_id = ? AND date = ? AND time_id = ?
                            )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date, timeId) == Boolean.FALSE;
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
