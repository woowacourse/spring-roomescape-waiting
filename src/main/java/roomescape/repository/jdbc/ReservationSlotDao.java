package roomescape.repository.jdbc;

import static roomescape.repository.jdbc.ReservationEntityMapper.mapReservation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.repository.dto.ReservationCondition;
import roomescape.repository.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class ReservationSlotDao {

    private static final String BASE_RESERVATION_SQL = """
            SELECT r.id AS res_id, r.date AS res_date,
                   rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.price, t.is_active
            FROM reservation r
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            """;
    private static final String BASE_ENTRY_SQL = """
            SELECT r.id AS res_id, r.date AS res_date,
                   rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                   t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.price, t.is_active
            FROM reservation_entry re
            JOIN reservation r ON re.reservation_id = r.id
            JOIN reservation_time rt ON r.time_id = rt.id
            JOIN theme t ON r.theme_id = t.id
            """;
    private final JdbcTemplate jdbcTemplate;

    public Long insert(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reservation (date, theme_id, time_id) VALUES (?, ?, ?)";

        RepositoryExceptionTranslator.execute(() -> {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setDate(1, Date.valueOf(reservation.getDate()));
                ps.setLong(2, reservation.getTheme().getId());
                ps.setLong(3, reservation.getTime().getId());
                return ps;
            }, keyHolder);
        }, "이미 예약이 존재하는 시간입니다.");

        return keyHolder.getKey().longValue();
    }

    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET date = ?, theme_id = ?, time_id = ?
                WHERE id = ?
                """;

        RepositoryExceptionTranslator.execute(() ->
                jdbcTemplate.update(
                        sql,
                        Date.valueOf(reservation.getDate()),
                        reservation.getTheme().getId(),
                        reservation.getTime().getId(),
                        reservation.getId()
                ), "이미 예약이 존재하는 시간입니다.");
    }

    public Optional<Reservation> findById(long id) {
        String whereById = "WHERE r.id = ?";
        return queryOptional(BASE_RESERVATION_SQL + whereById, id);
    }

    public Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        String whereBySlotForUpdate = """
                WHERE r.date = ?
                    AND r.time_id = ?
                    AND r.theme_id = ?
                FOR UPDATE
                """;
        return queryOptional(BASE_RESERVATION_SQL + whereBySlotForUpdate, condition.date(), condition.timeId(),
                condition.themeId());
    }

    private Optional<Reservation> queryOptional(String sql, Object... params) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapReservation(rs), params));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Reservation> findByEntryId(long entryId) {
        String whereByEntryId = "WHERE re.id = ?";
        return queryOptional(BASE_ENTRY_SQL + whereByEntryId, entryId);
    }

    public Optional<Reservation> findByEntryIdForUpdate(long entryId) {
        String whereByEntryIdForUpdate = """
                WHERE re.id = ?
                FOR UPDATE
                """;
        return queryOptional(BASE_ENTRY_SQL + whereByEntryIdForUpdate, entryId);
    }
}
