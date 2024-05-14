package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ReservationDao {
    private static final RowMapper<Reservation> rowMapper =
            (resultSet, rowNum) -> new Reservation(
                    resultSet.getLong("id"),
                    new Member(resultSet.getLong("member_id"),
                            resultSet.getString("email"),
                            resultSet.getString("name"),
                            resultSet.getString("role")),
                    LocalDate.parse(resultSet.getString("date")),
                    new TimeSlot(resultSet.getLong("time_id"), resultSet.getString("time_value")),
                    new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"), resultSet.getString("theme_description"), resultSet.getString("theme_thumbnail"))
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("date", "time_id", "theme_id", "member_id")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        String sql = """
                SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.email as email,
                m.name as name,
                m.role as role,
                r.date,
                t.id as time_id,
                t.start_at as time_value,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.thumbnail as theme_thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id
                INNER JOIN member as m ON r.member_id = m.id
                                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Long create(final Reservation reservation) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("member_id", reservation.getMember().getId());
        return jdbcInsert.executeAndReturnKey(parameterSource).longValue();
    }

    public void delete(final Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean isExists(final LocalDate date, final Long timeId, final Long themeId) {
        String sql = """
                SELECT
                    count(*)
                FROM reservation
                WHERE date = ? AND time_id = ? AND theme_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId) != 0;
    }

    public boolean isExistsTimeId(final Long timeId) {
        String sql = "select count(*) from reservation where time_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, timeId) != 0;
    }

    public boolean isExistsThemeId(final Long themeId) {
        String sql = "select count(*) from reservation where theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, themeId) != 0;
    }

    public List<Reservation> find(final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo) {
        String sql = """
                SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.email as email,
                m.name as name,
                m.role as role,
                r.date,
                t.id as time_id,
                t.start_at as time_value,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.thumbnail as theme_thumbnail
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON r.theme_id = th.id
                INNER JOIN member as m ON r.member_id = m.id
                WHERE theme_id = ? AND member_id = ? AND r.date BETWEEN ? AND ?
                """;
        return jdbcTemplate.query(sql, rowMapper, themeId, memberId, dateFrom, dateTo);
    }
}
