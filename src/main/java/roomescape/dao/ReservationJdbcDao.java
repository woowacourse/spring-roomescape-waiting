package roomescape.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.member.Role;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.dto.reservation.ReservationExistenceCheck;
import roomescape.dto.reservation.ReservationFilterParam;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

@Repository
public class ReservationJdbcDao implements ReservationDao {

    private static final String RESERVATION_BASIC_SQL = """
                SELECT r.id AS reservation_id, r.member_id, r.date, 
                    t.id AS time_id, t.start_at AS time_value,
                    th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                    m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN member AS m ON r.member_id = m.id
                """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNumber) -> new Reservation(
            resultSet.getLong("reservation_id"),
            new Member(
                    resultSet.getLong("member_id"),
                    new Name(resultSet.getString("member_name")),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password"),
                    Role.valueOf(resultSet.getString("member_role"))
            ),
            resultSet.getString("date"),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    resultSet.getString("time_value")
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            ));

    public ReservationJdbcDao(final DataSource source) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMemberId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getReservationTimeId())
                .addValue("theme_id", reservation.getThemeId());
        final Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(Objects.requireNonNull(id), reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(RESERVATION_BASIC_SQL, rowMapper);
    }

    @Override
    public List<Reservation> findAllBy(final ReservationFilterParam param) {
        final StringBuilder sqlBuilder = new StringBuilder(RESERVATION_BASIC_SQL);
        sqlBuilder.append("WHERE 1=1 ");

        final MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        addFilterCondition(parameterSource, sqlBuilder, param.themeId(), "themeId", "AND r.theme_id = :themeId ");
        addFilterCondition(parameterSource, sqlBuilder, param.memberId(), "memberId", "AND r.member_id = :memberId ");
        addFilterCondition(parameterSource, sqlBuilder, param.dateFrom(), "dateFrom", "AND (r.date BETWEEN :dateFrom ");
        addFilterCondition(parameterSource, sqlBuilder, param.dateTo(), "dateTo", "AND :dateTo) ");
        return jdbcTemplate.query(sqlBuilder.toString(), parameterSource, rowMapper);
    }

    private void addFilterCondition(final MapSqlParameterSource parameterSource, final StringBuilder sqlBuilder, final Object param,
                                    final String paramName, final String condition) {
        if (param != null) {
            sqlBuilder.append(condition);
            parameterSource.addValue(paramName, param);
        }
    }

    @Override
    public List<Reservation> findAllBy(final ReservationExistenceCheck search) {
        String sql = RESERVATION_BASIC_SQL;
        sql += "WHERE `date` = :date AND r.time_id = :timeId AND r.theme_id = :themeId";

        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("date", search.date())
                .addValue("timeId", search.timeId())
                .addValue("themeId", search.themeId());
        return jdbcTemplate.query(sql, parameterSource, rowMapper);
    }

    @Override
    public boolean existById(final Long id) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = :id) AS is_exist";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.queryForObject(sql, parameterSource,
                (resultSet, rowNumber) -> resultSet.getBoolean("is_exist"));
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM reservation WHERE id = :id";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, parameterSource);
    }

    @Override
    public int countByTimeId(final Long timeId) {
        final String sql = "SELECT count(*) FROM reservation WHERE time_id = :timeId";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("timeId", timeId);
        return jdbcTemplate.queryForObject(sql, parameterSource, Integer.class);
    }

    public List<Long> findTimeIds(final AvailableReservationTimeSearch search) {
        final String sql = "SELECT time_id FROM reservation WHERE date = :date AND theme_id = :themeId";
        final SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("date", search.date())
                .addValue("themeId", search.themeId());
        return jdbcTemplate.query(sql, parameterSource,
                (resultSet, rowNumber) -> resultSet.getLong("time_id"));
    }
}
