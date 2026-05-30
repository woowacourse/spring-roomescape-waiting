package roomescape.reservation.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;

@Component
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime insert(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        Long id = simpleJdbcInsert.executeAndReturnKey(Map.of(
                "start_at", reservationTimeCreateRequest.startAt()
        )).longValue();
        return ReservationTime.of(id, reservationTimeCreateRequest.startAt());
    }

    public List<ReservationTime> findAll() {
        String sql = "select id, start_at from reservation_time";
        RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> ReservationTime.of(
                resultSet.getLong("id"),
                LocalTime.parse(resultSet.getString("start_at"))
        );

        return jdbcTemplate.query(sql, rowMapper);
    }

    public ReservationTime findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "select id, start_at from reservation_time where id = ?",
                    (resultSet, rowNum) -> ReservationTime.of(resultSet.getLong("id"),
                            LocalTime.parse(resultSet.getString("start_at"))),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 예약 시간입니다.");
        }
    }

    public void delete(Long id) {
        jdbcTemplate.update("delete from reservation_time where id = ?", id);
    }
}

