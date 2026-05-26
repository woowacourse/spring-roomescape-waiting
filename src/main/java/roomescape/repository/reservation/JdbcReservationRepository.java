package roomescape.repository.reservation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

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

        return Reservation.of(
                resultSet.getLong("id"),
                resultSet.getString("reservation_name"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                reservationTime
        );
    };

    private static final RowMapper<Theme> themeRowMapper = (resultSet, rowNum) -> {
        return Theme.of(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
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
                       r.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                """;

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findAllByName(final String name) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                WHERE r.name = ?
                ORDER BY r.date, rt.start_at
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, name);
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                WHERE r.id = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByIdAndName(final long id, final String name) {
        String sql = """
                SELECT r.id,
                       r.name AS reservation_name,
                       r.date,
                       rt.id AS time_id,
                       rt.start_at,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description,
                       t.thumbnail_url
                FROM reservation AS r
                INNER JOIN reservation_time AS rt ON r.time_id = rt.id
                INNER JOIN theme AS t ON r.theme_id = t.id
                WHERE r.id = ? AND r.name = ?
                """;

        return jdbcTemplate.query(sql, reservationRowMapper, id, name)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteById(final long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        String sql = "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, reservation.getName());
            preparedStatement.setDate(2, Date.valueOf(reservation.getDate()));
            preparedStatement.setLong(3, reservation.getTheme().getId());
            preparedStatement.setLong(4, reservation.getTime().getId());
            return preparedStatement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("[ERROR] 예약 ID를 생성하지 못했습니다.");
        }

        return reservation.withId(key.longValue());
    }

    @Override
    public Reservation update(final Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET date = ?, time_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getId()
        );

        return reservation;
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeId(final LocalDate date, final long themeId, final long timeId) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE date = ? AND theme_id = ? AND time_id = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                Date.valueOf(date),
                themeId,
                timeId
        ));
    }

    @Override
    public boolean existsByDateAndThemeIdAndTimeIdExcludingId(
            final LocalDate date,
            final long themeId,
            final long timeId,
            final long reservationId
    ) {
        final String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE date = ? AND theme_id = ? AND time_id = ? AND id <> ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                Date.valueOf(date),
                themeId,
                timeId,
                reservationId
        ));
    }

    @Override
    public List<Long> findReservedTimeIdsByDateAndThemeId(final LocalDate date, final long themeId) {
        final String sql = """
                SELECT time_id
                FROM reservation
                WHERE date = ? AND theme_id = ?
                """;

        return jdbcTemplate.queryForList(sql, Long.class, Date.valueOf(date), themeId);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                timeId
        ));
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                themeId
        ));
    }

    @Override
    public Long findReservationIdByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        final String sql = """
                              SELECT id FROM reservation
                              WHERE date = ? AND theme_id = ? AND time_id = ?
                          """;

        return jdbcTemplate.queryForObject(sql, Long.class, date, themeId, timeId);
    }
}
