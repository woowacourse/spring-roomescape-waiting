package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.ConflictException;
import roomescape.global.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservation.exception.ReservationErrorMessage;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "date", "theme_id", "time_id");
    }

    @Override
    public List<ReservationDetail> findAll() {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.status, r.amount
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        ORDER BY r.date ASC
                        """,
                (rs, rowNum) -> new ReservationDetail(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime(),
                        ReservationStatus.valueOf(rs.getString("status")),
                        rs.getLong("amount"))
        );
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(
                "SELECT id, name, date, theme_id, time_id, status, amount FROM reservation WHERE name = ? ORDER BY date ASC",
                (rs, rowNum) -> mapReservation(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id"),
                        ReservationStatus.valueOf(rs.getString("status")),
                        rs.getLong("amount")),
                name
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jdbcTemplate.query(
                "SELECT id, name, date, theme_id, time_id, status, amount FROM reservation WHERE id = ?",
                (rs, rowNum) -> mapReservation(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id"),
                        ReservationStatus.valueOf(rs.getString("status")),
                        rs.getLong("amount")),
                id
        ).stream().findFirst();
    }

    @Override
    public Optional<ReservationDetail> findDetailById(Long id) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at, r.status, r.amount
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.id = ?
                        """,
                (rs, rowNum) -> new ReservationDetail(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_img_url"),
                        rs.getLong("time_id"),
                        rs.getTime("start_at").toLocalTime(),
                        ReservationStatus.valueOf(rs.getString("status")),
                        rs.getLong("amount")),
                id
        ).stream().findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("theme_id", reservation.getThemeId())
                .addValue("time_id", reservation.getTimeId());

        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return reservation.withId(id);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ReservationErrorMessage.DUPLICATE_RESERVATION);
        }
    }

    @Override
    public Reservation update(Reservation reservation) {
        int updatedRowCount = jdbcTemplate.update(
                "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?",
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getId()
        );

        if (updatedRowCount == 0) {
            throw new NotFoundException(ReservationErrorMessage.RESERVATION_NOT_FOUND, reservation.getId());
        }

        return reservation;
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public Boolean existsByNameAndDateAndThemeAndTime(String name, LocalDate date, Long themeId, Long timeId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE name = ? AND date = ? AND theme_id = ? AND time_id = ?)",
                Boolean.class,
                name, date, themeId, timeId);
    }

    @Override
    public Boolean existsByDateAndThemeAndTimeExcludingId(LocalDate date, Long themeId, Long timeId, Long id) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM reservation
                            WHERE date = ? AND theme_id = ? AND time_id = ? AND id <> ?
                        )
                        """,
                Boolean.class,
                date, themeId, timeId, id
        );
    }

    @Override
    public boolean insertFromOldestWaiting(LocalDate date, Long themeId, Long timeId) {
        try {
            int rows = jdbcTemplate.update(
                    """
                            INSERT INTO reservation (name, date, theme_id, time_id, status, amount, expires_at)
                            SELECT w.name, w.date, w.theme_id, w.time_id,
                                   'PENDING_PAYMENT', t.price, DATEADD('MINUTE', 10, NOW())
                            FROM waiting w
                            JOIN theme t ON t.id = w.theme_id
                            WHERE w.date = ? AND w.theme_id = ? AND w.time_id = ?
                            ORDER BY w.id ASC LIMIT 1
                            """,
                    date, themeId, timeId
            );
            return rows > 0;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public boolean confirmPayment(Long reservationId) {
        int rows = jdbcTemplate.update(
                """
                        UPDATE reservation SET status = 'CONFIRMED'
                        WHERE id = ? AND status = 'PENDING_PAYMENT' AND expires_at > NOW()
                        """,
                reservationId
        );
        return rows > 0;
    }

    @Override
    public boolean markAsUncertain(Long reservationId) {
        int rows = jdbcTemplate.update(
                "UPDATE reservation SET status = 'UNCERTAIN' WHERE id = ? AND status = 'PENDING_PAYMENT'",
                reservationId
        );
        return rows > 0;
    }

    private Reservation mapReservation(Long id, String name, LocalDate date, Long themeId, Long timeId,
                                        ReservationStatus status, Long amount) {
        return Reservation.builder()
                .id(id)
                .name(name)
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .status(status)
                .amount(amount)
                .build();
    }
}
