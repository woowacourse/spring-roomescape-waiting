package roomescape.reservationtime.repository.jdbc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.exception.BadRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jdbc")
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation_time")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        Map<String, Object> parameters = Map.ofEntries(Map.entry("start_at", reservationTime.getStartAt()));
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return ReservationTime.generateWithPrimaryKey(reservationTime, generatedKey);
    }

    @Override
    public List<ReservationTime> findAll() {
        final String query = "SELECT id, start_at FROM reservation_time";
        List<ReservationTime> reservationTimes = jdbcTemplate.query(query,
            (resultSet, rowNum) -> new ReservationTime(resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()));

        return reservationTimes;
    }

    @Override
    public Optional<ReservationTime> findById(Long timeId) {
        final String query = "SELECT id, start_at FROM reservation_time WHERE id = ?";

        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(query,
                    (resultSet, rowNum) -> new ReservationTime(resultSet.getLong("id"),
                        resultSet.getTime("start_at").toLocalTime()), timeId));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        final String query = "DELETE FROM reservation_time WHERE id = ?";
        int affectedRows = jdbcTemplate.update(query, id);
        if (affectedRows == 0) {
            throw new BadRequestException("예약 시간을 삭제할 수 없습니다. 존재하지 않는 ID입니다.");
        }
    }

    @Override
    public List<ReservationTime> findByReservationDateAndThemeId(LocalDate localDate, Long themeId) {
        final String query = """
            SELECT id, start_at
            FROM reservation_time
            WHERE id NOT IN (
                SELECT rt.id
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                WHERE r.date = ? AND r.theme_id = ?
            )
            """;

        List<ReservationTime> reservationTimes = jdbcTemplate.query(
            query,
            (resultSet, rowNum) -> new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getTime("start_at").toLocalTime()
            ),
            localDate, themeId
        );

        return reservationTimes;
    }
}
