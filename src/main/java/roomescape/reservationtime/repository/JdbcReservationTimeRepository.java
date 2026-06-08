package roomescape.reservationtime.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.InfrastructureException;
import roomescape.reservationtime.domain.ReservationTime;

import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private static final Logger log = LoggerFactory.getLogger(JdbcReservationTimeRepository.class);

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(reservationTime, keyHolder);
        validateCreatedRowCount(rowCount, reservationTime);

        Long id = getGeneratedId(keyHolder, reservationTime);
        return reservationTime.withId(id);
    }

    private int insert(ReservationTime reservationTime, KeyHolder keyHolder) {
        String sql = """
                INSERT INTO reservation_time (start_at)
                VALUES (?)
                """;

        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    sql,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservationTime.getStartAt().toString());
            return preparedStatement;
        }, keyHolder);
    }

    private void validateCreatedRowCount(int rowCount, ReservationTime reservationTime) {
        if (rowCount != 1) {
            log.error(
                    "Reservation time insert affected unexpected row count. rowCount={}, startAt={}",
                    rowCount,
                    reservationTime.getStartAt()
            );
            throw new InfrastructureException("예약 시간 생성에 실패했습니다.");
        }
    }

    private Long getGeneratedId(KeyHolder keyHolder, ReservationTime reservationTime) {
        Number key = keyHolder.getKey();
        if (key == null) {
            log.error(
                    "Reservation time insert did not return generated id. startAt={}",
                    reservationTime.getStartAt()
            );
            throw new InfrastructureException("예약 시간 생성에 실패했습니다.");
        }
        return key.longValue();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                """;

        return jdbcTemplate.query(sql, reservationTimeRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = """
                SELECT id, start_at
                FROM reservation_time
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, reservationTimeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation_time
                    WHERE id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }
}
