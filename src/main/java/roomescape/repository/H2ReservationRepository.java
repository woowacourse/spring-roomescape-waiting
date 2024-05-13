package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationNotFoundException;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class H2ReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public H2ReservationRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("RESERVATION")
                .usingGeneratedKeyColumns("ID");
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT R.ID, R.MEMBER_ID, R.DATE, R.TIME_ID, R.THEME_ID, RT.START_AT, T.NAME, T.NAME,
                        T.DESCRIPTION, T.THUMBNAIL, M.EMAIL, M.PASSWORD, M.NAME, M.ROLE
                FROM RESERVATION AS R
                LEFT JOIN RESERVATION_TIME RT ON RT.ID = R.TIME_ID
                LEFT JOIN THEME T ON T.ID = R.THEME_ID
                LEFT JOIN MEMBER M ON M.ID = R.MEMBER_ID
                """;

        return jdbcTemplate.query(sql, getReservation());
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        final String sql = """
                SELECT R.ID, R.MEMBER_ID, R.DATE, R.TIME_ID, R.THEME_ID, RT.START_AT, T.NAME FROM RESERVATION AS R
                JOIN RESERVATION_TIME RT ON RT.ID = R.TIME_ID
                JOIN THEME T ON T.ID = R.THEME_ID
                WHERE R.DATE = ? AND T.ID = ?
                """;

        return jdbcTemplate.query(sql, getReservationExceptTimeAndTheme(), date, themeId);
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        final String sql = "SELECT * FROM reservation WHERE ID = ?";

        return jdbcTemplate.query(sql, getReservationExceptTimeAndTheme(), id)
                .stream()
                .findAny();
    }

    @Override
    public Reservation fetchById(final long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        final String sql = "SELECT * FROM RESERVATION WHERE TIME_ID = ? LIMIT 1";

        return !jdbcTemplate.query(sql, getReservationExceptTimeAndTheme(), timeId)
                .isEmpty();
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        final String sql = "SELECT * FROM RESERVATION WHERE THEME_ID = ? LIMIT 1";

        return !jdbcTemplate.query(sql, getReservationExceptTimeAndTheme(), themeId)
                .isEmpty();
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final SqlParameterSource params = new MapSqlParameterSource()
                .addValue("MEMBER_ID", reservation.getMember().getId())
                .addValue("DATE", reservation.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addValue("TIME_ID", reservation.getTime().getId())
                .addValue("THEME_ID", reservation.getTheme().getId());

        final long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
    }

    @Override
    public boolean existsByThemesAndDateAndTimeId(final long themeId, final long timeId, final LocalDate date) {
        final String sql = "SELECT * FROM RESERVATION WHERE THEME_ID = ? AND TIME_ID = ? AND DATE = ? LIMIT 1";

        return !jdbcTemplate.query(sql, getReservationExceptTimeAndTheme(), themeId, timeId, date).isEmpty();
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM RESERVATION WHERE ID = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Reservation> searchReservations(final ReservationSearchCondition condition) {
        final String selectSql = """
                SELECT R.ID, R.MEMBER_ID, R.DATE, R.TIME_ID, R.THEME_ID, RT.START_AT, T.NAME, M.NAME
                FROM RESERVATION AS R
                JOIN RESERVATION_TIME RT ON RT.ID = R.TIME_ID
                JOIN THEME T ON T.ID = R.THEME_ID
                JOIN MEMBER M ON M.ID = R.MEMBER_ID
                """;

        final String whereSql = selectSql + generateDynamicSql(condition);

        return jdbcTemplate.query(whereSql, getSearchReservation());
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate from, final LocalDate until, final int limit) {
        final String sql = """
                SELECT R.THEME_ID, T.NAME, T.THUMBNAIL, T.DESCRIPTION, COUNT(T.ID) AS COUNT FROM RESERVATION AS R
                JOIN RESERVATION_TIME RT ON RT.ID = R.TIME_ID
                JOIN THEME T ON T.ID = R.THEME_ID
                WHERE R.DATE BETWEEN  ? AND ?
                GROUP BY R.THEME_ID
                ORDER BY COUNT DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, getTheme(), from, until, limit);
    }

    private RowMapper<Reservation> getReservation() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("ID"),
                new Member(
                        rs.getLong("MEMBER_ID"),
                        rs.getString("MEMBER.NAME"),
                        rs.getString("MEMBER.EMAIL"),
                        rs.getString("MEMBER.PASSWORD"),
                        Role.findByName(rs.getString("MEMBER.ROLE"))
                ),
                rs.getDate("DATE").toLocalDate(),
                new ReservationTime(
                        rs.getLong("RESERVATION.ID"),
                        rs.getTime("RESERVATION_TIME.START_AT").toLocalTime()
                ),
                new Theme(
                        rs.getLong("RESERVATION.ID"),
                        rs.getString("THEME.NAME"),
                        rs.getString("THEME.DESCRIPTION"),
                        rs.getString("THEME.THUMBNAIL")
                )
        );
    }

    private String generateDynamicSql(final ReservationSearchCondition condition) {
        final List<String> result = new ArrayList<>();
        addCondition(result, "R.THEME_ID = ", condition.themeId());
        addCondition(result, "R.MEMBER_ID = ", condition.memberId());
        addCondition(result, "R.DATE >= ", formatDate(condition.dateFrom()));
        addCondition(result, "R.DATE <= ", formatDate(condition.dateTo()));
        if (result.isEmpty()) {
            return "";
        }
        return "WHERE " + String.join(" AND ", result);
    }

    private String formatDate(final LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("'%s'", date);
    }

    private void addCondition(final List<String> result, final String sql, final Object target) {
        if (target != null) {
            result.add(sql + target);
        }
    }

    private RowMapper<Reservation> getReservationExceptTimeAndTheme() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("ID"),
                new Member(rs.getLong("RESERVATION.MEMBER_ID"), null, null, null, null),
                rs.getDate("DATE").toLocalDate(),
                new ReservationTime(rs.getLong("RESERVATION.TIME_ID"), null),
                new Theme(rs.getLong("RESERVATION.THEME_ID"), null, null, null)
        );
    }

    private RowMapper<Reservation> getSearchReservation() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("ID"),
                new Member(rs.getLong("RESERVATION.MEMBER_ID"), rs.getString("MEMBER.NAME"), null, null, null),
                rs.getDate("DATE").toLocalDate(),
                new ReservationTime(rs.getLong("RESERVATION.TIME_ID"), rs.getTime("RESERVATION_TIME.START_AT").toLocalTime()),
                new Theme(rs.getLong("RESERVATION.THEME_ID"), rs.getString("THEME.NAME"), null, null)
        );
    }

    private RowMapper<Theme> getTheme() {
        return (rs, rowNum) -> new Theme(
                rs.getLong("THEME_ID"),
                rs.getString("THEME.NAME"),
                rs.getString("THEME.DESCRIPTION"),
                rs.getString("THEME.THUMBNAIL")
        );
    }
}
