package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Duration;
import roomescape.domain.LoginMember;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private static final String FIND_RESERVATION_QUERY = """
            SELECT
                R.id AS reservation_id,
                R.date AS reservation_date,
                T.id AS time_id,
                T.start_at AS time_value,
                T2.id AS theme_id,
                T2.name AS theme_name,
                T2.description AS description,
                T2.thumbnail AS thumbnail,
                M.id AS member_id,
                M.name AS member_name,
                M.role AS member_role,
                M.email AS member_email
            FROM RESERVATION AS R
                INNER JOIN RESERVATION_TIME T ON R.time_id = T.id
                INNER JOIN THEME T2 ON T2.id = R.theme_id
                INNER JOIN MEMBER M ON M.id = R.member_id""";
    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            rs.getDate("reservation_date").toLocalDate(),
            new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_value").toLocalTime()
            ),
            new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            ),
            new LoginMember(
                    rs.getLong("member_id"),
                    rs.getString("member_name"),
                    Role.valueOf(rs.getString("member_role"))
            )
    );
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            );
    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            String sql = "INSERT INTO RESERVATION(date,time_id,theme_id, member_id) VALUES ( ?,?,?,? )";
            PreparedStatement preparedStatement = con.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setDate(1, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(2, reservation.getReservationTime().getId());
            preparedStatement.setLong(3, reservation.getTheme().getId());
            preparedStatement.setLong(4, reservation.getMember().getId());
            return preparedStatement;
        }, keyHolder);
    }

    @Override
    public Reservations findAll() {
        List<Reservation> findReservations = jdbcTemplate.query(
                FIND_RESERVATION_QUERY,
                RESERVATION_ROW_MAPPER
        );
        return new Reservations(findReservations);
    }

    @Override
    public Reservations findByThemeAndDate(Theme theme, LocalDate date) {
        List<Reservation> findReservations = jdbcTemplate.query(
                FIND_RESERVATION_QUERY + " WHERE theme_id = ? AND R.date = ?",
                RESERVATION_ROW_MAPPER,
                theme.getId(),
                Date.valueOf(date));
        return new Reservations(findReservations);
    }

    @Override
    public Reservations searchBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String searchQuery = FIND_RESERVATION_QUERY + " WHERE ";
        boolean hasQuery = false;
        if (themeId != null) {
            searchQuery += "T2.id = " + themeId + " ";
            hasQuery = true;
        }
        if (memberId != null) {
            if (hasQuery) {
                searchQuery += "AND ";
            }
            searchQuery += "M.id = " + memberId + " ";
        }
        if (hasQuery) {
            searchQuery += "AND ";
        }
        String finalSearchQuery = searchQuery;
        List<Reservation> findReservations = jdbcTemplate.query(
                finalSearchQuery + "R.date >= ? AND R.date <= ?",
                RESERVATION_ROW_MAPPER,
                dateFrom, dateTo
        );

        return new Reservations(findReservations);
    }

    @Override
    public Themes findAndOrderByPopularity(Duration duration, int count) {
        List<Theme> findThemes = jdbcTemplate.query(
                """
                        SELECT TH.*, COUNT(*) AS count FROM THEME TH
                            JOIN RESERVATION R
                            ON R.theme_id = TH.id
                        WHERE R.date >= ? AND R.date <= ?
                        GROUP BY TH.id
                        ORDER BY count
                        DESC
                        LIMIT ?
                        """,
                THEME_ROW_MAPPER,
                duration.getStartDate(),
                duration.getEndDate(),
                count);
        return new Themes(findThemes);
    }

    @Override
    public boolean existByTimeId(long timeId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT time_id FROM RESERVATION WHERE time_id = ?)",
                Boolean.class, timeId
        );
    }

    @Override
    public boolean existByThemeId(long themeId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT theme_id FROM RESERVATION WHERE theme_id = ?)",
                Boolean.class, themeId
        );
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM RESERVATION WHERE id = ?", id);
    }
}
