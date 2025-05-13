package roomescape.reservation.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@Repository
public class JdbcReservationDao implements ReservationDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("reservation_id"),
                    new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password"),
                            MemberRole.valueOf(resultSet.getString("member_role"))
                    ),
                    resultSet.getDate("date").toLocalDate(),
                    new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_value").toLocalTime()
                    ),
                    new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail")
                    )
            );

    public JdbcReservationDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
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
                FROM reservation AS r 
                INNER JOIN member AS m ON r.member_id = m.id
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final String sql = "INSERT INTO reservation(date, member_id, time_id, theme_id) VALUES(:date, :memberId, :timeId, :themeId)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", Date.valueOf(reservation.getDate()))
                .addValue("memberId", reservation.getMember().getId())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return new Reservation(keyHolder.getKeyAs(Long.class), reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM reservation WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, parameters);
    }

    @Override
    public boolean isExistsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        final String sql = "SELECT count(*) FROM reservation WHERE date = :date AND time_id = :timeId AND theme_id = :themeId";
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);
        final Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count > 0;
    }

    @Override
    public boolean isExistsByTimeId(final long timeId) {
        final String sql = "SELECT count(*) FROM reservation WHERE time_id = :timeId";
        final SqlParameterSource parameters = new MapSqlParameterSource("timeId", timeId);
        Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count > 0;
    }

    @Override
    public boolean isExistsByThemeId(Long themeId) {
        final String sql = "SELECT count(*) FROM reservation WHERE theme_id = :themeId";
        final SqlParameterSource parameters = new MapSqlParameterSource("themeId", themeId);
        final Long count = jdbcTemplate.queryForObject(sql, parameters, Long.class);
        return count > 0;
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        final String sql = """
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
                FROM reservation AS r 
                INNER JOIN member AS m ON r.member_id = m.id
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                WHERE r.date = :date AND r.theme_id = :themeId
                """;
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);
        return jdbcTemplate.query(sql, parameters, reservationRowMapper);
    }
}
