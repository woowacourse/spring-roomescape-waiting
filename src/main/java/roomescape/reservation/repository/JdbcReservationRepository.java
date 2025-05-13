package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        final LocalTime time = LocalTime.parse(resultSet.getString("time_value"), timeFormatter);
        return new Reservation(
                resultSet.getLong("id"),
                resultSet.getObject("date", LocalDate.class),
                new ReservationTime(resultSet.getLong("time_id"), time),
                new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"),
                        resultSet.getString("theme_description"), resultSet.getString("theme_thumbnail")),
                new Member(resultSet.getLong("member_id"), resultSet.getString("member_name"),
                        resultSet.getString("member_email"), resultSet.getString("member_password"),
                        resultSet.getString("member_role"))
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("reservation")
                .usingGeneratedKeyColumns("id")
                .usingColumns("date", "time_id", "theme_id", "member_id");
    }

    @Override
    public List<Reservation> findByCriteria(final Long themeId, final Long memberId, final LocalDate fromDate,
                                            final LocalDate toDate) {
        final String sql = """
                select
                    r.id, 
                    r.date, 
                    t.id as time_id, 
                    t.start_at as time_value,
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description as theme_description,
                    th.thumbnail as theme_thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role
                from reservation as r
                inner join reservation_time as t 
                on r.time_id = t.id
                inner join theme as th
                on r.theme_id = th.id
                inner join member as m 
                on r.member_id = m.id
                WHERE (:themeId   IS NULL OR r.theme_id = :themeId)
                AND (:memberId  IS NULL OR r.member_id = :memberId)
                AND (:fromDate  IS NULL OR r.date     >= :fromDate)
                AND (:toDate    IS NULL OR r.date     <= :toDate)
                """;

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("memberId", memberId)
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);
        return namedParameterJdbcTemplate.query(sql, params, RESERVATION_ROW_MAPPER);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final LocalTime time, final Long themeId) {
        final String sql = """
                select exists(
                    select 1
                    from reservation r
                    inner join reservation_time rt
                    on r.time_id = rt.id
                    where r.date = ? 
                        and rt.start_at = ?
                        and r.theme_id = ?
                );
                """;
        final Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, time, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByReservationTimeId(final Long reservationTimeId) {
        final String sql = """
                        select exists (
                            select 1
                            from reservation as r 
                            where r.time_id = ?
                        );
                """;
        final Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, reservationTimeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByThemeId(final Long themeId) {
        final String sql = """
                        select exists (
                            select 1
                            from reservation as r 
                            where r.theme_id = ?
                        );
                """;
        final Boolean count = jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
        return Boolean.TRUE.equals(count);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("member_id", reservation.getMember().getId());

        final long newId = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(newId,
                reservation.getDate(), reservation.getTime(), reservation.getTheme(), reservation.getMember());
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        final String sql = """
                select 
                    r.id, 
                    r.date, 
                    t.id as time_id, 
                    t.start_at as time_value,
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description as theme_description,
                    th.thumbnail as theme_thumbnail,
                     m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role
                from reservation as r
                inner join reservation_time as t 
                on r.time_id = t.id
                inner join theme as th
                on r.theme_id = th.id
                inner join member as m
                on r.member_id = m.id
                where r.id = ?
                """;
        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
