package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
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
                            rs.getTime("start_at").toLocalTime()),
                    ReservationStatus.from(rs.getString("status"))
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
                "select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, "
                        + "r.theme_id as theme_id, r.status "
                        + "from reservation r "
                        + "inner join reservation_time t "
                        + "on r.time_id = t.id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Reservation> selectById(Long id) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id, r.status
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.id = ?
                        """;

        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Optional<Reservation> selectByIdForUpdate(Long id) {
        String sql = """
                select r.id as reservation_id, r.name, r.date,
                       t.id as time_id, t.start_at as start_at,
                       r.theme_id as theme_id, r.status
                from reservation r
                inner join reservation_time t
                on r.time_id = t.id
                where r.id = ?
                for update
                """;

        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public List<Reservation> selectByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id, r.status
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
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id, r.status
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

    public Optional<Reservation> selectByThemeIdAndDateAndTimeIdForUpdate(Long themeId, LocalDate date, Long timeId) {
        String sql = """
                select r.id as reservation_id, r.name, r.date,
                       t.id as time_id, t.start_at as start_at,
                       r.theme_id as theme_id, r.status
                from reservation r
                inner join reservation_time t
                on r.time_id = t.id
                where r.theme_id = ? and r.date = ? and r.time_id = ?
                for update
                """;

        return jdbcTemplate.query(sql, rowMapper, themeId, date, timeId).stream().findFirst();
    }

    public List<Reservation> selectByName(String name) {
        String sql =
                """
                        select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id, r.status
                        from reservation r
                        inner join reservation_time t
                        on r.time_id = t.id
                        where r.name = ?
                        """;

        return jdbcTemplate.query(sql, rowMapper, name);
    }

    public Reservation insert(Reservation reservation) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("theme_id", reservation.getThemeId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("status", reservation.getStatus().name());

        Long id = (long) simpleJdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(id, reservation.getName(), reservation.getThemeId(), reservation.getDate(),
                reservation.getTime(), reservation.getStatus());
    }

    public Optional<Reservation> updateDateTimeById(Long id, LocalDate date, Long timeId) {
        String sql = "update reservation set date = ?, time_id = ? where id = ?";
        jdbcTemplate.update(sql, date, timeId, id);

        return selectById(id);
    }

    public Optional<Reservation> updateNameByThemeIdAndDateAndTimeId(Reservation reservation) {
        String sql = """
                update reservation
                set name = ?, status = ?
                where id = ? and theme_id = ? and date = ? and time_id = ?
                """;

        jdbcTemplate.update(
                sql,
                reservation.getName(),
                reservation.getStatus().name(),
                reservation.getId(),
                reservation.getThemeId(),
                reservation.getDate(),
                reservation.getTime().getId()
        );

        return selectById(reservation.getId());
    }

    public void updateStatusById(Long id, ReservationStatus status) {
        String sql = "update reservation set status = ? where id = ?";
        jdbcTemplate.update(sql, status.name(), id);
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

    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
