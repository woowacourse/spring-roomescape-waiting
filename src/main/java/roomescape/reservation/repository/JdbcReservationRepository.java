package roomescape.reservation.repository;

import java.sql.Date;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.global.exception.NotFoundException;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeMapper;
import roomescape.theme.repository.entity.ThemeEntity;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeMapper;
import roomescape.time.repository.entity.ReservationTimeEntity;
import roomescape.reservation.repository.entity.ReservationEntity;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTimeEntity timeEntity = new ReservationTimeEntity(
                resultSet.getLong("time_id"),
                resultSet.getTime("time_start_at").toLocalTime()
        );

        ThemeEntity themeEntity = new ThemeEntity(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail_url")
        );

        ReservationEntity reservationEntity = new ReservationEntity(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("reservation_date").toLocalDate(),
                timeEntity,
                themeEntity
        );

        return ReservationMapper.toDomain(reservationEntity);
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = ReservationMapper.toEntity(reservation);

        String sql = """
                INSERT INTO reservation (name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entity.getName());
            ps.setDate(2, Date.valueOf(entity.getDate()));
            ps.setLong(3, entity.getTime().getId());
            ps.setLong(4, entity.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();

        ReservationEntity savedEntity = new ReservationEntity(
                id,
                entity.getName(),
                entity.getDate(),
                entity.getTime(),
                entity.getTheme()
        );

        return ReservationMapper.toDomain(savedEntity);
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE r.name = ?
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, name);
    }

    @Override
    public List<ReservationWithStatusResult> findAllByNameWithStatus(String name) {
        String sql = """
        SELECT id, name, reservation_date, time_id, time_start_at,
               theme_id, theme_name, theme_description, theme_thumbnail_url,
               status, waiting_order
        FROM (
            SELECT r.id AS id,
                   r.name AS name,
                   r.reservation_date,
                   r.time_id,
                   t.start_at AS time_start_at,
                   h.id AS theme_id,
                   h.name AS theme_name,
                   h.description AS theme_description,
                   h.thumbnail_url AS theme_thumbnail_url,
                   'reserved' AS status,
                   0 AS waiting_order
            FROM reservation r
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme h ON r.theme_id = h.id
            WHERE r.name = ?
            UNION ALL
            SELECT ranked.id,
                   ranked.name,
                   ranked.reservation_date,
                   ranked.time_id,
                   ranked.time_start_at,
                   ranked.theme_id,
                   ranked.theme_name,
                   ranked.theme_description,
                   ranked.theme_thumbnail_url,
                   ranked.status,
                   ranked.waiting_order
            FROM (
                SELECT rw.id AS id,
                       rw.name AS name,
                       rw.reservation_date AS reservation_date,
                       rw.time_id,
                       t.start_at AS time_start_at,
                       h.id AS theme_id,
                       h.name AS theme_name,
                       h.description AS theme_description,
                       h.thumbnail_url AS theme_thumbnail_url,
                       'waiting' AS status,
                       ROW_NUMBER() OVER (PARTITION BY rw.reservation_date, rw.time_id, rw.theme_id ORDER BY rw.id) AS waiting_order
                FROM reservation_waiting rw
                INNER JOIN reservation_time t ON rw.time_id = t.id
                INNER JOIN theme h ON rw.theme_id = h.id
            ) ranked
            WHERE ranked.name = ?
        ) combined
        ORDER BY reservation_date, time_id, theme_id, status
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReservationTime time = ReservationTimeMapper.toDomain(new ReservationTimeEntity(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime()
            ));
            Theme theme = ThemeMapper.toDomain(new ThemeEntity(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url")
            ));
            return new ReservationWithStatusResult(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getDate("reservation_date").toLocalDate(),
                    time,
                    theme,
                    rs.getString("status"),
                    rs.getLong("waiting_order")
            );
        }, name, name);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE r.id = ?
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        WHERE  r.reservation_date = ? AND r.time_id = ? AND r.theme_id = ?
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER, date, timeId, themeId)
                .stream().findFirst();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE reservation_date = ? AND time_id = ? AND theme_id = ?
        )
        """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
        SELECT r.id AS reservation_id,
               r.name AS reservation_name,
               r.reservation_date,
               r.time_id,
               t.start_at AS time_start_at,
               h.id AS theme_id,
               h.name AS theme_name,
               h.description AS theme_description,
               h.thumbnail_url AS theme_thumbnail_url
        FROM reservation r
        INNER JOIN reservation_time t
          ON r.time_id = t.id
        INNER JOIN theme h
          ON r.theme_id = h.id
        """;

        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<PopularThemeQueryResult> findPopularThemes(LocalDate from, LocalDate to, int limit) {
        String sql = """
        SELECT t.id,
               t.name,
               t.description,
               t.thumbnail_url
        FROM reservation r
        INNER JOIN theme t
          ON r.theme_id = t.id
        WHERE r.reservation_date >= ?
          AND r.reservation_date <= ?
        GROUP BY t.id,
                 t.name,
                 t.description,
                 t.thumbnail_url
        ORDER BY COUNT(r.id) DESC,
                 t.id ASC
        LIMIT ?
        """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new PopularThemeQueryResult(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail_url")
                ),
                Date.valueOf(from),
                Date.valueOf(to),
                limit
        );
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndIdNot(LocalDate date, Long timeId, Long themeId, Long id) {
        String sql = """
        SELECT EXISTS (
            SELECT 1
            FROM reservation
            WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND id != ?
        )
        """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void update(Reservation reservation) {
        ReservationEntity entity = ReservationMapper.toEntity(reservation);
        String sql = """
                UPDATE reservation
                SET name = ?, reservation_date = ?, time_id = ?, theme_id = ?
                WHERE id = ?
                """;

        int affectedRow = jdbcTemplate.update(
                sql,
                entity.getName(),
                entity.getDate(),
                entity.getTime().getId(),
                entity.getTheme().getId(),
                entity.getId()
        );

        if (affectedRow == 0) {
            throw new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
               DELETE FROM reservation
               WHERE id = ?
               """;

        return jdbcTemplate.update(sql, id);
    }
}
