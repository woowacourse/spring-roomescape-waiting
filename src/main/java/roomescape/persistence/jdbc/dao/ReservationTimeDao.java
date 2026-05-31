package roomescape.persistence.jdbc.dao;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.persistence.jdbc.mapper.ReservationTimeRowMapper;
import roomescape.persistence.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class ReservationTimeDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)";

        RepositoryExceptionTranslator.execute(() -> {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setTime(1, Time.valueOf(reservationTime.getStartAt()));
                ps.setString(2, reservationTime.getStatus().toString());
                return ps;
            }, keyHolder);
        }, "이미 존재하는 시간 정보입니다.");

        return new ReservationTime(
                keyHolder.getKey().longValue(),
                reservationTime.getStartAt(),
                reservationTime.getStatus()
        );
    }

    public Optional<ReservationTime> findById(long id) {
        try {
            String sql = "SELECT * FROM reservation_time WHERE id = ?";
            ReservationTime time = jdbcTemplate.queryForObject(
                    sql,
                    ReservationTimeRowMapper.RESERVATION_TIME_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByStartAt(LocalTime time) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, time);
        return Boolean.TRUE.equals(result);
    }

    public void update(ReservationTime time) {
        String sql = """
                    UPDATE reservation_time
                    SET start_at = ?, status = ?
                    WHERE id = ?
                """;
        RepositoryExceptionTranslator.execute(
                () -> jdbcTemplate.update(sql, time.getStartAt(), time.getStatus().toString(), time.getId()),
                "이미 존재하는 시간 정보입니다.");
    }
}
