package roomescape.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.rowmapper.ReservationRowMapper;
import roomescape.rowmapper.ThemeRowMapper;

public class ReservationCustomRepositoryImpl implements ReservationCustomRepository { // TODO: jdbcTemplate 대체해보기
    private final JdbcTemplate jdbcTemplate;
    private final ReservationRowMapper reservationRowMapper;
    private final ThemeRowMapper themeRowMapper;

    public ReservationCustomRepositoryImpl(JdbcTemplate jdbcTemplate, ReservationRowMapper reservationRowMapper,
                                           ThemeRowMapper themeRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRowMapper = reservationRowMapper;
        this.themeRowMapper = themeRowMapper;
    }

    @Override
    public List<Reservation> searchAll(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = """
                SELECT reservation.id, reservation.date, 
                member.id AS member_id, member.name AS member_name, member.email AS member_email, 
                member.password AS member_password, member.role AS member_role, 
                `time`.id AS time_id, `time`.start_at AS time_start_at, 
                theme.id AS theme_id, theme.name AS theme_name, 
                theme.description AS theme_description, theme.thumbnail AS theme_thumbnail 
                FROM reservation 
                INNER JOIN member ON reservation.member_id = member.id 
                INNER JOIN reservation_time AS `time` ON reservation.time_id = `time`.id 
                INNER JOIN theme ON reservation.theme_id = theme.id
                """;
        String whereClause = buildWhereClause(memberId, themeId, dateFrom, dateTo);
        return jdbcTemplate.query(sql + whereClause, reservationRowMapper);
    }

    private String buildWhereClause(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<String> conditions = new ArrayList<>();
        if (memberId != null) {
            conditions.add("reservation.member_id = " + memberId);
        }
        if (themeId != null) {
            conditions.add("reservation.theme_id = " + themeId);
        }
        if (dateFrom != null) {
            conditions.add("reservation.date >= '" + dateFrom + "'");
        }
        if (dateTo != null) {
            conditions.add("reservation.date <= '" + dateTo + "'");
        }
        if (conditions.isEmpty()) {
            return "";
        }
        return "WHERE " + String.join(" AND ", conditions);
    }

    @Override
    public List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        String sql = """
                SELECT time_id
                FROM reservation
                WHERE date = ? AND theme_id = ?
                """;
        return jdbcTemplate.queryForList(sql, Long.class, date, themeId);
    }

    @Override
    public List<Theme> findThemeWithMostPopularReservation(String startDate, String endDate) {
        String sql = """
                SELECT theme.id, theme.name, theme.description, theme.thumbnail
                FROM reservation
                LEFT JOIN theme ON theme.id=reservation.theme_id
                WHERE reservation.date > ? AND reservation.date < ?
                GROUP BY theme.id
                ORDER BY COUNT(*) DESC
                LIMIT 10;
                """;
        return jdbcTemplate.query(sql, themeRowMapper, startDate, endDate);
    }
}
