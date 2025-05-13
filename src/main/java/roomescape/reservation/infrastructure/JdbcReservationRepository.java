package roomescape.reservation.infrastructure;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@Primary
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> Reservation.createWithId(
            resultSet.getLong("reservation_id"),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.createWithId(
                    resultSet.getLong("time_id"),
                    resultSet.getTime("time_value").toLocalTime()
            ),
            Theme.createWithId(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            ),
            Member.createWithId(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("email"),
                    resultSet.getString("password")
            )
    );

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final DataSource dataSource) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(final Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date", Date.valueOf(reservation.getDate()));
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());
        parameters.put("member_id", reservation.getMember().getId());

        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public List<Reservation> findBy(final LocalDate date, final Long themeId) {
        String sql = """               
                SELECT
                    r.id as reservation_id,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description,
                    th.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON th.id = r.theme_id
                INNER JOIN member as m ON m.id = r.member_id
                WHERE th.id = :themeId AND r.date = :date
                """;
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", Date.valueOf(date));

        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findBy(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        String sql = """               
                SELECT
                    r.id as reservation_id,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description,
                    th.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id
                INNER JOIN theme as th ON th.id = r.theme_id
                INNER JOIN member as m ON m.id = r.member_id
                WHERE m.id = :memberId AND th.id = :themeId AND r.date >= :from AND r.date <= :to
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId)
                .addValue("themeId", themeId)
                .addValue("from", from)
                .addValue("to", to);

        return namedParameterJdbcTemplate.query(sql, param, ROW_MAPPER);
    }

    @Override
    public boolean deleteById(final Long id) {
        String sql = "DELETE FROM reservation WHERE id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);

        int count = namedParameterJdbcTemplate.update(sql, param);
        return count != 0;
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """               
                SELECT
                    r.id as reservation_id,
                    r.date,
                    t.id as time_id,
                    t.start_at as time_value,
                    th.id as theme_id,
                    th.name as theme_name,
                    th.description,
                    th.thumbnail,
                    m.id as member_id,
                    m.name as member_name,
                    m.email,
                    m.password
                FROM reservation as r
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON th.id = r.theme_id
                INNER JOIN member as m ON m.id = r.member_id
                """;

        return namedParameterJdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public boolean existByReservationTimeId(final Long timeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM reservation
                    WHERE time_id = :timeId
                )
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("timeId", timeId);

        return namedParameterJdbcTemplate.queryForObject(sql, param, Boolean.class).booleanValue();
    }

    @Override
    public boolean existBy(final Long themeId, final LocalDate date, final LocalTime time) {
        String sql = """               
                SELECT EXISTS(
                    SELECT 1 FROM reservation as r
                    INNER JOIN reservation_time as t ON r.time_id = t.id
                    INNER JOIN theme as th ON r.theme_id = th.id
                    INNER JOIN member as m ON m.id = r.member_id
                    WHERE r.date = :date and t.start_at = :startAt and th.id = :themeId
                )
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("date", Date.valueOf(date))
                .addValue("startAt", time)
                .addValue("themeId", themeId);

        return namedParameterJdbcTemplate.queryForObject(sql, param, Boolean.class).booleanValue();
    }

    @Override
    public boolean existBy(final Long themeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM reservation
                    WHERE theme_id = :themeId
                )
                """;

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("themeId", themeId);

        return namedParameterJdbcTemplate.queryForObject(sql, param, Boolean.class).booleanValue();
    }
}
