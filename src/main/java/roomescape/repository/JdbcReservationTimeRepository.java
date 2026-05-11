package roomescape.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.common.exception.ErrorCode;
import roomescape.common.exception.InfrastructureException;
import roomescape.repository.dto.ReservationTimeAvailability;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {


    private final JdbcTemplate jdbcTemplate;
    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowCount = insert(reservationTime, keyHolder);
        validateCreatedRowCount(rowCount);

        Long id = getGeneratedId(keyHolder);
        return reservationTime.withId(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("""
                SELECT id, start_at
                FROM reservation_time
                """, reservationTimeRowMapper);
    }

    @Override
    public boolean deleteById(Long id) {
        int rowCount = jdbcTemplate.update("""
                DELETE FROM reservation_time
                WHERE id = ?
                """, id);
        return rowCount > 0;
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jdbcTemplate.query("""
                        SELECT id, start_at
                        FROM reservation_time
                        WHERE id = ?
                        """, reservationTimeRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation_time
                WHERE start_at = ?
                """, Integer.class, startAt.toString());
        return count != null && count > 0;
    }

    @Override
    public List<ReservationTimeAvailability> findAllByDateAndThemeIdWithAvailability(LocalDate date, Long themeId) {
        String sql = """
                 SELECT rt.id AS id,
                        rt.start_at AS start_at,
                        r.id IS NULL AS available
                 FROM reservation_time rt
                 LEFT JOIN reservation r
                     ON r.time_id = rt.id
                    AND r.date = ?
                    AND r.theme_id = ?
                ORDER BY rt.start_at
                """;

        return jdbcTemplate.query(sql, reservationTimeAvailabilityRowMapper, date, themeId);
    }

    private int insert(ReservationTime reservationTime, KeyHolder keyHolder) {
        return jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO reservation_time (start_at)
                            VALUES (?)
                            """,
                    new String[]{"id"}
            );
            preparedStatement.setString(1, reservationTime.getStartAt().toString());
            return preparedStatement;
        }, keyHolder);
    }

    private void validateCreatedRowCount(int rowCount) {
        if (rowCount != 1) {
            throw new InfrastructureException(ErrorCode.RESERVATION_TIME_CREATE_FAILED);
        }
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new InfrastructureException(ErrorCode.RESERVATION_TIME_CREATE_FAILED);
        }
        return key.longValue();
    }

    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );

    private final RowMapper<ReservationTimeAvailability> reservationTimeAvailabilityRowMapper = (resultSet, rowNum) -> {

        if(resultSet.getBoolean("available")) {
            return ReservationTimeAvailability.available(
                    new ReservationTime(
                            resultSet.getLong("id"),
                            LocalTime.parse(resultSet.getString("start_at"))
                    )
            );
        }
        return ReservationTimeAvailability.unavailable(
                new ReservationTime(
                        resultSet.getLong("id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                )
        );
    };
}
