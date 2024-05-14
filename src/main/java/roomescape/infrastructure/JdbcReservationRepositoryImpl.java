package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberEmail;
import roomescape.domain.MemberName;
import roomescape.domain.MemberPassword;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Repository
public class JdbcReservationRepositoryImpl implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
            .withTableName("reservation")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> saveSource = Map.ofEntries(
            Map.entry("date", reservation.getReservationDate().getDate()),
            Map.entry("time_id", reservation.getReservationTime().getId()),
            Map.entry("theme_id", reservation.getTheme().getId()),
            Map.entry("member_id", reservation.getMember().getId())
        );

        long id = simpleJdbcInsert
            .executeAndReturnKey(saveSource)
            .longValue();

        return new Reservation(
            id,
            reservation.getMember(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getTheme()
        );
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
            SELECT 
                r.id AS reservation_id, 
                m.name AS member_name , 
                m.email AS member_email , 
                m.password AS member_password , 
                m.role AS member_role , 
                r.date AS reservation_date, 
                t.id AS time_id, 
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail 
            FROM reservation AS r 
            JOIN reservation_time AS t ON r.time_id = t.id
            JOIN theme AS th ON r.theme_id = th.id
            JOIN member AS m ON r.member_id = m.id
            """;

        return jdbcTemplate.query(
            sql, getReservationRowMapper()
        );
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean isTimeIdExists(Long id) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public boolean isThemeIdExists(Long id) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public boolean isDuplicated(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
            SELECT 
                r.id AS reservation_id, 
                m.name AS member_name, 
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role,
                r.date AS reservation_date, 
                t.id AS time_id, 
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail            
            FROM reservation AS r 
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            INNER JOIN member AS m ON r.member_id = m.id
            WHERE date = ? AND theme_id = ?
            """;

        return jdbcTemplate.query(
            sql, getReservationRowMapper(), date, themeId);
    }

    @Override
    public List<Reservation> findAllMemberIdAndThemeIdInPeriod(Long memberId, Long themeId, String from, String to) {
        String sql = """
            SELECT 
                r.id AS reservation_id, 
                m.id AS member_id,
                m.name AS member_name, 
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role,
                r.date AS reservation_date, 
                t.id AS time_id, 
                t.start_at AS time_value,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail            
            FROM reservation AS r 
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            INNER JOIN member AS m ON r.member_id = m.id
            WHERE m.id = ? AND th.id = ? AND r.date BETWEEN ? AND ?
            """;
        return jdbcTemplate.query(sql, getReservationRowMapper(), memberId, themeId, from, to);
    }

    private RowMapper<Reservation> getReservationRowMapper() {
        return (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            new Member(
                new MemberName(rs.getString("member_name")),
                new MemberEmail(rs.getString("member_email")),
                new MemberPassword(rs.getString("member_password")),
                MemberRole.from(rs.getString("member_role"))),
            new ReservationDate(
                rs.getString("reservation_date")),
            new ReservationTime(
                rs.getLong("time_id"),
                rs.getString("time_value")),
            new Theme(
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail"))
        );
    }
}
