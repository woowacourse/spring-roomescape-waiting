package roomescape.reservation.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Repository
public class ReservationDao implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(DataSource source) {
        this.jdbcTemplate = new JdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS time_value,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM 
                    reservation AS r 
                INNER JOIN 
                    reservation_time AS t 
                ON 
                    r.time_id = t.id
                INNER JOIN 
                    theme AS th
                ON
                    r.theme_id = th.id
                INNER JOIN 
                    member AS m
                ON 
                    r.member_id = m.id
                """;
        return jdbcTemplate.query(sql, this::mapRowToReservation);
    }

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId,
                                                                       LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS time_value,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role
                FROM 
                    reservation AS r 
                INNER JOIN 
                    reservation_time AS t 
                ON 
                    r.time_id = t.id
                INNER JOIN 
                    theme AS th
                ON
                    r.theme_id = th.id
                INNER JOIN 
                    member AS m
                ON 
                    r.member_id = m.id
                WHERE
                    r.member_id = ? AND r.theme_id = ? AND r.date >= ? AND r.date <= ? 
                """;
        return jdbcTemplate.query(sql, this::mapRowToReservation, memberId, themeId, fromDate, toDate);
    }

    private Reservation mapRowToReservation(ResultSet resultSet, int rowNumber) throws SQLException {
        ReservationTime reservationTime = mapRowToReservationTime(resultSet);
        Theme theme = mapRowToTheme(resultSet);
        Member member = mapRowToMember(resultSet);
        return new Reservation(
                resultSet.getLong("reservation_id"),
                member,
                resultSet.getDate("date").toLocalDate(),
                reservationTime,
                theme
        );
    }

    private ReservationTime mapRowToReservationTime(ResultSet resultSet) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_value").toLocalTime()
        );
    }

    private Theme mapRowToTheme(ResultSet resultSet) throws SQLException {
        return new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail")
        );
    }

    private Member mapRowToMember(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("member_email"),
                resultSet.getString("member_password"),
                Role.valueOf(resultSet.getString("member_role"))
        );
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 
                        1
                    FROM 
                        reservation AS r
                    WHERE 
                        `date` = ? AND r.time_id = ? AND r.theme_id = ?
                ) AS is_exist;
                """;
        return jdbcTemplate.queryForObject(sql,
                (resultSet, rowNumber) -> resultSet.getBoolean("is_exist"),
                Date.valueOf(date), timeId, themeId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMemberId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getReservationTimeId())
                .addValue("theme_id", reservation.getThemeId());
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(Objects.requireNonNull(id), reservation);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public int countByTimeId(Long timeId) {
        String sql = "SELECT count(*) FROM reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, timeId);
    }

    @Override
    public List<Long> findAllTimeIdsByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT 
                    time_id
                FROM 
                    reservation 
                WHERE 
                    date = ? AND theme_id = ?
                """;
        return jdbcTemplate.query(sql,
                (resultSet, rowNumber) -> resultSet.getLong("time_id"),
                Date.valueOf(date), themeId);
    }
}
