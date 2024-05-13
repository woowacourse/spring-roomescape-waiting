package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.service.exception.TimeNotFoundException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class H2ReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public H2ReservationTimeRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("RESERVATION_TIME")
                .usingGeneratedKeyColumns("ID");
    }

    private ReservationTime mapRowTime(final ResultSet rs, final int rowNum) throws SQLException {
        return new ReservationTime(
                rs.getLong("ID"),
                LocalTime.parse(rs.getString("START_AT"))
        );
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT * FROM RESERVATION_TIME ORDER BY START_AT";

        return jdbcTemplate.query(sql, this::mapRowTime);
    }

    @Override
    public Optional<ReservationTime> findById(final long id) {
        final String sql = "SELECT * FROM RESERVATION_TIME WHERE ID = ?";

        return jdbcTemplate.query(sql, this::mapRowTime, id)
                .stream()
                .findAny();
    }

    @Override
    public ReservationTime fetchById(final long id) {
        return findById(id).orElseThrow(() -> new TimeNotFoundException("존재하지 않는 시간입니다."));
    }

    @Override
    public ReservationTime save(final ReservationTime time) {
        final SqlParameterSource params = new BeanPropertySqlParameterSource(time);
        final long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, time.getStartAt());
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM RESERVATION_TIME WHERE ID = ?";

        jdbcTemplate.update(sql, id);
    }
}
