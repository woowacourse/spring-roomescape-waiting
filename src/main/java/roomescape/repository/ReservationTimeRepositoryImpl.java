package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JdbcTemplate template;

    public ReservationTimeRepositoryImpl(final JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        final List<ReservationTime> reservationTimes = template.query(sql, reservationTimeRowMapper(), id);
        if (reservationTimes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reservationTimes.getFirst());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "select * from reservation_time";
        return template.query(sql, reservationTimeRowMapper());
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        String sql = "insert into reservation_time (start_at) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationTime.getStartAt().toString());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservationTime.toEntity(id);
    }

    @Override
    public int deleteById(final long id) {
        try {
            String sql = "delete from reservation_time where id = ?";
            return template.update(sql, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("예약 시간을 지울 수 없습니다.");
        }
    }

    @Override
    public boolean existsByStartAt(final String startAt) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";
        return Boolean.TRUE.equals(template.queryForObject(sql, Boolean.class, startAt));
    }

    private RowMapper<ReservationTime> reservationTimeRowMapper() {
        return (rs, rowNum) -> {
            return new ReservationTime(
                    rs.getLong("id"),
                    rs.getString("start_at")
            );
        };
    }
}
