package roomescape.repository.reservationmember.legacy;

import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMember;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservationmember.ReservationMemberRepository;

@Repository
public class JdbcReservationMemberRepository implements ReservationMemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationMember> reservationMemberRowMapper = (rs, rowNum) -> {
        // ReservationTime
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time").toLocalTime()
        );

        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("theme_thumbnail")
        );

        Reservation reservation = new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                rs.getDate("reservation_date").toLocalDate(),
                time,
                theme
        );

        Member member = new Member(
                rs.getLong("member_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("member_name"),
                Role.valueOf(rs.getString("role"))
        );

        return new ReservationMember(
                rs.getLong("id"),
                reservation,
                member
        );
    };

    public JdbcReservationMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationMember> findAll() {
        String sql = """
                    SELECT rm.id,
                           r.id AS reservation_id, r.name AS reservation_name, r.date AS reservation_date,
                           t.id AS time_id, t.time,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                           m.id AS member_id, m.username, m.password, m.role, m.name AS member_name
                    FROM reservation_member rm
                    JOIN reservation r ON rm.reservation_id = r.id
                    JOIN reservation_time t ON r.time_id = t.id
                    JOIN theme th ON r.theme_id = th.id
                    JOIN member m ON rm.member_id = m.id
                """;
        return jdbcTemplate.query(sql, reservationMemberRowMapper);
    }

    @Override
    public long add(Reservation reservation, Member member) {
        String sql = "INSERT INTO reservation_member (reservation_id, member_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getId());
            ps.setLong(2, member.getId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteById(long id) {
        ReservationMember reservationMember = findReservationMemberById(id);

        String deleteReservationMemberSql = "DELETE FROM reservation_member WHERE id = ?";
        jdbcTemplate.update(deleteReservationMemberSql, id);

        Long reservationId = reservationMember.getReservationId();
        deleteReservationById(reservationId);
    }

    private void deleteReservationById(Long reservationId) {
        String sql = "delete from reservation where id=?";
        jdbcTemplate.update(sql, reservationId);
    }

    private ReservationMember findReservationMemberById(long id) {
        String findByIdSql = """
                    SELECT rm.id,
                           r.id AS reservation_id, r.name AS reservation_name, r.date AS reservation_date,
                           t.id AS time_id, t.time,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                           m.id AS member_id, m.username, m.password, m.role, m.name AS member_name
                    FROM reservation_member rm
                    JOIN reservation r ON rm.reservation_id = r.id
                    JOIN reservation_time t ON r.time_id = t.id
                    JOIN theme th ON r.theme_id = th.id
                    JOIN member m ON rm.member_id = m.id
                    where rm.id = ?
                """;

        return jdbcTemplate.queryForObject(findByIdSql, this.reservationMemberRowMapper, id);
    }

    @Override
    public List<ReservationMember> findAllByMemberId(Long memberId) {
        String sql = """
                    SELECT rm.id,
                           r.id AS reservation_id, r.name AS reservation_name, r.date AS reservation_date,
                           t.id AS time_id, t.time,
                           th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                           m.id AS member_id, m.username, m.password, m.role, m.name AS member_name
                    FROM reservation_member rm
                    JOIN reservation r ON rm.reservation_id = r.id
                    JOIN reservation_time t ON r.time_id = t.id
                    JOIN theme th ON r.theme_id = th.id
                    JOIN member m ON rm.member_id = m.id
                    WHERE m.id = ?
                """;
        return jdbcTemplate.query(sql, reservationMemberRowMapper, memberId);
    }
}
