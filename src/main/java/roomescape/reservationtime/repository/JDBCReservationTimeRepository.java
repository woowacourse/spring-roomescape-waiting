package roomescape.reservationtime.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.entity.ReservationTimeEntity;

@Repository
public class JDBCReservationTimeRepository implements ReservationTimeRepository {

    private static final RowMapper<ReservationTimeEntity> RESERVATION_TIME_ENTITY_ROW_MAPPER = (resultSet, rowNum) -> new ReservationTimeEntity(
            resultSet.getLong("id"),
            resultSet.getString("start_at")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JDBCReservationTimeRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query(
                "SELECT id, start_at FROM reservation_time",
                (resultSet, rowNum) -> {
                    ReservationTimeEntity entity = new ReservationTimeEntity(
                            resultSet.getLong("id"),
                            resultSet.getString("start_at")
                    );
                    return entity.toReservationTime();
                }
        );
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        Long generatedId = simpleJdbcInsert.executeAndReturnKey(
                Map.of("start_at", reservationTime.getStartAt())).longValue();

        return ReservationTime.of(generatedId, reservationTime.getStartAt());
    }

    @Override
    public boolean deleteById(final Long id) {
        return jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id) != 0;
    }

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        try {
            ReservationTimeEntity reservationTimeEntity = jdbcTemplate.queryForObject(
                    "SELECT id, start_at FROM reservation_time WHERE id = ?",
                    RESERVATION_TIME_ENTITY_ROW_MAPPER,
                    id
            );
            return Optional.ofNullable(reservationTimeEntity)
                    .map(ReservationTimeEntity::toReservationTime);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean checkExistsByStartAt(final LocalTime time) {
        try {
            Boolean exists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)",
                    Boolean.class,
                    time
            );
            return Boolean.TRUE.equals(exists);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
