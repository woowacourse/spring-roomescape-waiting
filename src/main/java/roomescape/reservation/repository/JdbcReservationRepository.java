package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            new Member(resultSet.getLong("member_id"), resultSet.getString("member_name"),
                    resultSet.getString("member_email"), resultSet.getString("member_password"),
                    Role.from(resultSet.getString("member_role"))),
            resultSet.getDate("date").toLocalDate(),
            new ReservationTime(resultSet.getLong("time_id"), resultSet.getTime("time_value").toLocalTime()),
            new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"), resultSet.getString("theme_thumbnail"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("RESERVATION")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT 
                    r.id AS reservation_id, 
                    r.date, 
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    t.id AS time_id, 
                    t.start_at AS time_value, 
                    th.id AS theme_id, 
                    th.name AS theme_name, 
                    th.description AS theme_description, 
                    th.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id 
                INNER JOIN theme AS th ON r.theme_id = th.id 
                INNER JOIN member AS m ON r.member_id = m.id;
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllByMemberAndThemeAndPeriod(Long memberId, Long themeId, LocalDate dateFrom,
                                                              LocalDate dateTo) {
        String sql = """
                SELECT
                    r.id AS reservation_id, 
                    r.date, 
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    t.id AS time_id, 
                    t.start_at AS time_value, 
                    th.id AS theme_id, 
                    th.name AS theme_name, 
                    th.description AS theme_description, 
                    th.thumbnail AS theme_thumbnail 
                FROM reservation AS r
                INNER JOIN reservation_time AS t ON r.time_id = t.id 
                INNER JOIN member AS m ON r.member_id = m.id
                INNER JOIN theme AS th ON r.theme_id = th.id 
                WHERE member_id = ? 
                AND theme_id = ? 
                AND r.date BETWEEN ? AND ?;
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, memberId, themeId, dateFrom, dateTo);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT
                    r.id AS reservation_id, 
                    r.date, 
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
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
                WHERE r.id = ?;
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(reservation);
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Reservation(id, reservation);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS ( 
                    SELECT 1 
                    FROM reservation 
                    WHERE date = ? AND time_id = ? AND theme_id = ?
                );
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}
