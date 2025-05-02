package roomescape.repository.reservationmember;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationmember.ReservationMemberIds;

@Repository
public class JdbcReservationMemberRepository implements ReservationMemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ReservationMemberIds> reservationMemberIdRowMapper = (resultSet, rowNum) -> {
        long reservationId = resultSet.getLong("reservation_id");
        long memberId = resultSet.getLong("member_id");
        long id = resultSet.getLong("id");
        return new ReservationMemberIds(id, reservationId, memberId);
    };

    public JdbcReservationMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationMemberIds> findAll() {
        String sql = "select id,reservation_id,member_id from reservation_member";
        return jdbcTemplate.query(sql, reservationMemberIdRowMapper);
    }

    @Override
    public Optional<ReservationMemberIds> findById(long id) {
        try {
            String sql = "select id,reservation_id,member_id from reservation_member where id = ?";
            return Optional.of(jdbcTemplate.queryForObject(sql, reservationMemberIdRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public long add(Reservation reservation, Member member) {
        String sql = "insert into reservation_member (reservation_id,member_id) values(?,?)";

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
        String sql = "delete from reservation_member where id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<ReservationMemberIds> findAllByMemberId(Long memberId) {
        String sql = "select id,reservation_id,member_id from reservation_member where member_id = ?";
        return jdbcTemplate.query(sql, reservationMemberIdRowMapper, memberId);
    }
}
