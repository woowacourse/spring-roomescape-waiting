package roomescape.reservation.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.auth.Role;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.ReservationSearchCondRequest;

@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(Reservation reservation) {
        String sql = "insert into reservation (member_id, date, theme_id, time_id) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    sql, new String[]{"id"}
            );
            ps.setLong(1, reservation.getMember().getId());
            ps.setString(2, String.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getTheme().getId());
            ps.setLong(4, reservation.getTime().getId());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<Reservation> findById(Long id) {
        String sql = """
                select
                r.id,
                m.id as member_id,
                m.role,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail,
                rt.id as time_id,
                rt.start_at
                from reservation r
                join reservation_time rt
                on r.time_id = rt.id
                join theme t
                on r.theme_id = t.id
                join member m
                on r.member_id = m.id
                where r.id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, createReservationRowMapper(), id));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Long> findTimeIdsByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                select time_id
                from reservation r
                where r.date = ? and r.theme_id = ?
                """;

        return jdbcTemplate.queryForList(sql, Long.class, date, themeId);
    }

    public List<Reservation> findAll() {
        String sql = """
                select
                r.id,
                m.id as member_id,
                m.role,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail,
                rt.id as time_id,
                rt.start_at
                from reservation r
                join reservation_time rt
                on r.time_id = rt.id
                join theme t
                on r.theme_id = t.id
                join member m
                on r.member_id = m.id
                """;

        return jdbcTemplate.query(sql, createReservationRowMapper());
    }

    public List<Reservation> findAllByThemeIdAndMemberIdAndBetweenStartDateAndEndDate(
            ReservationSearchCondRequest reservationSearchCondRequest) {
        String sql = """
                select
                r.id,
                m.id as member_id,
                m.role,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date,
                t.id as theme_id,
                t.name as theme_name,
                t.description,
                t.thumbnail,
                rt.id as time_id,
                rt.start_at
                from reservation r
                join reservation_time rt
                on r.time_id = rt.id
                join theme t
                on r.theme_id = t.id
                join member m
                on r.member_id = m.id
                where theme_id = ? and member_id = ? and r.date BETWEEN ? AND ?
                """;

        return jdbcTemplate.query(
                sql,
                createReservationRowMapper(),
                reservationSearchCondRequest.themeId(),
                reservationSearchCondRequest.memberId(),
                reservationSearchCondRequest.dateFrom(),
                reservationSearchCondRequest.dateTo()
        );
    }

    public boolean existReservation(Reservation reservation) {
        String sql = """
                select exists (select 1
                from reservation r
                join reservation_time t on r.time_id = t.id where r.date = ? and t.start_at = ?)
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString());
    }

    public void delete(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    private RowMapper<Reservation> createReservationRowMapper() {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("id"),
                new Member(
                        rs.getLong("member_id"),
                        Role.from(rs.getString("role")),
                        new MemberName(rs.getString("member_name")),
                        rs.getString("member_email"),
                        rs.getString("member_password")
                ),
                rs.getDate("date").toLocalDate(),
                new Theme(
                        rs.getLong("theme_id"),
                        new ThemeName(rs.getString("theme_name")),
                        new Description(rs.getString("description")),
                        rs.getString("thumbnail")
                ),
                new ReservationTime(
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime()
                )
        );
    }
}
