package roomescape.domain.reservationdate;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
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
public class JdbcReservationDateRepository implements ReservationDateRepository {

    private static final String INSERT_SQL = "insert into reservation_date(play_day) values (?)";
    private static final String FIND_BY_ID_SQL = "select id, play_day from reservation_date where id = ?";
    private static final String FIND_ALL_SQL = "select id, play_day from reservation_date order by id";
    private static final String DELETE_BY_ID_SQL = "delete from reservation_date where id = ?";
    private static final String EXISTS_BY_PLAY_DAY_SQL =
        """
            select exists(
            select 1
            from reservation_date
            where play_day = ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ReservationDate> findById(Long id) {
        List<ReservationDate> result = jdbcTemplate.query(FIND_BY_ID_SQL, reservationDateRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public List<ReservationDate> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, reservationDateRowMapper());
    }

    @Override
    public ReservationDate save(ReservationDate reservationDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reservationDate.getPlayDay().toString());
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return ReservationDate.of(
            id,
            reservationDate.getPlayDay()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public boolean existsByPlayDay(LocalDate playDay) {
        return jdbcTemplate.queryForObject(EXISTS_BY_PLAY_DAY_SQL, Boolean.class, playDay);
    }

    private RowMapper<ReservationDate> reservationDateRowMapper() {
        return (rs, rowNum) -> ReservationDate.of(
            rs.getLong("id"),
            LocalDate.parse(rs.getString("play_day"))
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
