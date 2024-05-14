package roomescape.reservation.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.model.Reservation;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.theme.model.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;

    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<Reservation> reservationRowMapper =
            (resultSet, rowNum) -> new Reservation(
                    resultSet.getLong("id"),
                    new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("name"),
                            MemberRole.getMemberRole(resultSet.getString("role")),
                            resultSet.getString("email"),
                            resultSet.getString("password")),
                    resultSet.getDate("date").toLocalDate(),
                    new ReservationTime(
                            resultSet.getLong("time_id"),
                            resultSet.getTime("start_at").toLocalTime()),
                    new Theme(resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("description"),
                            resultSet.getString("thumbnail")
                    )
            );

    @Override
    public Reservation save(final Reservation reservation) {
        SqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("id", reservation.getMember().getId())
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", Date.valueOf(reservation.getDate()))
                .addValue("time_id", reservation.getReservationTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());

        long id = simpleJdbcInsert.executeAndReturnKey(mapSqlParameterSource).longValue();

        return new Reservation(
                id,
                reservation.getMember(),
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                select r.id, 
                        m.id as member_id, m.name, m.email, m.password, m.role,
                        r.date,
                        rt.id as time_id, rt.start_at, 
                        t.id as theme_id, t.name as theme_name, t.description, t.thumbnail 
                from reservation as r 
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id 
                inner join member as m 
                on r.theme_id = m.id 
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        String sql = """
                select r.id, 
                        m.id as member_id, m.name, m.email, m.password, m.role,
                        r.date,  
                        rt.id as time_id, rt.start_at, 
                        t.id as theme_id, t.name as theme_name, t.description, t.thumbnail
                from reservation as r 
                inner join reservation_time as rt 
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id 
                inner join member as m 
                on r.theme_id = m.id 
                where r.id = ?
                limit 1
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reservationRowMapper, id));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final Long themeId) {
        String sql = """
                select r.id, 
                    m.id as member_id, m.name, m.email, m.password, m.role,
                    r.date, 
                    rt.id as time_id, rt.start_at,
                    t.id as theme_id, t.name as theme_name, t.description, t.thumbnail
                from reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id 
                inner join member as m 
                on r.theme_id = m.id 
                where r.date = ? and r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, date, themeId);
    }

    @Override
    public List<Reservation> searchBy(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                      final LocalDate dateTo) {
        String sql = """
                select r.id,
                    m.id as member_id, m.name, m.email, m.password, m.role,
                    r.date,
                    rt.id as time_id, rt.start_at,
                    t.id as theme_id, t.name as theme_name, t.description, t.thumbnail
                from reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id
                inner join member as m
                on r.member_id = m.id
                where 1 = 1                           
                """;

        List<Object> params = new ArrayList<>();

        if (themeId != null) {
            sql += " AND theme_id = ?";
            params.add(themeId);
        }
        if (memberId != null) {
            sql += " AND member_id = ?";
            params.add(memberId);
        }
        if (dateFrom != null) {
            sql += " AND date >= ?";
            params.add(dateFrom);
        }
        if (dateTo != null) {
            sql += " AND date <= ?";
            params.add(dateTo);
        }

        return jdbcTemplate.query(sql, params.toArray(), reservationRowMapper);
    }

    @Override
    public boolean existsById(final Long id) {
        String sql = """
                select count(*)
                from reservation as r
                where r.id = ? 
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) != 0;
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        String sql = """
                select exists ( select 1 
                from reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                where r.time_id = ? )
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) != 0;
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        String sql = """
                select exists ( select 1 
                from reservation as r
                inner join theme as t
                on r.theme_id = t.id 
                where r.theme_id = ? )
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) != 0;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        String sql = """
                select exists ( select 1 
                from reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id 
                where r.date = ? and r.time_id = ? and r.theme_id = ? )
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId) != 0;
    }

    @Override
    public void deleteById(final Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
