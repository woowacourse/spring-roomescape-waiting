package roomescape.reservation.infrastructure;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> Reservation.createWithId(
            resultSet.getLong("reservation_id"),
            Member.createWithId(
                    resultSet.getLong("member_id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    Role.findRole(resultSet.getString("role"))
            ),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.createWithId(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_value").toLocalTime()
            ),
            Theme.createWithId(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            )
    );

    private static final String RESERVATION_JOIN_QUERY =
            """               
            SELECT
                r.id as reservation_id,
                m.name as name,
                m.email as email,
                m.password as password,
                m.role as role,
                m.id as member_id,
                r.date,
                t.id as time_id,
                t.start_at as time_value,
                th.id as theme_id,
                th.name as theme_name,
                th.description,
                th.thumbnail
            FROM reservation as r
            INNER JOIN reservation_time as t ON r.time_id = t.id 
            INNER JOIN theme as th ON th.id = r.theme_id 
            INNER JOIN member as m ON m.id = r.member_id 
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date", Date.valueOf(reservation.getDate()));
        parameters.put("time_id", reservation.getTimeId());
        parameters.put("theme_id", reservation.getThemeId());
        parameters.put("member_id", reservation.getMemberId());

        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId) {
        String sql = RESERVATION_JOIN_QUERY + "WHERE th.id = ? AND r.date = ?";

        return jdbcTemplate.query(sql, ROW_MAPPER, themeId, Date.valueOf(date));
    }

    @Override
    public boolean deleteById(final Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int count = jdbcTemplate.update(sql, id);

        return count != 0;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(RESERVATION_JOIN_QUERY, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                             LocalDate dateTo) {
        List<Object> params = createParams(memberId, themeId, dateFrom, dateTo);
        String whereClause = createWhereClause(memberId, themeId, dateFrom, dateTo);

        return jdbcTemplate.query(RESERVATION_JOIN_QUERY + whereClause, ROW_MAPPER, params.toArray());
    }

    @Override
    public boolean existByReservationTimeId(final Long timeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    @Override
    public boolean hasSameReservation(Reservation reservation) {
        String sql = """
                SELECT EXISTS(            
                    SELECT 1
                    FROM reservation as r
                    INNER JOIN reservation_time as t ON r.time_id = t.id
                    INNER JOIN theme as th ON r.theme_id = th.id
                    WHERE r.date = ? and t.start_at = ? and th.id = ?
                )
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class,
                Date.valueOf(reservation.getDate()),
                Time.valueOf(reservation.getReservationTime()),
                reservation.getThemeId());
    }

    @Override
    public boolean existByThemeId(final Long themeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    private String addReservationMemberCondition(String sql, boolean hasCondition) {
        if (hasCondition) {
            sql += "AND r.member_id = ? ";
            return sql;
        }
        sql += " r.member_id = ? ";
        return sql;
    }

    private String addReservationThemeCondition(String sql, boolean hasCondition) {
        if (hasCondition) {
            sql += "AND r.theme_id = ? ";
            return sql;
        }
        sql += " r.theme_id = ? ";
        return sql;
    }

    private String addReservationDateFromCondition(String sql, boolean hasCondition) {
        if (hasCondition) {
            sql += "AND r.date >= ? ";
            return sql;
        }
        sql += " r.date >= ? ";
        return sql;
    }

    private String addReservationDateToCondition(String sql, boolean hasCondition) {
        if (hasCondition) {
            sql += "AND r.date <= ? ";
            return sql;
        }
        sql += " r.date <= ? ";
        return sql;
    }

    private List<Object> createParams(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Object> params = new ArrayList<>();
        if (memberId != null) {
            params.add(memberId);
        }
        if (themeId != null) {
            params.add(themeId);
        }
        if (dateFrom != null) {
            params.add(Date.valueOf(dateFrom));
        }
        if (dateTo != null) {
            params.add(Date.valueOf(dateTo));
        }
        return params;
    }

    private String createWhereClause(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = "WHERE ";
        boolean hasCondition = false;
        if (memberId != null) {
            sql = addReservationMemberCondition(sql, hasCondition);
            hasCondition = true;
        }
        if (themeId != null) {
            sql = addReservationThemeCondition(sql, hasCondition);
            hasCondition = true;
        }
        if (dateFrom != null) {
            sql = addReservationDateFromCondition(sql, hasCondition);
            hasCondition = true;
        }
        if (dateTo != null) {
            sql = addReservationDateToCondition(sql, hasCondition);
        }
        return sql;
    }

}
