package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationQueryingDao {

    private final JdbcTemplate jdbcTemplate;

    public ReservationQueryingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("start_at", LocalTime.class)
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );

        return Reservation.restore(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getObject("reservation_date", LocalDate.class),
                reservationTime,
                theme,
                resultSet.getObject("reservation_created_at", LocalDateTime.class),
                resultSet.getString("reservation_version")
        );
    };

    public Optional<Reservation> findReservationById(long id) {
        String sql = """
                select r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, r.time_id, r.created_at as reservation_created_at, r.version as reservation_version, t.start_at, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.url as theme_url
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on th.id = r.theme_id
                where r.id = ?
                """;
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Reservation> findAllReservations() {
        String sql = """
                select r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, r.time_id, r.created_at as reservation_created_at, r.version as reservation_version, t.start_at, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.url as theme_url
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on th.id = r.theme_id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    public List<Reservation> findAllByName(String name) {
        String sql = """
                select r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, r.time_id, r.created_at as reservation_created_at, r.version as reservation_version, t.start_at, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.url as theme_url
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on th.id = r.theme_id
                where r.name = ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }

    public Optional<Reservation> findReservationByThemeAndDateAndTime(Long themeId, LocalDate date, Long timeId) {
        String sql = """
                select r.id as reservation_id, r.name as reservation_name, r.date as reservation_date, r.time_id, r.created_at as reservation_created_at, r.version as reservation_version, t.start_at, th.id as theme_id, th.name as theme_name, th.description as theme_description, th.url as theme_url
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on th.id = r.theme_id
                where r.theme_id = ? and r.date = ? and r.time_id = ?
                """;
        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, themeId, date, timeId);
            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public boolean isExistById(Long id) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM reservation
                    WHERE id = ?
            )
            """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }
}
