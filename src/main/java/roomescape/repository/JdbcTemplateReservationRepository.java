package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.rowmapper.ReservationRowMapper;

@Repository
public class JdbcTemplateReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ReservationRowMapper reservationRowMapper;

    public JdbcTemplateReservationRepository(JdbcTemplate jdbcTemplate, ReservationRowMapper reservationRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRowMapper = reservationRowMapper;
    }

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        save(reservation, keyHolder);
        long id = keyHolder.getKey().longValue();
        return new Reservation(id, reservation);
    }

    private void save(Reservation reservation, KeyHolder keyHolder) {
        jdbcTemplate.update(con -> {
            String sql = "INSERT INTO reservation(member_id, date, time_id, theme_id) VALUES ( ?,?,?,? )";
            PreparedStatement preparedStatement = con.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setLong(1, reservation.getReservationMember().getId());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getReservationTime().getId());
            preparedStatement.setLong(4, reservation.getTheme().getId());
            return preparedStatement;
        }, keyHolder);
    }

    @Override
    public List<Reservation> findAll() {
        String query = """
                SELECT
                   r.id AS reservation_id,
                   r.date AS reservation_date,
                   t.id AS time_id,
                   t.start_at AS time_value,
                   t2.id AS theme_id,
                   t2.name AS theme_name,
                   t2.description AS description,
                   t2.thumbnail AS thumbnail,
                   m.id AS member_id,
                   m.name AS member_name,
                   m.email AS email,
                   m.password AS password,
                   m.role AS role
                FROM reservation AS r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme t2
                    ON t2.id = r.theme_id
                INNER JOIN member m ON r.member_id = m.id
                """;
        return jdbcTemplate.query(query, reservationRowMapper);
    }

    @Override
    public List<Reservation> findByMemberAndThemeBetweenDates(long memberId, long themeId, LocalDate start,
                                                              LocalDate end) {
        String query = """
                SELECT
                   r.id AS reservation_id,
                   r.date AS reservation_date,
                   t.id AS time_id,
                   t.start_at AS time_value,
                   t2.id AS theme_id,
                   t2.name AS theme_name,
                   t2.description AS description,
                   t2.thumbnail AS thumbnail,
                   m.id AS member_id,
                   m.name AS member_name,
                   m.email AS email,
                   m.password AS password,
                   m.role AS role
                FROM reservation AS r
                INNER JOIN reservation_time t
                    ON r.time_id = t.id
                INNER JOIN theme t2
                    ON t2.id = r.theme_id
                INNER JOIN member m ON r.member_id = m.id
                WHERE r.member_id = ?
                    AND r.theme_id = ?
                    AND
                        PARSEDATETIME(r.date,'yyyy-MM-dd') >= PARSEDATETIME(?,'yyyy-MM-dd')
                    AND
                        PARSEDATETIME(r.date,'yyyy-MM-dd') <= PARSEDATETIME(?,'yyyy-MM-dd')
                """;
        return jdbcTemplate.query(query, reservationRowMapper, memberId, themeId, start, end);
    }

    @Override
    public boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ? AND date = ? AND time_id = ?)";
        long themeId = theme.getId();
        long timeId = reservationTime.getId();
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId, date, timeId));
    }

    @Override
    public boolean existsByTime(ReservationTime reservationTime) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)";
        long timeId = reservationTime.getId();
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByTheme(Theme theme) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ?)";
        long themeId = theme.getId();
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}
