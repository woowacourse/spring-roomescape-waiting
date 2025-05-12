package roomescape.reservation.dao;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.exception.AssociatedReservationExistsException;
import roomescape.reservation.exception.TimeNotExistException;

@Repository
public class JdbcReservationTimeDao implements ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation_time")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime add(ReservationTime reservationTime) {
        Map<String, Object> param = new HashMap<>();
        param.put("start_at", reservationTime.getStartAt());

        Number key = jdbcInsert.executeAndReturnKey(param);
        return reservationTime.toEntity(key.longValue());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = generateFindAllQuery();
        return jdbcTemplate.query(sql, mapResultsToReservationTime());
    }

    private String generateFindAllQuery() {
        return "SELECT id, start_at FROM reservation_time";
    }

    @Override
    public ReservationTime findById(Long id) {
        String whereClause = " WHERE id = ?";
        String sql = generateFindAllQuery() + whereClause;
        try {
            return jdbcTemplate.queryForObject(sql, mapResultsToReservationTime(), id);
        } catch (DataAccessException e) {
            throw new TimeNotExistException();
        }
    }

    @Override
    public int deleteById(Long id) {
        try {
            String sql = "DELETE FROM reservation_time WHERE id = ?";
            return jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException exception) {
            throw new AssociatedReservationExistsException("해당 시간에 이미 저장된 예약이 있으므로 삭제할 수 없다.");
        }
    }

    @Override
    public boolean existByStartAt(LocalTime startAt) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation_time WHERE start_at = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, startAt);
    }

    private RowMapper<ReservationTime> mapResultsToReservationTime() {
        return (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime());
    }
}
