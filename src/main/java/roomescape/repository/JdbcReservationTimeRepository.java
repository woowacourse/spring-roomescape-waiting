package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;

@Primary
@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTimeWithoutId) {
        String sql = "INSERT INTO `reservation_time`(`start_at`) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setTime(1, Time.valueOf(reservationTimeWithoutId.getStartAt()));

            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return ReservationTime.of(id, reservationTimeWithoutId);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT * FROM `reservation_time` WHERE `id` = (?)";

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            LocalTime startAt = resultSet.getTime("start_at").toLocalTime();
            return new ReservationTime(id, startAt);
        }, id).stream().findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT * FROM `reservation_time` "
                + "ORDER BY start_at ASC";

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Long id = resultSet.getLong("id");
            LocalTime startAt = resultSet.getTime("start_at").toLocalTime();
            return new ReservationTime(id, startAt);
        });
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM `reservation_time` WHERE `id` = (?)";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<ReservationTime> findReservedTimesByDateAndTheme(LocalDate date, Long themeId) {
        String sql = "SELECT t.id as time_id, t.start_at as start_at "
                + "FROM `reservation_time` t "
                + "INNER JOIN `reservation` r ON r.time_id = t.id "
                + "WHERE r.date = (?) AND r.theme_id = (?) ";

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Long id = resultSet.getLong("time_id");
            LocalTime startAt = resultSet.getTime("start_at").toLocalTime();
            return new ReservationTime(id, startAt);
        }, date, themeId);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = "SELECT EXISTS ("
                + "SELECT 1 FROM `reservation_time` WHERE `start_at` = (?) "
                + ") AS exist";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, startAt));
    }
}
