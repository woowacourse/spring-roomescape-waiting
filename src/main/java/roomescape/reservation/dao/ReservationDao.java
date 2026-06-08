package roomescape.reservation.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.Reservation;
import roomescape.time.ReservationTime;

@Repository
public class ReservationDao {
    private static final RowMapper<Reservation> MAPPER = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("theme_id"),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime())
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
                "select r.id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id "
                        + "from reservation r "
                        + "inner join reservation_time t "
                        + "on r.time_id = t.id";
        return jdbcTemplate.query(sql, MAPPER);
    }

    public Optional<Long> lockById(Long id) {
        String sql = "SELECT id FROM reservation r WHERE r.id = ? FOR UPDATE";
        List<Long> result = jdbcTemplate.queryForList(sql, Long.class, id);
        return result.stream().findFirst();
    }

    public Optional<Reservation> selectById(Long id) {
        String sql = """
            SELECT r.id, r.name, r.theme_id, r.date, t.id as time_id, t.start_at as start_at
            FROM reservation r
            inner join reservation_time t
            on r.time_id = t.id
            WHERE r.id = ?
            """;
        List<Reservation> result = jdbcTemplate.query(sql, MAPPER, id);
        return result.stream().findFirst();
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
                        select r.id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.name = ?
                        """;
        return jdbcTemplate.query(sql, MAPPER, name);
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

    public boolean existsUpcomingByTimeId(Long timeId, LocalDate today, LocalTime now) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation r
                    JOIN reservation_time t ON r.time_id = t.id
                    WHERE r.time_id = ?
                      AND (r.date > ? OR (r.date = ? AND t.start_at >= ?))
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId, today, today, now) == Boolean.TRUE;
    }

    public boolean existsByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long timeId) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM reservation
                WHERE theme_id = ? AND date = ? AND time_id = ?
            )
            """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date, timeId) == Boolean.TRUE;
    }

    public boolean existsByThemeIdAndDateAndTimeIdForUpdate(Long themeId, LocalDate date, Long timeId) {
        String sql = """
            SELECT 1
            FROM reservation
            WHERE theme_id = ? AND date = ? AND time_id = ?
            FOR UPDATE
            """;

        List<Integer> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getInt(1),
                themeId, date, timeId
        );
        return !result.isEmpty();
    }

    public boolean existsByNameAndThemeIdAndDateAndTimeId(String name, Long themeId, LocalDate date, Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE name = ? AND theme_id = ? AND date = ? AND time_id = ?
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name, themeId, date, timeId) == Boolean.TRUE;
    }

    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
