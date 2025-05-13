package roomescape.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.*;
import roomescape.persistence.query.CreateReservationQuery;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    new Member(rs.getLong("member_id"), rs.getString("member_name"), MemberRole.of(rs.getString("member_role")), rs.getString("member_email"), rs.getString("member_password")),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(rs.getLong("time_id"), rs.getTime("time_value").toLocalTime()),
                    new Theme(rs.getLong("theme_id"), rs.getString("theme_name"), rs.getString("theme_description"), rs.getString("theme_thumbnail"))
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query("""
                        SELECT  
                            r.id as reservation_id,
                            r.date, 
                            t.id as time_id, 
                            t.start_at as time_value,
                            tm.id as theme_id, 
                            tm.name as theme_name, 
                            tm.description as theme_description, 
                            tm.thumbnail as theme_thumbnail,
                            m.id as member_id,
                            m.role as member_role,
                            m.name as member_name,
                            m.email as member_email,
                            m.password as member_password
                        FROM reservation as r
                            inner join reservation_time as t on r.time_id = t.id
                            inner join theme as tm on r.theme_id = tm.id
                            inner join member as m on r.member_id = m.id
                        """,
                reservationRowMapper);
    }

    @Override
    public Long create(CreateReservationQuery createReservationQuery) {
        String sql = "INSERT INTO reservation(member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, createReservationQuery.member().getId());
            ps.setString(2, createReservationQuery.date().toString());
            ps.setLong(3, createReservationQuery.time().id());
            ps.setLong(4, createReservationQuery.theme().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(Long reservationId) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", reservationId);
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        try {
            Reservation reservation = jdbcTemplate.queryForObject(
                        """
                        SELECT  
                            r.id as reservation_id,
                            r.date, 
                            t.id as time_id, 
                            t.start_at as time_value,
                            tm.id as theme_id, 
                            tm.name as theme_name, 
                            tm.description as theme_description, 
                            tm.thumbnail as theme_thumbnail,
                            m.id as member_id,
                            m.role as member_role,
                            m.name as member_name,
                            m.email as member_email,
                            m.password as member_password
                        FROM reservation as r
                            inner join reservation_time as t on r.time_id = t.id
                            inner join theme as tm on r.theme_id = tm.id
                            inner join member as m on r.member_id = m.id
                        WHERE r.id = ?
                        """,
                    reservationRowMapper, reservationId);
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByTimeId(final Long reservationTimeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, reservationTimeId) > 0;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate reservationDate, final Long timeId, final Long themeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, reservationDate, timeId, themeId) > 0;
    }

    @Override
    public boolean existsByThemeId(final Long themeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, themeId) > 0;
    }

    @Override
    public List<Reservation> findByThemeIdAndReservationDate(final Long themeId, final LocalDate reservationDate) {
        String sql = """
                        SELECT  
                            r.id as reservation_id,
                            r.date, 
                            t.id as time_id, 
                            t.start_at as time_value,
                            tm.id as theme_id, 
                            tm.name as theme_name, 
                            tm.description as theme_description, 
                            tm.thumbnail as theme_thumbnail,
                            m.id as member_id,
                            m.role as member_role,
                            m.name as member_name,
                            m.email as member_email,
                            m.password as member_password
                        FROM reservation as r
                            inner join reservation_time as t on r.time_id = t.id
                            inner join theme as tm on r.theme_id = tm.id
                            inner join member as m on r.member_id = m.id
                        WHERE r.theme_id = ? AND r.date = ?
                        """;
        return jdbcTemplate.query(sql, reservationRowMapper, themeId, reservationDate);
    }

    @Override
    public List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo) {
        String baseSelectSql = """
                        SELECT
                            r.id as reservation_id,
                            r.date,
                            t.id as time_id,
                            t.start_at as time_value,
                            tm.id as theme_id,
                            tm.name as theme_name,
                            tm.description as theme_description,
                            tm.thumbnail as theme_thumbnail,
                            m.id as member_id,
                            m.role as member_role,
                            m.name as member_name,
                            m.email as member_email,
                            m.password as member_password
                        FROM reservation as r
                            inner join reservation_time as t on r.time_id = t.id
                            inner join theme as tm on r.theme_id = tm.id
                            inner join member as m on r.member_id = m.id
                        """;

        StringBuilder sql = new StringBuilder(baseSelectSql);
        List<Object> parameters = new ArrayList<>();
        boolean isFirstCondition = true;

        if (memberId != null || themeId != null || dateFrom != null || dateTo != null) {
            sql.append(" WHERE");
        }

        if (memberId != null) {
            sql.append(" r.member_id = ?");
            parameters.add(memberId);
            isFirstCondition = false;
        }

        if (themeId != null) {
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.theme_id = ?");
            parameters.add(themeId);
            isFirstCondition = false;
        }

        if (dateFrom != null) {
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.date >= ?");
            parameters.add(dateFrom);
            isFirstCondition = false;
        }

        if (dateTo != null) {
            appendAndIfNotFirstCondition(isFirstCondition, sql);
            sql.append(" r.date <= ?");
            parameters.add(dateTo);
        }

        sql.append(" ORDER BY r.id");

        return jdbcTemplate.query(sql.toString(), reservationRowMapper, parameters.toArray());
    }

    private void appendAndIfNotFirstCondition(final boolean isFirstCondition, final StringBuilder sql) {
        if (!isFirstCondition) {
            sql.append(" AND");
        }
    }
}
