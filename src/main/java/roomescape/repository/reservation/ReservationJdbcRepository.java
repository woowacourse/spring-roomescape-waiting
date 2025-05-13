package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.search.SearchConditions;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final String joinedReservationTableSql = """
            SELECT
                r.id AS reservation_id,
                r.date AS reservation_date,
                rt.id AS time_id,
                rt.start_at AS time_value,
                t.id AS theme_id,
                t.name AS theme_name,
                t.description AS theme_description,
                t.thumbnail AS theme_thumbnail,
                m.id AS member_id,
                m.name AS member_name,
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role
            FROM
                reservation r
            INNER JOIN reservation_time rt ON r.time_id = rt.id
            INNER JOIN theme t ON r.theme_id = t.id
            INNER JOIN member m ON r.member_id = m.id
            """;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", reservation.getDate());
        params.put("time_id", reservation.getTime().getId());
        params.put("theme_id", reservation.getTheme().getId());
        params.put("member_id", reservation.getMember().getId());

        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return reservation.withId(id);
    }

    @Override
    public int deleteReservationById(Long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Reservation> findAllReservation() {

        return jdbcTemplate.query(
                joinedReservationTableSql + ";",
                (resultSet, rowNum) -> {
                    ReservationTime reservationTime = new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_value").toLocalTime());

                    Theme theme = new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail"));

                    Member member = new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password"), Role.from(resultSet.getString("role")));

                    return new Reservation(
                            resultSet.getLong("reservation_id"),
                            resultSet.getDate("reservation_date").toLocalDate(),
                            reservationTime,
                            theme,
                            member
                    );
                }
        );
    }

    @Override
    public boolean existsByDateAndTime(LocalDate date, Long id) {
        String sql = "select count(*) from reservation where date = ? and time_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, date, id);
        return count != 0;
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "select count(*) from reservation where time_id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, timeId);
        return count != 0;
    }

    @Override
    public List<Long> findTimeIdsByDateAndTheme(LocalDate date, Long themeId) {
        String sql = "SELECT time_id from reservation where date=? and theme_id=?";
        List<Long> timeIds = jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> resultSet.getLong("time_id"),
                date,
                themeId);
        return timeIds;
    }

    @Override
    public List<Long> findTopThemesByReservationCountBetween(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT theme_id
                FROM reservation
                WHERE date BETWEEN ? AND ?
                GROUP BY theme_id
                ORDER BY COUNT(*) DESC
                LIMIT 10
                """;
        List<Long> themeIds = jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> resultSet.getLong("theme_id"),
                startDate,
                endDate
        );
        return themeIds;
    }

    @Override
    public List<Reservation> findReservationsByConditions(SearchConditions searchConditions) {
        String sql =
                joinedReservationTableSql + "WHERE r.member_id = ? AND r.theme_id = ? AND r.date >= ? AND r.date <= ?;";
        List<Reservation> reservations = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                    ReservationTime reservationTime = new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_value").toLocalTime());

                    Theme theme = new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail"));

                    Member member = new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password"), Role.from(resultSet.getString("role")));

                    return new Reservation(
                            resultSet.getLong("reservation_id"),
                            resultSet.getDate("reservation_date").toLocalDate(),
                            reservationTime,
                            theme,
                            member
                    );
                }, searchConditions.memberId(), searchConditions.themeId(), searchConditions.dateFrom(),
                searchConditions.dateTo());
        return reservations;
    }
}
