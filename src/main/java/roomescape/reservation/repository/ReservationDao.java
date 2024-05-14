package roomescape.reservation.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao implements ReservationRepository {

    private static final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("id"),
            new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password")
            ),
            resultSet.getDate("date").toLocalDate(),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime()
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            )
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", reservation.getMember())
                .addValue("date", reservation.getDate())
                .addValue("member_id", reservation.getMember().getId())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());

        Long id = simpleJdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();

        return new Reservation(id, reservation);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r
                    INNER JOIN reservation_time AS t
                        ON r.time_id = t.id
                    INNER JOIN theme AS th
                        ON r.theme_id = th.id
                    INNER JOIN member AS m
                        ON r.member_id = m.id""";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        try {
            String sql = """
                    SELECT
                        r.id AS reservation_id,
                        m.id AS member_id,
                        m.name AS member_name,
                        m.email AS member_email,
                        m.password AS member_password,
                        r.date,
                        t.id AS time_id,
                        t.start_at AS start_at,
                        th.id AS theme_id,
                        th.name AS theme_name,
                        th.description AS theme_description,
                        th.thumbnail AS theme_thumbnail
                    FROM reservation AS r
                        INNER JOIN reservation_time AS t
                            ON r.time_id = t.id
                        INNER JOIN theme AS th
                            ON r.theme_id = th.id
                        INNER JOIN member AS m
                            ON r.member_id = m.id
                    WHERE r.id = ?""";
            Reservation reservation = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findByDateBetween(LocalDate start, LocalDate end) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r
                    INNER JOIN reservation_time AS t
                        ON r.time_id = t.id
                    INNER JOIN theme AS th
                        ON r.theme_id = th.id
                    INNER JOIN member AS m
                        ON r.member_id = m.id
                WHERE r.date BETWEEN ? AND ?""";
        return jdbcTemplate.query(sql, rowMapper, start, end);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r
                    INNER JOIN reservation_time AS t
                        ON r.time_id = t.id
                    INNER JOIN theme AS th
                        ON r.theme_id = th.id
                    INNER JOIN member AS m
                        ON r.member_id = m.id
                WHERE r.date = ? AND th.id = ?""";
        return jdbcTemplate.query(sql, rowMapper, date, themeId);
    }

    @Override
    public List<Reservation> findByDateBetweenAndMemberIdAndThemeId(
            LocalDate start,
            LocalDate end,
            Long memberId,
            Long themeId
    ) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.email AS member_email,
                    m.password AS member_password,
                    r.date,
                    t.id AS time_id,
                    t.start_at AS start_at,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r
                    INNER JOIN reservation_time AS t
                        ON r.time_id = t.id
                    INNER JOIN theme AS th
                        ON r.theme_id = th.id
                    INNER JOIN member AS m
                        ON r.member_id = m.id
                WHERE r.date BETWEEN ? AND ? AND th.id = ? AND m.id = ?""";
        return jdbcTemplate.query(sql, rowMapper, start, end, themeId, memberId);
    }

    @Override
    public Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        try {
            String sql = """
                    SELECT
                        r.id AS reservation_id,
                        m.id AS member_id,
                        m.name AS member_name,
                        m.email AS member_email,
                        m.password AS member_password,
                        r.date,
                        t.id AS time_id,
                        t.start_at AS start_at,
                        th.id AS theme_id,
                        th.name AS theme_name,
                        th.description AS theme_description,
                        th.thumbnail AS theme_thumbnail
                    FROM reservation AS r
                        INNER JOIN reservation_time AS t
                            ON r.time_id = t.id
                        INNER JOIN theme AS th
                            ON r.theme_id = th.id
                        INNER JOIN member AS m
                            ON r.member_id = m.id
                    WHERE r.date = ? AND r.time_id = ? AND r.theme_id = ?""";
            Reservation reservation = jdbcTemplate.queryForObject(sql, rowMapper, date, timeId, themeId);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
    }

    @Override
    public Boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS(SELECT id FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }
}
