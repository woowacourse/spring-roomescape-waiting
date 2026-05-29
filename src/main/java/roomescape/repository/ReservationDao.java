package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {

    private static final String SELECT_BASE = """
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
            """;

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

        Slot slot = new Slot(rs.getObject("date", LocalDate.class), reservationTime, theme);

        return Reservation.create(
                rs.getLong("reservation_id"),
                rs.getString("name"),
                slot
        );
    };

    public ReservationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public Reservation save(Reservation reservation) {
        Slot slot = reservation.slot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.name())
                .addValue("date", slot.date())
                .addValue("time_id", slot.time().id())
                .addValue("theme_id", slot.theme().id());

        long id = insertExecutor.executeAndReturnKey(params).longValue();

        return Reservation.create(id, reservation.name(), slot);
    }

    public Reservation update(Reservation reservation) {
        Slot slot = reservation.slot();
        String sql = "UPDATE reservation SET name = ?, date = ?, time_id = ?, theme_id = ? WHERE id = ?";
        int affected = jdbcTemplate.update(sql, reservation.name(), slot.date(), slot.time().id(), slot.theme().id(), reservation.id());

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
        return reservation;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        int affected = jdbcTemplate.update(sql, id);

        if (affected == 0) {
            throw new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다.");
        }
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(SELECT_BASE, rowMapper);
    }

    public Optional<Reservation> findById(long id) {
        String sql = SELECT_BASE + " WHERE reservation.id = ?";
        return jdbcTemplate.query(sql, rowMapper, id)
                .stream()
                .findFirst();
    }

    public List<Reservation> findAllByName(String name) {
        String sql = SELECT_BASE + " WHERE reservation.name = ?";
        return jdbcTemplate.query(sql, rowMapper, name);
    }

    public Optional<Reservation> findBySlot(Slot slot) {
        String sql = SELECT_BASE + " WHERE reservation.date = ? AND reservation.time_id = ? AND reservation.theme_id = ?";
        return jdbcTemplate.query(sql, rowMapper, slot.date(), slot.time().id(), slot.theme().id())
                .stream()
                .findFirst();
    }
}
