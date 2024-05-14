package roomescape.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.reservation.NotFoundReservationException;

@Repository
public class ReservationDao implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            LocalDate.parse(resultSet.getString("date")),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    LocalTime.parse(resultSet.getString("time_start_at"))
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            ),
            new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    "masked password",
                    Role.of(resultSet.getString("member_role"))
            )
    );

    public ReservationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT reservation.id, reservation.date, 
                `time`.id AS time_id, `time`.start_at AS time_start_at, 
                theme.id AS theme_id, theme.name AS theme_name, 
                theme.description AS theme_description, theme.thumbnail AS theme_thumbnail,
                member.id AS member_id, member.name AS member_name, member.email AS member_email,
                member.role AS member_role  
                FROM reservation 
                INNER JOIN reservation_time AS `time` ON reservation.time_id = `time`.id 
                INNER JOIN theme ON reservation.theme_id = theme.id
                INNER JOIN member ON reservation.member_id = member.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                SELECT reservation.id, reservation.date, 
                `time`.id AS time_id, `time`.start_at AS time_start_at, 
                theme.id AS theme_id, theme.name AS theme_name, 
                theme.description AS theme_description, theme.thumbnail AS theme_thumbnail, 
                member.id AS member_id, member.name AS member_name, member.email AS member_email, member.role AS member_role
                FROM reservation 
                INNER JOIN reservation_time AS `time` ON reservation.time_id = `time`.id 
                INNER JOIN theme ON reservation.theme_id = theme.id
                INNER JOIN member ON reservation.member_id = member.id
                WHERE reservation.id = ?
                """;
        List<Reservation> reservation = jdbcTemplate.query(sql, reservationRowMapper, id);
        return DataAccessUtils.optionalResult(reservation);
    }

    @Override
    public List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT time_id
                FROM reservation
                WHERE date = ? AND theme_id = ?
                """;
        return jdbcTemplate.queryForList(sql, Long.class, date, themeId);
    }

    @Override
    public List<Reservation> findByPeriodAndMemberAndTheme(LocalDate start, LocalDate end, String memberName,
                                                           String themeName) {
        String sql = """
                SELECT reservation.id, reservation.date,
                `time`.id AS time_id, `time`.start_at AS time_start_at, 
                theme.id AS theme_id, theme.name AS theme_name, 
                theme.description AS theme_description, theme.thumbnail AS theme_thumbnail, 
                member.id AS member_id, member.name AS member_name, member.email AS member_email, member.role AS member_role                
                FROM reservation
                INNER JOIN member ON reservation.member_id = member.id
                INNER JOIN theme ON reservation.theme_id = theme.id
                INNER JOIN reservation_time AS `time` ON reservation.time_id = `time`.id 
                WHERE date >= ? AND date <= ?
                AND member.name = ? 
                AND theme.name = ? 
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, start, end, memberName, themeName);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)";
        return Objects.requireNonNull(jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date", reservation.getDate());
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());
        parameters.put("member_id", reservation.getMember().getId());

        Long id = (Long) jdbcInsert.executeAndReturnKey(parameters);
        return new Reservation(
                id, reservation.getDate(), reservation.getTime(), reservation.getTheme(), reservation.getMember());
    }

    @Override
    public void delete(Reservation reservation) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int update = jdbcTemplate.update(sql, reservation.getId());
        checkRemoved(update);
    }

    private void checkRemoved(int count) {
        if (count < 1) {
            throw new NotFoundReservationException();
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM reservation";
        jdbcTemplate.update(sql);
    }
}
