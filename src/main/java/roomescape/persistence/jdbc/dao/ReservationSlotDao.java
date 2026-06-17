package roomescape.persistence.jdbc.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.exception.EntityNotFoundException;
import roomescape.persistence.dto.ReservationCondition;
import roomescape.persistence.jdbc.mapper.ReservationSlotRowMapper;
import roomescape.persistence.util.RepositoryExceptionTranslator;

@Repository
@RequiredArgsConstructor
public class ReservationSlotDao {

    private final JdbcTemplate jdbcTemplate;

    public Long insert(ReservationSlot slot) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)";

        RepositoryExceptionTranslator.execute(() -> {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setDate(1, Date.valueOf(slot.getDate()));
                ps.setLong(2, slot.getTheme().getId());
                ps.setLong(3, slot.getTime().getId());
                return ps;
            }, keyHolder);
        }, "이미 예약이 존재하는 시간입니다.");

        return keyHolder.getKey().longValue();
    }

    public void update(ReservationSlot slot) {
        String sql = """
                UPDATE reservation_slot
                SET date = ?, theme_id = ?, time_id = ?
                WHERE id = ?
                """;

        int updatedRows = RepositoryExceptionTranslator.execute(
                () -> jdbcTemplate.update(
                        sql,
                        Date.valueOf(slot.getDate()),
                        slot.getTheme().getId(),
                        slot.getTime().getId(),
                        slot.getId()
                ), "이미 예약이 존재하는 시간입니다.");
        if (updatedRows == 0) {
            throw new EntityNotFoundException("존재하지 않는 예약 슬롯입니다.");
        }
    }

    public Optional<ReservationSlot> findById(long id) {
        String sql = """
                SELECT r.id AS res_id, r.date AS res_date,
                       rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.price, t.is_active
                FROM reservation_slot r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.id = ?
                """;
        return queryOptional(sql, id);
    }

    public Optional<ReservationSlot> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        String sql = """
                SELECT r.id AS res_id, r.date AS res_date,
                       rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.price, t.is_active
                FROM reservation_slot r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?
                FOR UPDATE
                """;
        return queryOptional(sql, condition.date(), condition.timeId(), condition.themeId());
    }

    public Optional<ReservationSlot> findByReservationIdForUpdate(long reservationId) {
        String sql = """
                SELECT r.id AS res_id, r.date AS res_date,
                       rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.price, t.is_active
                FROM reservation re
                JOIN reservation_slot r ON re.slot_id = r.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
                WHERE re.id = ?
                FOR UPDATE
                """;
        return queryOptional(sql, reservationId);
    }

    private Optional<ReservationSlot> queryOptional(String sql, Object... params) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql,
                    ReservationSlotRowMapper.RESERVATION_SLOT_WITHOUT_RESERVATIONS_ROW_MAPPER,
                    params
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
