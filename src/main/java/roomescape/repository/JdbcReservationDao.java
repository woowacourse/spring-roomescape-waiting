package roomescape.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationDao implements ReservationRepository {

    private static final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        String date = rs.getString("date");
        Long timeId = rs.getLong("reservation_time_id");
        String timeValue = rs.getString("start_at");
        String roleName = rs.getString("auth_role");
        ReservationTime reservationTime = new ReservationTime(timeId, LocalTime.parse(timeValue));
        Theme theme = new Theme(
                rs.getLong("reservation_theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail")
        );

        Member member = new Member(
                rs.getLong("reservation_member_id"),
                rs.getString("member_name"),
                rs.getString("email"),
                Role.valueOf(roleName),
                rs.getString("password")
        );

        Reservation reservation = new Reservation(
                rs.getLong("id"),
                member,
                LocalDate.parse(date),
                reservationTime,
                theme
        );

        return reservation;
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationDao(final JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(final Reservation reservation) {
        try {
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("date", reservation.getDate())
                    .addValue("time_id", reservation.getTime().id())
                    .addValue("theme_id", reservation.getTheme().id())
                    .addValue("member_id", reservation.getMember().getId());

            long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return new Reservation(id, reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("[ERROR] 이미 등록된 예약 입니다.");
        }
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                r.id,
                r.date,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                r.member_id as reservation_member_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail,
                m.name as member_name,
                m.email,
                m.auth_role,
                m.password
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id 
                inner join member as m on r.member_id = m.id
                inner join theme as th on r.theme_id = th.id
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        String sql = """
                SELECT
                r.id,
                r.date,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                r.member_id as reservation_member_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail,
                m.name as member_name,
                m.email,
                m.auth_role,
                m.password
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id 
                inner join member as m on r.member_id = m.id
                inner join theme as th on r.theme_id = th.id
                where r.id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByDateTimeAndThemeId(LocalDate date, LocalTime time, long themeId) {
        String sql = """
                SELECT
                r.id,
                r.date,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                r.member_id as reservation_member_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail,
                m.name as member_name,
                m.email,
                m.auth_role,
                m.password
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id 
                inner join member as m on r.member_id = m.id
                inner join theme as th on r.theme_id = th.id
                where r.date = ? and t.start_at = ? and r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, date, time, themeId);
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, long themeId) {
        String sql = """
                SELECT
                r.id,
                r.date,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                r.member_id as reservation_member_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail,
                m.name as member_name,
                m.email,
                m.auth_role,
                m.password
                FROM reservation as r
                inner join reservation_time as t on r.time_id = t.id 
                inner join member as m on r.member_id = m.id
                inner join theme as th on r.theme_id = th.id
                WHERE r.date = ? AND th.id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, date, themeId);
    }

    @Override
    public List<Reservation> findReservationsByPeriodAndMemberAndTheme(long themeId, long memberId, LocalDate from, LocalDate to) {
        String sql = """
                SELECT
                r.id,
                r.date,
                r.time_id as reservation_time_id,
                r.theme_id as reservation_theme_id,
                r.member_id as reservation_member_id,
                t.start_at,
                th.name as theme_name,
                th.description,
                th.thumbnail,
                m.name as member_name,
                m.email,
                m.auth_role,
                m.password
                FROM reservation as r
                INNER JOIN reservation_time as t on r.time_id = t.id
                INNER JOIN theme as th on r.theme_id = th.id
                INNER JOIN member as m on r.member_id = m.id
                WHERE r.date >= ? AND r.date <= ? AND th.id = ? AND m.id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, from, to, themeId, memberId);
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public int deleteById(final long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
