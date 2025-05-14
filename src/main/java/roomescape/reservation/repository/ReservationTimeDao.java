package roomescape.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roomescape.common.template.AbstractDaoTemplate;
import roomescape.reservation.domain.ReservationTime;

@Repository
public class ReservationTimeDao extends AbstractDaoTemplate<ReservationTime> {

    private static final String TABLE_NAME = "reservation_time";
    private static final String BASE_SELECT_SQL = "select id, start_at from " + TABLE_NAME;

    @Autowired
    public ReservationTimeDao(final NamedParameterJdbcTemplate jdbcTemplate, final DataSource dataSource) {
        super(jdbcTemplate, TABLE_NAME, dataSource);
    }

    @Override
    protected RowMapper<ReservationTime> rowMapper() {
        return this::mapRowToReservationTime;
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, rowMapper());
    }

    public Optional<ReservationTime> findById(final Long id) {
        String sql = BASE_SELECT_SQL + " where id = :id";
        return executeQueryForObject(sql, Map.of("id", id));
    }

    public ReservationTime save(final ReservationTime reservationTime) {
        Map<String, Object> params = Map.of("start_at", reservationTime.getStartAt());
        long timeId = jdbcInsert.executeAndReturnKey(params).longValue();
        return new ReservationTime(timeId, reservationTime.getStartAt());
    }

    public boolean existsByStartAt(final LocalTime startAt) {
        return existsBy("start_at", startAt);
    }

    private ReservationTime mapRowToReservationTime(final ResultSet resultSet, final int rowNum) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("id"),
                LocalTime.parse(resultSet.getString("start_at"))
        );
    }
}

