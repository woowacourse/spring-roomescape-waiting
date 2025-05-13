package roomescape.reservationTime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.common.KeyHolderManager;
import roomescape.common.RowMapperManager;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.exception.NotFoundReservationTimeException;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final KeyHolderManager keyHolderManager;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate, KeyHolderManager keyHolderManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.keyHolderManager = keyHolderManager;
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "select id AS reservation_time_id, start_at AS reservation_time_start_at from reservation_time",
                RowMapperManager.reservationTimeRowMapper);
    }

    @Override
    public ReservationTime findByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundReservationTimeException("해당 예약시간 id가 존재하지 않습니다."));
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "select id AS reservation_time_id, start_at AS reservation_time_start_at from reservation_time where id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RowMapperManager.reservationTimeRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationTime add(ReservationTime reservationTime) {
        Long id = insertWithKeyHolder(reservationTime);
        return findByIdOrThrow(id);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation_time where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Long insertWithKeyHolder(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (?)";
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//
//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(
//                    sql,
//                    new String[]{"id"});
//
//            return ps;
//        }, keyHolder);
//
//        return keyHolder.getKey().longValue();

        return keyHolderManager.insertAndReturnId(sql,
                ps -> {
                    ps.setString(1, reservationTime.getStartAt().toString());
                }
        );
    }

    @Override
    public boolean existsByReservationTime(LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }
}
