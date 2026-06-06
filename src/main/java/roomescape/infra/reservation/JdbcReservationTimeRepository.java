package roomescape.infra.reservation;

import java.sql.Time;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final String TABLE_NAME = "reservation_time";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_START_AT = "start_at";
    private static final String FIND_ALL_SQL = "select id, start_at from reservation_time order by id";
    private static final String DELETE_BY_ID_SQL = "delete from reservation_time where id = :id";

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) -> ReservationTime.of(
            rs.getLong(COLUMN_ID),
            rs.getTime(COLUMN_START_AT).toLocalTime()
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationTimeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue(COLUMN_START_AT, Time.valueOf(reservationTime.getStartAt())));
        return ReservationTime.of(extractId(key), reservationTime.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new MapSqlParameterSource(), RESERVATION_TIME_ROW_MAPPER);
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, new MapSqlParameterSource().addValue(COLUMN_ID, id));
    }

    private long extractId(Number key) {
        if (key == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return key.longValue();
    }
}
