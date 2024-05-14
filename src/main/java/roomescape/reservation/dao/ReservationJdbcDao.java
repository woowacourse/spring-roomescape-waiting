package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Component
public class ReservationJdbcDao implements ReservationDao {

    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum)
            -> Reservation.reservationOf(
            resultSet.getLong("id"),
            resultSet.getDate("date")
                    .toLocalDate(),
            new Time(resultSet.getLong("timeId"),
                    resultSet.getTime("start_at")
                            .toLocalTime()),
            Theme.themeOf(resultSet.getLong("themeId"),
                    resultSet.getString("themeName"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")),
            Member.memberOf(resultSet.getLong("memberId"),
                    resultSet.getString("memberName"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("role")
            )
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationJdbcDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getReservationTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("member_id", reservation.getMember().getId());

        long id = jdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
        reservation.setIdOnSave(id);
        return reservation;
    }

    @Override
    public List<Reservation> findAllReservationOrderByDateAndTimeStartAt() {
        String findAllReservationSql = """
                SELECT r.id, r.date,
                    t.id AS timeId, t.start_at,
                    th.id AS themeId, th.name AS themeName, th.description, th.thumbnail,
                    m.id AS memberId, m.name AS memberName, m.email, m.password, m.role
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                INNER JOIN member m ON r.member_id = m.id
                ORDER BY r.date ASC, t.start_at ASC
                """;

        return jdbcTemplate.query(findAllReservationSql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllByThemeIdAndDate(long themeId, LocalDate date) {
        String findAllByThemeIdAndDateSql = """
                SELECT r.id, r.date,
                    t.id AS timeId, t.start_at,
                    th.id AS themeId, th.name AS themeName, th.description, th.thumbnail,
                    m.id AS memberId, m.name AS memberName, m.email, m.password, m.role
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                INNER JOIN member m ON r.member_id = m.id
                WHERE r.date = ? AND r.theme_id = ?
                ORDER BY r.date ASC, t.start_at ASC
                """;

        return jdbcTemplate.query(findAllByThemeIdAndDateSql, RESERVATION_ROW_MAPPER, date, themeId);
    }

    @Override
    public List<Theme> findThemeByDateOrderByThemeIdCountLimit(LocalDate startDate, LocalDate endDate, int limitCount) {
        String findThemesInOrderSql = """
                SELECT th.id, th.name, th.thumbnail, th.description
                FROM theme th
                INNER JOIN (
                    SELECT theme_id
                    FROM reservation
                    WHERE date BETWEEN ? AND ?
                    GROUP BY theme_id
                    ORDER BY COUNT(theme_id) DESC
                    LIMIT ?
                ) r ON th.id = r.theme_id;
                """;

        return jdbcTemplate.query(findThemesInOrderSql, (resultSet, rowNum)
                        -> Theme.themeOf(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail")
                )
                , startDate.toString(), endDate.toString(), limitCount);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String existByThemeIdSql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?) THEN TRUE ELSE FALSE END";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(existByThemeIdSql, Boolean.class, themeId));
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String existByTimeIdSql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM reservation WHERE time_id = ?) THEN TRUE ELSE FALSE END";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(existByTimeIdSql, Boolean.class, timeId));
    }

    @Override
    public void deleteById(long reservationId) {
        String deleteReservationSql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(deleteReservationSql, reservationId);
    }

    @Override
    public List<Reservation> findAllByUserIdAndThemeIdBetweenDate(long userId, long themeId, LocalDate fromDate,
                                                                  LocalDate toDate) {
        String findReservationsInOrderSql = """
                SELECT r.id, r.date,
                    t.id AS timeId, t.start_at,
                    th.id AS themeId, th.name AS themeName, th.description, th.thumbnail,
                    m.id AS memberId, m.name AS memberName, m.email, m.password, m.role
                FROM reservation r
                INNER JOIN reservation_time t ON r.time_id = t.id
                INNER JOIN theme th ON r.theme_id = th.id
                INNER JOIN member m ON r.member_id = m.id
                WHERE m.id = ? AND th.id = ? AND DATE BETWEEN ? AND ?
                ORDER BY r.date ASC, t.start_at ASC
                """;

        return jdbcTemplate.query(findReservationsInOrderSql, RESERVATION_ROW_MAPPER, userId, themeId, fromDate,
                toDate);
    }
}
