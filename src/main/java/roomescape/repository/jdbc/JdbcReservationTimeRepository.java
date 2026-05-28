package roomescape.repository.jdbc;

import static roomescape.repository.jdbc.ReservationTimeEntityMapper.RESERVATION_TIME_MAPPER;
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
import roomescape.domain.TimeStatus;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
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

    @Override
    public Optional<ReservationTime> findById(long id) {
        try {
            String sql = "SELECT * FROM reservation_time WHERE id = ?";
            ReservationTime time = jdbcTemplate.queryForObject(sql, RESERVATION_TIME_MAPPER, id);
            return Optional.ofNullable(time);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByStartAt(LocalTime time) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, time);
        return Boolean.TRUE.equals(result);
    }

    @Override
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
