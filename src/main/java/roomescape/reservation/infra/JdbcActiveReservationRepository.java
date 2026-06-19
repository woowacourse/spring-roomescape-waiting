package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.ActiveReservationRepository;
import roomescape.reservation.domain.TimeSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcActiveReservationRepository implements ActiveReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<ActiveReservation> rowMapper = (resultSet, rowNum) -> {
        Theme theme = Theme.builder()
                .id(resultSet.getLong("t_id"))
                .name(resultSet.getString("t_name"))
                .thumbnailImageUrl(resultSet.getString("t_thumbnail_image_url"))
                .description(resultSet.getString("t_description"))
                .durationTime(resultSet.getTime("t_duration_time").toLocalTime())
                .build();

        ReservationTime time = ReservationTime.builder()
                .id(resultSet.getLong("rt_id"))
                .startAt(resultSet.getTime("rt_start_at").toLocalTime())
                .build();

        TimeSlot slot = TimeSlot.builder()
                .id(resultSet.getLong("ts_id"))
                .date(resultSet.getDate("ts_date").toLocalDate())
                .time(time)
                .theme(theme)
                .build();

        return ActiveReservation.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .slot(slot)
                .is_deleted(resultSet.getLong("r_is_deleted"))
                .createdAt(resultSet.getTimestamp("r_created_at").toLocalDateTime())
                .build();
    };

    @Override
    public ActiveReservation save(final ActiveReservation reservation) {
        String sql = "INSERT INTO reservation(name, slot_id, created_at) "
                + "VALUES(:name, :slotId, :createdAt)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("createdAt", reservation.getCreatedAt());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return reservation.withId(generatedId);
    }

    @Override
    public ActiveReservation insertWithId(ActiveReservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.getId())
                .addValue("name", reservation.getName())
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("createdAt", reservation.getCreatedAt());

        String updateSql = "UPDATE reservation "
                + "SET name = :name, slot_id = :slotId, created_at = :createdAt, is_deleted = 0 "
                + "WHERE id = :id";

        int affected = jdbcTemplate.update(updateSql, params);

        if (affected == 0) {
            String insertSql = "INSERT INTO reservation(id, name, slot_id, created_at) "
                    + "VALUES(:id, :name, :slotId, :createdAt)";
            jdbcTemplate.update(insertSql, params);
        }
        return reservation;
    }

    @Override
    public int update(final ActiveReservation reservation) {
        String sql = "UPDATE reservation "
                + "SET slot_id = :slotId, created_at = :createdAt "
                + "WHERE id = :id AND is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("slotId", reservation.getSlot().getId())
                .addValue("createdAt", reservation.getCreatedAt())
                .addValue("id", reservation.getId());

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<ActiveReservation> findById(final Long id) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.id = :id AND r.is_deleted = 0 ";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<ActiveReservation> findAll() {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.is_deleted = 0 "
                + "ORDER BY ts.date ASC, rt.start_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<ActiveReservation> findByThemeAndDate(final Long themeId, final LocalDate date) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE ts.theme_id = :themeId AND ts.date = :date AND r.is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", date);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<ActiveReservation> findAllByName(final String name) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.is_deleted = 0 AND r.name = :name "
                + "ORDER BY ts.date ASC, rt.start_at ASC";
        return jdbcTemplate.query(sql, Map.of("name", name), rowMapper);
    }

    @Override
    public List<ActiveReservation> findAllByIdIn(List<Long> reservationIds) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.is_deleted AS r_is_deleted, r.created_at AS r_created_at, "
                + "ts.id AS ts_id, ts.date AS ts_date, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "INNER JOIN theme t ON ts.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON ts.time_id = rt.id "
                + "WHERE r.id IN (:ids) AND r.is_deleted = 0 "
                + "ORDER BY ts.date ASC, rt.start_at ASC";

        SqlParameterSource params = new MapSqlParameterSource("ids", reservationIds);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public boolean existsByReservationTime(final Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "WHERE ts.time_id=:timeId AND r.is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId), Boolean.class));
    }

    @Override
    public boolean existsByTheme(final Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation r "
                + "INNER JOIN time_slot ts ON r.slot_id = ts.id "
                + "WHERE ts.theme_id=:themeId AND r.is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId), Boolean.class));
    }

    @Override
    public boolean existsByActiveSlotId(Long slotId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE slot_id=:slotId AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("slotId", slotId),
                        Boolean.class));
    }

    @Override
    public boolean existsByActiveSlotIdNotId(Long slotId, Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE slot_id = :slotId AND is_deleted = 0 AND id != :id)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("slotId", slotId, "id", id),
                        Boolean.class));
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = :id AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("id", id),
                        Boolean.class));
    }

    @Override
    public int cancel(final ActiveReservation reservation) {
        String sql = "UPDATE reservation SET is_deleted=:id WHERE id = :id AND is_deleted = 0";
        return jdbcTemplate.update(sql, Map.of("id", reservation.getId()));
    }
}
