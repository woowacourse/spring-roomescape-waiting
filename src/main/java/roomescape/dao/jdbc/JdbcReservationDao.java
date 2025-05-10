package roomescape.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.ReservationDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Reservation add(Reservation reservation) {
        Map<String, Object> param = new HashMap<>();
        param.put("date", Date.valueOf(reservation.getDate()));
        param.put("member_id", reservation.getMember().getId());
        param.put("time_id", reservation.getTime().getId());
        param.put("theme_id", reservation.getTheme().getId());

        Number key = jdbcInsert.executeAndReturnKey(param);

        return new Reservation(key.longValue(), reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                    INNER JOIN member as m ON r.member_id = m.id
                    INNER JOIN reservation_time as rt ON r.time_id = rt.id
                    INNER JOIN theme as t ON r.theme_id = t.id
                """;
        return jdbcTemplate.query(sql, mapResultsToReservation());
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    rt.id as time_id,
                    rt.start_at as time_value,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description,
                    t.thumbnail
                FROM reservation as r
                    INNER JOIN member as m ON r.member_id = m.id
                    INNER JOIN reservation_time as rt ON r.time_id = rt.id
                    INNER JOIN theme as t ON r.theme_id = t.id
                WHERE r.date = ? AND r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, mapResultsToReservation(), date, themeId);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existByDateTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    private RowMapper<Reservation> mapResultsToReservation() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("reservation_id"),
                new Member(rs.getLong("member_id"),
                        rs.getString("member_name"),
                        rs.getString("member_email"),
                        rs.getString("member_password"),
                        Role.valueOf(rs.getString("role"))),
                rs.getDate("date").toLocalDate(),
                new ReservationTime(rs.getLong("time_id"),
                        rs.getTime("time_value").toLocalTime()),
                new Theme(rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail")
                )
        );
    }
}
