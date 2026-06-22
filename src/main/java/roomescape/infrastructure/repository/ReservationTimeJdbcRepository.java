package roomescape.infrastructure.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.exception.ConflictException;

@Repository
public class ReservationTimeJdbcRepository implements ReservationTimeRepository {

    private static final String ALREADY_EXISTS_TIME = "이미 존재하는 예약 시간입니다.";
    private static final String CANNOT_DELETE_TIME_IN_USE = "ID %d번 시간을 사용 중인 예약이 존재하여 시간을 삭제할 수 없습니다.";

    private final RowMapper<ReservationTime> timeRowMapper = (rs, rowNum) ->
            new ReservationTime(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public ReservationTimeJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time ORDER BY start_at";
        return jdbcTemplate.query(sql, timeRowMapper);
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time (start_at) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, time.getStartAt().toString());
                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ALREADY_EXISTS_TIME, e);
        }

        long id = keyHolder.getKey().longValue();
        return new ReservationTime(
                id,
                time.getStartAt()
        );
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(String.format(CANNOT_DELETE_TIME_IN_USE, id), e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM reservation_time WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        List<ReservationTime> results = jdbcTemplate.query(sql, timeRowMapper, id);
        return results.stream().findFirst();
    }
}
