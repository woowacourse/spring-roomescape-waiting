package roomescape.reservation.infra;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.repository.ReservationRepository;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jdbcTemplate.query("""
                            SELECT r.id, r.name, r.date, r.theme_id, r.time_id, r.status, rt.start_at
                            FROM reservation r
                            JOIN reservation_time rt ON r.time_id = rt.id
                            WHERE r.id = ?
                        """,
                (rs, rowNum) -> {
                    ReservationSlot slot = ReservationSlot.builder()
                            .date(rs.getDate("date").toLocalDate())
                            .themeId(rs.getLong("theme_id"))
                            .timeId(rs.getLong("time_id"))
                            .startAt(rs.getObject("start_at", LocalTime.class))
                            .build();

                    User user = User.builder()
                            .name(rs.getString("name"))
                            .build();

                    return Reservation.builder()
                            .id(rs.getLong("id"))
                            .user(user)
                            .slot(slot)
                            .status(ReservationStatus.valueOf(rs.getString("status")))
                            .build();
                }
                , id).stream().findFirst();
    }

    @Override
    public Optional<ReservationSlot> findSlotById(Long id) {
        return jdbcTemplate.query("""
                            SELECT r.date, r.theme_id, r.time_id, rt.start_at
                            FROM reservation r
                            JOIN reservation_time rt ON r.time_id = rt.id
                            WHERE r.id = ?
                        """,
                (rs, rowNum) -> ReservationSlot.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .themeId(rs.getLong("theme_id"))
                        .timeId(rs.getLong("time_id"))
                        .startAt(rs.getObject("start_at", LocalTime.class))
                        .build()
                , id).stream().findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationSlot slot = reservation.getSlot();
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getUserName())
                .addValue("date", slot.date())
                .addValue("theme_id", slot.themeId())
                .addValue("time_id", slot.timeId())
                .addValue("status", reservation.getStatus().name());
        try {
            Long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return reservation.withId(id);
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    @Override
    public Integer update(Long id, ReservationSlot slot) {
        try {
            return jdbcTemplate.update(
                    "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?",
                    slot.date(),
                    slot.timeId(),
                    id);
        } catch (DuplicateKeyException e) {
            throw new UniqueConstraintViolationException(e);
        }
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public Boolean existsBySlot(ReservationSlot slot) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE date = ? AND theme_id = ? AND time_id = ?)",
                Boolean.class,
                slot.date(),
                slot.themeId(),
                slot.timeId());
    }

    @Override
    public Boolean existsByUserAndSlot(String username, ReservationSlot slot) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1
                            FROM reservation
                            WHERE name = ?
                              AND date = ?
                              AND theme_id = ?
                              AND time_id = ?
                        )
                        """,
                Boolean.class,
                username,
                slot.date(),
                slot.themeId(),
                slot.timeId());
    }

    @Override
    public Boolean existsByTheme(Long themeId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = ?)",
                Boolean.class,
                themeId);
    }

    @Override
    public Boolean existsByTime(Long timeId) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM reservation WHERE time_id = ?)",
                Boolean.class,
                timeId);
    }
}
