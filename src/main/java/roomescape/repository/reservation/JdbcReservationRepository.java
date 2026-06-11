package roomescape.repository.reservation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );

        ReservationTime reservationTime = ReservationTime.of(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        ReservationSlot slot = new ReservationSlot(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                reservationTime
        );

        return new Reservation(
                resultSet.getLong("id"),
                resultSet.getString("reservation_name"),
                slot,
                resultSet.getTimestamp("created_at").toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.slot_id,
                       s.date,
                       r.created_at,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.slot_id,
                       s.date,
                       r.created_at,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Reservation> findBySlot(final ReservationSlot slot) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.slot_id,
                       s.date,
                       r.created_at,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE s.date = ? AND s.theme_id = ? AND s.time_id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        reservationRowMapper,
                        Date.valueOf(slot.getDate()),
                        slot.getTheme().getId(),
                        slot.getTime().getId()
                )
                .stream()
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateAndTheme(final LocalDate date, final Theme theme) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.slot_id,
                       s.date,
                       r.created_at,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_slot AS s ON r.slot_id = s.id
                INNER JOIN reservation_time AS rt ON s.time_id = rt.id
                INNER JOIN theme AS t ON s.theme_id = t.id
                WHERE s.date = ? AND s.theme_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                reservationRowMapper,
                Date.valueOf(date),
                theme.getId()
        );
    }

    @Override
    public boolean existsByTime(final ReservationTime time) {
        String sql = """
                SELECT COUNT(1)
                FROM reservation AS r
                INNER JOIN reservation_slot AS s ON r.slot_id = s.id
                WHERE s.time_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, time.getId());
        return count > 0;
    }

    @Override
    public void delete(final Reservation reservation) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        try {
            jdbcTemplate.update(sql, reservation.getId());
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    public Reservation save(final Reservation reservation) {
        if (reservation.getId() == null) {
            return insert(reservation);
        }

        return update(reservation);
    }

    private Reservation insert(final Reservation reservation) {
        String sql = "INSERT INTO reservation (name, slot_id, created_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
                preparedStatement.setString(1, reservation.getName());
                preparedStatement.setLong(2, reservation.getSlot().getId());
                preparedStatement.setTimestamp(3, Timestamp.valueOf(reservation.getCreatedAt()));
                return preparedStatement;
            }, keyHolder);
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 예약 ID를 생성하지 못했습니다.");
        }

        return reservation.withId(key.longValue());
    }

    private Reservation update(final Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET slot_id = ?
                WHERE id = ?
                """;

        try {
            jdbcTemplate.update(
                    sql,
                    reservation.getSlot().getId(),
                    reservation.getId()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }

        return reservation;
    }
}
