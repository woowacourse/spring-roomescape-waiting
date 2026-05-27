package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.create(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.create(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return Reservation.create(
                rs.getLong("reservation_id"),
                rs.getString("name"),
                rs.getObject("date", LocalDate.class),
                reservationTime,
                theme
        );
    };

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(String name, LocalDate date, long timeId, long themeId) {

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("time_id", timeId)
                .addValue("theme_id", themeId);

        Number reservationId = insertExecutor.executeAndReturnKey(params);

        String sql = """
                SELECT 
                    reservation.id as reservation_id,
                    reservation.name,
                    reservation.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description 
                FROM reservation as reservation
                INNER JOIN reservation_time as time
                ON reservation.time_id = time.id
                INNER JOIN theme as theme
                ON reservation.theme_id = theme.id
                WHERE reservation.id = ?
                """;

        return jdbcTemplate.queryForObject(sql, rowMapper, reservationId.longValue());
    }

    public void delete(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int affected = jdbcTemplate.update(sql, reservationId);

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
    }

    public List<Reservation> findAllReservations() {
        String sql = """
                SELECT
                    reservation.id as reservation_id,
                    reservation.name,
                    reservation.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description 
                FROM reservation as reservation
                INNER JOIN reservation_time as time
                ON reservation.time_id = time.id
                INNER JOIN theme as theme
                ON reservation.theme_id = theme.id
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    public Reservation findById(long reservationId) {
        String sql = """
                SELECT
                    reservation.id as reservation_id,
                    reservation.name,
                    reservation.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description
                FROM reservation as reservation
                INNER JOIN reservation_time as time ON reservation.time_id = time.id
                INNER JOIN theme as theme ON reservation.theme_id = theme.id
                WHERE reservation.id = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, reservationId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다."));
    }

    public List<Reservation> findByName(String name) {
        String sql = """
                SELECT
                    reservation.id as reservation_id,
                    reservation.name,
                    reservation.date,
                    time.id as time_id,
                    time.start_at as time_value,
                    theme.id as theme_id,
                    theme.name as theme_name,
                    theme.thumbnail_url as thumbnail_url,
                    theme.description as theme_description
                FROM reservation as reservation
                INNER JOIN reservation_time as time ON reservation.time_id = time.id
                INNER JOIN theme as theme ON reservation.theme_id = theme.id
                WHERE reservation.name = ?
                """;
        return jdbcTemplate.query(sql, rowMapper, name);
    }

    public Reservation updateDateAndTime(long reservationId, LocalDate date, long timeId) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, date, timeId, reservationId);
        return findById(reservationId);
    }

    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
                SELECT
                            reservation.id as reservation_id,
                            reservation.name,
                            reservation.date,
                            time.id as time_id,
                            time.start_at as time_value,
                            theme.id as theme_id,
                            theme.name as theme_name,
                            theme.thumbnail_url as thumbnail_url,
                            theme.description as theme_description
                        FROM reservation as reservation
                        INNER JOIN reservation_time as time ON reservation.time_id = time.id
                        INNER JOIN theme as theme ON reservation.theme_id = theme.id
                        WHERE date = ? AND time_id = ? AND theme_id = ?;
                """;
        return jdbcTemplate.query(sql, rowMapper, date, timeId, themeId).stream().findFirst();
    }

}
