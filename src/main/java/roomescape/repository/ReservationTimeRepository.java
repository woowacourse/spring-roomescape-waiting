package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.exception.BadRequestException;

@Repository
public class ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("SELECT * FROM reservation_time",
                (rs, rowNum) -> new ReservationTime(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime()
                ));
    }

    public Optional<ReservationTime> findById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM reservation_time WHERE id = ?",
                    (rs, rowNum) -> new ReservationTime(
                            rs.getLong("id"),
                            rs.getTime("start_at").toLocalTime()
                    ), id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existByStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException("시간이 빈값일 수 없습니다.");
        }
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT * FROM reservation_time WHERE start_at = ?)",
                Boolean.class, startAt));
    }

    public ReservationTime save(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new BadRequestException("시간이 빈값일 수 없습니다.");
        }

        SqlParameterSource params = new MapSqlParameterSource("start_at",
                reservationTime.getStartAt());
        Long id = simpleJdbcInsert.executeAndReturnKey(params)
                .longValue();

        return reservationTime.withId(id);
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }
        return jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id) > 0;
    }
}
