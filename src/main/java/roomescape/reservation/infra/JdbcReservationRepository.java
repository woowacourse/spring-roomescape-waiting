package roomescape.reservation.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
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

        return Reservation.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .date(resultSet.getDate("r_date").toLocalDate())
                .status(Status.valueOf(resultSet.getString("r_status")))
                .time(time)
                .theme(theme)
                .build();
    };

    private final RowMapper<ReservationQueryResult> queryResultRowMapper = (resultSet, rowNum)->{
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

        return  ReservationQueryResult.builder()
                .id(resultSet.getLong("r_id"))
                .name(resultSet.getString("r_name"))
                .date(resultSet.getDate("r_date").toLocalDate())
                .status(Status.valueOf(resultSet.getString("r_status")))
                .pendingIndex(resultSet.getLong("pending_index"))
                .time(time)
                .theme(theme)
                .build();
    };

    @Override
    public Reservation save(Reservation reservation) {
        String sql = "INSERT INTO reservation(name, date, time_id, theme_id, status, created_at) "
                + "VALUES(:name, :date, :timeId, :themeId, :status, :createdAt)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("createdAt", reservation.getCreatedAt());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return reservation.withId(generatedId);
    }

    @Override
    public void updateById(Long id, Reservation reservation) {
        String sql = "UPDATE reservation "
                + "SET date = :date, time_id = :timeId, theme_id = :themeId, status = :status "
                + "WHERE id = :id AND is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("id", id);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.id = :id AND r.status IN ('ACTIVE', 'PENDING') AND r.is_deleted = 0 ";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.status = 'ACTIVE' AND r.is_deleted = 0 "
                + "ORDER BY r.date ASC, rt.start_at ASC";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Reservation> findByThemeAndDate(Long themeId, LocalDate date) {
        String sql = "SELECT "
                + "r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.theme_id = :themeId AND r.date = :date AND r.status = 'ACTIVE' AND r.is_deleted = 0";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", date);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<ReservationQueryResult> findAllByName(String username) {
        String sql = "SELECT "
                + "    r.id AS r_id, "
                + "    r.name AS r_name, "
                + "    r.date AS r_date, "
                + "    r.status AS r_status, "
                + "    r.created_at AS r_created_at, "
                + "    t.id AS t_id, "
                + "    t.name AS t_name, "
                + "    t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "    t.description AS t_description, "
                + "    t.duration_time AS t_duration_time, "
                + "    rt.id AS rt_id, "
                + "    rt.start_at AS rt_start_at, "
                + "    p.pending_index AS pending_index "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "LEFT JOIN ( "
                + "    SELECT "
                + "        id, "
                + "        ROW_NUMBER() OVER ( "
                + "            PARTITION BY date, time_id, theme_id "
                + "            ORDER BY created_at ASC "
                + "        ) AS pending_index "
                + "    FROM reservation "
                + "    WHERE status = 'PENDING' "
                + "      AND is_deleted = 0 "
                + ") p ON r.id = p.id "
                + "WHERE r.name = :username "
                + "  AND r.is_deleted = 0 "
                + "ORDER BY r.created_at DESC";

        return jdbcTemplate.query(sql, Map.of("username", username), queryResultRowMapper);
    }

    @Override
    public boolean existsByReservationTime(Long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=:timeId AND status='ACTIVE' AND is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId), Boolean.class));
    }

    @Override
    public boolean existsByReservationTimeAndThemeAndDate(Long timeId, Long themeId, LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id=:timeId AND theme_id=:themeId AND date=:date AND status='ACTIVE' AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("timeId", timeId, "themeId", themeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsByIdAndUsernameAndActiveOrPending(Long reservationId, String username) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id=:reservationId AND name=:username AND status IN ('ACTIVE', 'PENDING') AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("reservationId", reservationId, "username", username),
                        Boolean.class));
    }

    @Override
    public boolean existsByReservationTimeAndThemeAndDateAndIdNot(Long id, Long timeId, Long themeId,
                                                                  LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id != :id AND time_id=:timeId AND theme_id=:themeId AND date=:date AND status='ACTIVE' AND is_deleted = 0)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("id", id, "timeId", timeId, "themeId", themeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsByTheme(Long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND is_deleted = 0)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId), Boolean.class));
    }

    @Override
    public boolean existsActiveReservationByThemeAndTime(Long timeId, Long themeId, LocalDate date) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND time_id=:timeId AND date=:date AND status='ACTIVE')";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Map.of("themeId", themeId, "timeId", timeId, "date", date),
                        Boolean.class));
    }

    @Override
    public boolean existsPendingReservationByName(Long timeId, Long themeId, LocalDate date, String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id=:themeId AND time_id=:timeId AND date=:date AND status='PENDING' AND name=:name)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql,
                        Map.of("themeId", themeId, "timeId", timeId, "date", date, "name", name),
                        Boolean.class));
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id=:id";
        return jdbcTemplate.update(sql, Map.of("id", id));
    }

    @Override
    public void cancel(Reservation reservation) {
        String sql = "UPDATE reservation SET status = 'CANCELED', is_deleted=:id WHERE id = :id AND (status='ACTIVE' OR status='PENDING')";
        jdbcTemplate.update(sql, Map.of("id", reservation.getId()));
    }

    @Override
    public Optional<Reservation> findNextPendingReservation(LocalDate date, Long timeId, Long themeId) {
        String sql = "SELECT r.id AS r_id, r.name AS r_name, r.date AS r_date, r.status AS r_status, r.created_at AS r_created_at, "
                + "t.id AS t_id, t.name AS t_name, t.thumbnail_image_url AS t_thumbnail_image_url, "
                + "t.description AS t_description, t.duration_time AS t_duration_time, "
                + "rt.id AS rt_id, rt.start_at AS rt_start_at "
                + "FROM reservation r "
                + "INNER JOIN theme t ON r.theme_id = t.id "
                + "INNER JOIN reservation_time rt ON r.time_id = rt.id "
                + "WHERE r.date = :date AND r.time_id = :timeId AND r.theme_id = :themeId "
                + "AND r.status = 'PENDING' AND r.is_deleted = 0 "
                + "ORDER BY r.created_at ASC " // 👈 가장 먼저 대기 건 사람 순서대로 줄 세우기
                + "LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("timeId", timeId)
                .addValue("themeId", themeId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
