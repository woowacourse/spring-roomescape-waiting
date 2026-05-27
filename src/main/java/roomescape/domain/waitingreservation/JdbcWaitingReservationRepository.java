package roomescape.domain.waitingreservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingReservationRepository implements WaitingReservationRepository {

    private static final String INSERT_SQL = "insert into waiting_reservation(name, date_id, time_id, theme_id, created_at) values (?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public WaitingReservation save(WaitingReservation waitingReservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, waitingReservation.getName());
            ps.setLong(2, waitingReservation.getDate().getId());
            ps.setLong(3, waitingReservation.getTime().getId());
            ps.setLong(4, waitingReservation.getTheme().getId());
            ps.setTimestamp(5, Timestamp.valueOf(waitingReservation.getCreatedAt()));
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return WaitingReservation.of(
                id,
                waitingReservation.getName(),
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme(),
                waitingReservation.getCreatedAt()
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
