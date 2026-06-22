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
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("name", "date", "theme_id", "time_id")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationDetail> findAll() {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.status = 'CONFIRMED'
                        ORDER BY r.date ASC
                        """,
                (rs, rowNum) ->
                        new ReservationDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime())
        );
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(
                """
                        SELECT id, name, date, theme_id, time_id
                        FROM reservation
                        WHERE name = ? AND status = 'CONFIRMED'
                        ORDER BY date ASC
                        """,
                (rs, rowNum) -> mapReservation(rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id")),
                name
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jdbcTemplate.query(
                """
                        SELECT id, name, date, theme_id, time_id
                        FROM reservation
                        WHERE id = ? AND status = 'CONFIRMED'
                        """,
                (rs, rowNum) -> mapReservation(rs.getLong("id"),
                        rs.getString("name"),
                        rs.getDate("date").toLocalDate(),
                        rs.getLong("theme_id"),
                        rs.getLong("time_id")),
                id
        ).stream().findFirst();
    }

    @Override
    public Optional<ReservationDetail> findDetailById(Long id) {
        return jdbcTemplate.query(
                """
                        SELECT r.id, r.name, r.date, r.theme_id, t.name as theme_name, t.description, t.thumbnail_img_url, r.time_id, rt.start_at
                        FROM reservation r
                        JOIN theme t ON r.theme_id = t.id
                        JOIN reservation_time rt ON r.time_id = rt.id
                        WHERE r.id = ? AND r.status = 'CONFIRMED'
                        """,
                (rs, rowNum) ->
                        new ReservationDetail(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getDate("date").toLocalDate(),
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("description"),
                                rs.getString("thumbnail_img_url"),
                                rs.getLong("time_id"),
                                rs.getTime("start_at").toLocalTime()),
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
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
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
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        return reservation;
    }

    @Override
    public Integer delete(Long id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public Boolean existsByDateAndThemeAndTime(LocalDate date, Long themeId, Long timeId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1 FROM reservation
                            WHERE date = ? AND theme_id = ? AND time_id = ? AND status = 'CONFIRMED'
                        )
                        """,
                Boolean.class,
                date,
                themeId,
                timeId);
    }

    @Override
    public Boolean existsByNameAndDateAndThemeAndTime(
            String name,
            LocalDate date,
            Long themeId,
            Long timeId
    ) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1
                            FROM reservation
                            WHERE name = ? AND date = ? AND theme_id = ? AND time_id = ?
                                AND status = 'CONFIRMED'
                        )
                        """,
                Boolean.class,
                name,
                date,
                themeId,
                timeId
        );
    }

    @Override
    public Boolean existsByDateAndThemeAndTimeExcludingId(LocalDate date, Long themeId, Long timeId, Long id) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT EXISTS(
                            SELECT 1
                            FROM reservation
                            WHERE date = ? AND theme_id = ? AND time_id = ? AND id <> ?
                                AND status = 'CONFIRMED'
                        )
                        """,
                Boolean.class,
                date,
                themeId,
                timeId,
                id
        );
    }

    @Override
    public void updateWaitingOwner(Long id, String name) {
        int updatedRowCount = jdbcTemplate.update(
                "UPDATE reservation SET name = ? WHERE id = ?",
                name,
                id
        );

        if (updatedRowCount == 0) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private Reservation mapReservation(Long id, String name, LocalDate date, Long themeId, Long timeId) {
        return Reservation.builder()
                .id(id)
                .name(name)
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build();
    }
}
