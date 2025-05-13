package roomescape.reservationTime.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTime> ROW_MAPPER = (resultSet, rowNum) -> ReservationTime.createWithId(
            resultSet.getLong("id"),
            resultSet.getTime("start_at").toLocalTime()
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationTimeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final ReservationTime reservationTime) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start_at", reservationTime.getStartAt());

        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public boolean deleteById(final Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = ?";
        int count = jdbcTemplate.update(sql, id);

        return count != 0;
    }

    @Override
    public ReservationTime findById(final Long id) {
        String sql = "SELECT * FROM reservation_time WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }
}
