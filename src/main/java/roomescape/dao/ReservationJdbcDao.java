package roomescape.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.dto.request.ReservationSearchFilter;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@Repository
public class ReservationJdbcDao implements ReservationDao {

    private static final String SELECT_RESERVATION = """
            SELECT 
                r.id, 
                r.date,
                rt.id AS time_id, 
                rt.start_at AS time_start_at,
                t.id AS theme_id, 
                t.name AS theme_name, 
                t.description AS theme_description, 
                t.thumbnail AS theme_thumbnail,
                m.id AS member_id,
                m.name AS member_name,
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role
            FROM reservation AS r
            INNER JOIN reservation_time AS rt ON r.time_id = rt.id
            INNER JOIN theme AS t ON t.id = r.theme_id 
            INNER JOIN member AS m ON m.id = r.member_id 
            """;

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("id"),
                    resultSet.getDate("date").toLocalDate(),
                    new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("time_start_at").toLocalTime()),
                    new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail")),
                    new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password"),
                            Role.fromValue(resultSet.getString("member_role"))
                    )
            );

    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll(ReservationSearchFilter reservationSearchFilter) {
        WhereClause whereClause = toWhereClause(reservationSearchFilter);

        if (whereClause == null) {
            return jdbcTemplate.query(SELECT_RESERVATION, RESERVATION_ROW_MAPPER);
        }
        return jdbcTemplate.query(SELECT_RESERVATION + whereClause.clause, RESERVATION_ROW_MAPPER, whereClause.params);
    }

    @Override
    public Long saveReservation(Reservation reservation) {
        String sql = "INSERT INTO reservation (date, time_id, theme_id, member_id) values (?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setDate(1, Date.valueOf(reservation.getDate()));
            ps.setLong(2, reservation.getTime().getId());
            ps.setLong(3, reservation.getTheme().getId());
            ps.setLong(4, reservation.getMember().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Reservation> findByDateAndTime(Reservation reservation) {
        String sql = SELECT_RESERVATION + " WHERE r.date = ? AND rt.id = ?";

        return jdbcTemplate.query(
                        sql,
                        RESERVATION_ROW_MAPPER,
                        reservation.getDate(),
                        reservation.getTime().getId()).stream()
                .findFirst();
    }

    private WhereClause toWhereClause(ReservationSearchFilter reservationSearchFilter) {
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (reservationSearchFilter.themeId() != null) {
            clauses.add("theme_id = ?");
            params.add(reservationSearchFilter.themeId());
        }

        if (reservationSearchFilter.memberId() != null) {
            clauses.add("member_id = ?");
            params.add(reservationSearchFilter.memberId());
        }

        if (reservationSearchFilter.startDate() != null) {
            clauses.add("r.date >= ?");
            params.add(reservationSearchFilter.startDate());
        }

        if (reservationSearchFilter.endDate() != null) {
            clauses.add("r.date <= ?");
            params.add(reservationSearchFilter.endDate());
        }

        if (clauses.isEmpty()) {
            return null;
        }
        String clause = "WHERE " + String.join(" AND ", clauses);
        return new WhereClause(params.toArray(), clause);
    }

    private static class WhereClause {
        private final Object[] params;
        private final String clause;

        public WhereClause(Object[] params, String clause) {
            this.params = params;
            this.clause = clause;
        }
    }
}
