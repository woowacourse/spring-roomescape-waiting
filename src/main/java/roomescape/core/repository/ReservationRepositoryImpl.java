package roomescape.core.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Role;
import roomescape.core.domain.Theme;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationRepositoryImpl(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Reservation reservation) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("member_id", reservation.getMemberId())
                .addValue("time_id", reservation.getTimeId())
                .addValue("theme_id", reservation.getThemeId());
        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public List<Reservation> findAll() {
        final String query = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    t.id as time_id,
                    t.start_at as time_value,
                    h.id as theme_id,
                    h.name as theme_name,
                    h.description as theme_description,
                    h.thumbnail as theme_thumbnail
                FROM reservation as r
                inner join member as m
                on r.member_id = m.id
                inner join reservation_time as t
                on r.time_id = t.id
                inner join theme as h
                on r.theme_id = h.id
                """;

        return jdbcTemplate.query(query, getReservationRowMapper());
    }

    private RowMapper<Reservation> getReservationRowMapper() {
        return (resultSet, rowNum) -> {
            final Long id = resultSet.getLong("id");
            final Long memberId = resultSet.getLong("member_id");
            final String name = resultSet.getString("member_name");
            final String email = resultSet.getString("member_email");
            final String password = resultSet.getString("member_password");
            final Role role = Role.valueOf(resultSet.getString("member_role"));
            final Member member = new Member(memberId, name, email, password, role);
            final String date = resultSet.getString("date");
            final Long timeId = resultSet.getLong("time_id");
            final String timeValue = resultSet.getString("time_value");
            final ReservationTime time = new ReservationTime(timeId, timeValue);
            final Long themeId = resultSet.getLong("theme_id");
            final String themeName = resultSet.getString("theme_name");
            final String themeDescription = resultSet.getString("theme_description");
            final String themeThumbnail = resultSet.getString("theme_thumbnail");
            final Theme theme = new Theme(themeId, themeName, themeDescription, themeThumbnail);

            return new Reservation(id, member, date, time, theme);
        };
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final String date, final long themeId) {
        final String query = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    t.id as time_id,
                    t.start_at as time_value,
                    h.id as theme_id,
                    h.name as theme_name,
                    h.description as theme_description,
                    h.thumbnail as theme_thumbnail
                FROM reservation as r
                inner join member as m
                on r.member_id = m.id
                inner join reservation_time as t
                on r.time_id = t.id
                inner join theme as h
                on r.theme_id = h.id
                WHERE r.date = ? AND r.theme_id = ?
                """;
        return jdbcTemplate.query(query, getReservationRowMapper(), date, themeId);
    }

    @Override
    public List<Reservation> findAllByMemberAndThemeAndPeriod(final Long memberId, final Long themeId,
                                                              final String dateFrom, final String dateTo) {

        String query = """
                SELECT
                    r.id as reservation_id,
                    r.date,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    t.id as time_id,
                    t.start_at as time_value,
                    h.id as theme_id,
                    h.name as theme_name,
                    h.description as theme_description,
                    h.thumbnail as theme_thumbnail
                FROM reservation as r
                inner join member as m
                on r.member_id = m.id
                inner join reservation_time as t
                on r.time_id = t.id
                inner join theme as h
                on r.theme_id = h.id
                """;
        query += buildConditionalQuery(memberId, themeId, dateFrom, dateTo);

        return jdbcTemplate.query(query, getReservationRowMapper());
    }

    private String buildConditionalQuery(final Long memberId, final Long themeId, final String dateFrom,
                                         final String dateTo) {
        final List<String> queries = new ArrayList<>();

        if (memberId != null) {
            queries.add("r.member_id = " + memberId);
        }
        if (themeId != null) {
            queries.add("r.theme_id = " + themeId);
        }
        if (!dateFrom.isEmpty() && !dateTo.isEmpty()) {
            queries.add("r.date BETWEEN '" + dateFrom + "' AND '" + dateTo + "'");
        }

        if (queries.isEmpty()) {
            return "";
        }
        return "WHERE " + String.join(" AND ", queries);
    }

    @Override
    public List<Theme> findPopularTheme(final LocalDate today, final LocalDate lastWeek) {
        final String query = """
                SELECT t.id, t.name, t.description, t.thumbnail
                FROM theme as t
                JOIN reservation as r ON t.id = r.theme_id
                WHERE r.date BETWEEN ? AND ?
                GROUP BY t.id
                ORDER BY count(r.id) DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(query, getThemeRowMapper(), lastWeek, today);
    }

    private RowMapper<Theme> getThemeRowMapper() {
        return (resultSet, rowNum) -> {
            final Long themeId = resultSet.getLong("id");
            final String name = resultSet.getString("name");
            final String description = resultSet.getString("description");
            final String thumbnail = resultSet.getString("thumbnail");

            return new Theme(themeId, name, description, thumbnail);
        };
    }

    @Override
    public Integer countByTimeId(final long timeId) {
        final String query = """
                SELECT count(*)
                FROM reservation
                WHERE time_id = ?
                """;
        return jdbcTemplate.queryForObject(query, Integer.class, timeId);
    }

    @Override
    public Integer countByThemeId(final long themeId) {
        final String query = """
                SELECT count(*)
                FROM reservation
                WHERE theme_id = ?
                """;
        return jdbcTemplate.queryForObject(query, Integer.class, themeId);
    }

    @Override
    public Integer countByDateAndTimeIdAndThemeId(final String date, final long timeId, final long themeId) {
        final String query = """
                SELECT count(*)
                FROM reservation as r
                inner join reservation_time as t
                on r.time_id = t.id
                inner join theme as m
                on r.theme_id = m.id
                WHERE r.date = ? and t.id = ? and m.id = ?
                """;

        return jdbcTemplate.queryForObject(query, Integer.class, date, timeId, themeId);
    }

    @Override
    public void deleteById(final long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }
}
