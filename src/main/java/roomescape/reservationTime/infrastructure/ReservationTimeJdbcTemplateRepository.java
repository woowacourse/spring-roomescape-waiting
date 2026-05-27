package roomescape.reservationTime.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

@Repository
public class ReservationTimeJdbcTemplateRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeJdbcTemplateRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservation) {
        Map<String, Object> params = Map.of(
                "start_at", reservation.getStartAt()
        );
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return ReservationTime.createRow(id, reservation.getStartAt());
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        List<ReservationTime> reservationTime = jdbcTemplate.query(sql,
                (rs, rowNum) -> ReservationTime.createRow(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime()
                ),
                id
        );
        return reservationTime.stream()
                .findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> ReservationTime.createRow(
                        rs.getLong("id"),
                        rs.getTime("start_at").toLocalTime()
                ));
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }
}
