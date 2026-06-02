package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;
    private final RowMapper<Reservation> rowMapper = (rs, rowNum) -> {
        Theme theme = Theme.from(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("thumbnail_url"),
                rs.getString("theme_description")
        );

        ReservationTime reservationTime = ReservationTime.from(
                rs.getLong("time_id"),
                rs.getObject("time_value", LocalTime.class)
        );

        return Reservation.from(
                rs.getLong("reservation_id"),
                UserName.from(rs.getString("name")),
                Slot.from(
                        Schedule.from(
                                rs.getObject("date", LocalDate.class),
                                reservationTime
                        ),
                        theme
                )
        );
    };

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Long create(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getUserNameValue())
                .addValue("date", reservation.getReservationDate())
                .addValue("time_id", reservation.getReservationTime().getId())
                .addValue("theme_id", reservation.getReservationTheme().getId());

        Number reservationId = insertExecutor.executeAndReturnKey(params);

        return reservationId.longValue();
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

    public Optional<Reservation> findById(Long reservationId) {
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
                .findFirst();
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

    public Optional<Reservation> findBySlot(Slot slot) {
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
        return jdbcTemplate.query(sql, rowMapper, slot.getDate(), slot.getTime().getId(), slot.getTheme().getId()).stream().findFirst();
    }

    public boolean existsBySlotAndIdNot(Long reservationId, Slot slot) {
        String sql = """
            SELECT COUNT(1)
            FROM reservation
            WHERE date = ? AND time_id = ? AND theme_id = ? 
              AND id != ?  
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                slot.getDate(),
                slot.getTime().getId(),
                slot.getTheme().getId(),
                reservationId
        );

        return count != null && count > 0;
    }

    public void updateDateAndTime(Reservation reservation) {
        String sql = "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?";

        jdbcTemplate.update(sql, reservation.getReservationDate(), reservation.getReservationTime().getId(), reservation.getId());
    }

    public void delete(Reservation reservation) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservation.getId());
    }
}
