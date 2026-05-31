package roomescape.domain.reservationtime;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final String INSERT_SQL = "insert into reservation_time(start_at) values (?)";
    private static final String FIND_ALL_SQL = "select id, start_at from reservation_time order by id";
    private static final String FIND_BY_ID_SQL = "select id, start_at from reservation_time where id = ?";
    private static final String DELETE_BY_ID_SQL = "delete from reservation_time where id = ?";
    private static final String EXISTS_BY_START_AT_SQL =
        """
            select exists(
            select 1
            from reservation_time
            where start_at = ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reservationTime.getFormattedStartAt());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return ReservationTime.of(
            id,
            reservationTime.getStartAt()
        );
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, reservationTimeRowMapper());
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        List<ReservationTime> result = jdbcTemplate.query(FIND_BY_ID_SQL, reservationTimeRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return jdbcTemplate.queryForObject(EXISTS_BY_START_AT_SQL, Boolean.class, startAt);
    }

    private RowMapper<ReservationTime> reservationTimeRowMapper() {
        return (rs, rowNum) -> ReservationTime.of(
            rs.getLong("id"),
            LocalTime.parse(rs.getString("start_at"))
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
