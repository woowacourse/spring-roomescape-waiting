package roomescape.dao.jdbc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.ReservationDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.custom.NotFoundException;

@Repository
public class JdbcReservationDao implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation")
            .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAllReservations() {
        String sql = """
            SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                rt.id as time_id,
                rt.start_at as time_value,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail
            FROM reservation as r
            INNER JOIN member as m
            INNER JOIN reservation_time as rt
            INNER JOIN theme as t
            ON r.member_id = m.id
            ON r.time_id = rt.id
            ON r.theme_id = t.id
            """;
        return jdbcTemplate.query(sql, createReservationMapper());
    }

    public List<Reservation> findReservationsByFilters(Long themeId, Long memberId,
        LocalDate dateFrom, LocalDate dateTo) {
        String sql = """
            SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                rt.id as time_id,
                rt.start_at as time_value,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail
            FROM reservation as r
            INNER JOIN member as m
            INNER JOIN reservation_time as rt
            INNER JOIN theme as t
            ON r.member_id = m.id
            ON r.time_id = rt.id
            ON r.theme_id = t.id
            WHERE theme_id = ?
                AND member_id = ?
                AND r.date >= ?
                AND r.date <= ?
            """;
        return jdbcTemplate.query(sql, createReservationMapper(), themeId, memberId, dateFrom, dateTo);
    }

    public boolean existReservationByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = """
            SELECT EXISTS (
                SELECT id
                FROM reservation
                WHERE date = ?
                    AND time_id = ?
                    AND theme_id = ?
                )
            """;
        return Boolean.TRUE.equals(
            jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId));
    }

    public Reservation addReservation(Reservation reservation) {
        Map<String, Object> param = new HashMap<>();
        param.put("member_id", reservation.getMember().getId());
        param.put("date", Date.valueOf(reservation.getDate()));
        param.put("time_id", reservation.getTime().getId());
        param.put("theme_id", reservation.getTheme().getId());

        Number key = jdbcInsert.executeAndReturnKey(param);
        return new Reservation(key.longValue(), reservation.getMember(), reservation.getDate(),
            reservation.getTime(), reservation.getTheme());
    }

    public void removeReservationById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int activeRow = jdbcTemplate.update(sql, id);

        if(activeRow == 0) {
            throw new NotFoundException("reservation");
        }
    }

    private RowMapper<Reservation> createReservationMapper() {
        return (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            new Member(
                rs.getLong("member_id"),
                rs.getString("member_name"),
                rs.getString("member_email"),
                rs.getString("member_password")
            ),
            rs.getDate("date").toLocalDate(),
            new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_value").toLocalTime()),
            new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail")
            )
        );
    }
}
