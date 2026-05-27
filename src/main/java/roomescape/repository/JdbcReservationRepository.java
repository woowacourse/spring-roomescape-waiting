package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.RoomEscapeException;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    private static RowMapper<Reservation> getReservationRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime time = ReservationTime.of(resultSet.getLong("reservation_time_id"),
                    LocalTime.parse(resultSet.getString("time_value")));

            Theme theme = Theme.of(resultSet.getLong("reservation_theme_id"),
                    resultSet.getString("reservation_theme_name"),
                    resultSet.getString("reservation_theme_description"),
                    resultSet.getString("reservation_theme_image_url"));
            return Reservation.of(resultSet.getLong("reservation_id"), resultSet.getString("name"),
                    resultSet.getDate("date").toLocalDate(), time, theme);
        };
    }

    @Override
    public Reservation save(Reservation reservation) {
        long generatedKey = simpleJdbcInsert.executeAndReturnKey(
                new BeanPropertySqlParameterSource(reservation)).longValue();

        return Reservation.of(generatedKey, reservation.getName(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                    select r.id as reservation_id,   
                    r.name, r.date, 
                    t.id as reservation_time_id,
                    t.start_at as time_value,
                    th.id as reservation_theme_id,
                    th.name as reservation_theme_name,
                    th.description as reservation_theme_description,
                    th.image_url as reservation_theme_image_url
                    from reservation as r 
                    inner join reservation_time as t
                    on r.time_id = t.id 
                    inner join theme as th
                    on r.theme_id = th.id
                    where r.id = :id
                """;

        Map<String, Object> params = Map.of("id", id);

        List<Reservation> results = jdbcTemplate.query(sql, params, getReservationRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                    select r.id as reservation_id,   
                    r.name, r.date, 
                    t.id as reservation_time_id,
                    t.start_at as time_value,
                    th.id as reservation_theme_id,
                    th.name as reservation_theme_name,
                    th.description as reservation_theme_description,
                    th.image_url as reservation_theme_image_url
                    
                    from reservation as r 
                    inner join reservation_time as t
                    on r.time_id = t.id 
                    
                    inner join theme as th
                    on r.theme_id = th.id
                """;

        return jdbcTemplate.query(sql, getReservationRowMapper());
    }

    @Override
    public List<Reservation> findByName(String name) {
        String sql = """
                    select r.id as reservation_id,   
                    r.name, r.date, 
                    t.id as reservation_time_id,
                    t.start_at as time_value,
                    th.id as reservation_theme_id,
                    th.name as reservation_theme_name,
                    th.description as reservation_theme_description,
                    th.image_url as reservation_theme_image_url
                    from reservation as r 
                    inner join reservation_time as t
                    on r.time_id = t.id 
                    inner join theme as th
                    on r.theme_id = th.id
                    where r.name = :name
                """;

        Map<String, Object> params = Map.of("name", name);

        return jdbcTemplate.query(sql, params, getReservationRowMapper());
    }

    @Override
    public Optional<Reservation> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        String sql = """
                    select r.id as reservation_id,   
                    r.name, r.date, 
                    t.id as reservation_time_id,
                    t.start_at as time_value,
                    th.id as reservation_theme_id,
                    th.name as reservation_theme_name,
                    th.description as reservation_theme_description,
                    th.image_url as reservation_theme_image_url
                    from reservation as r 
                    inner join reservation_time as t
                    on r.time_id = t.id 
                    inner join theme as th
                    on r.theme_id = th.id
                    where r.date = :date
                    and t.id = :time_id
                    and th.id = :theme_id
                """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("date", date)
                .addValue("time_id", time.getId()).addValue("theme_id", theme.getId());

        List<Reservation> results = jdbcTemplate.query(sql, params, getReservationRowMapper());
        return results.stream().findFirst();
    }

    @Override
    public Reservation update(Long id, LocalDate date, ReservationTime time) {
        String sql = "update reservation set date = :date, time_id = :time_id where id = :id";
        SqlParameterSource params = new MapSqlParameterSource().addValue("date", date)
                .addValue("time_id", time.getId()).addValue("id", id);
        jdbcTemplate.update(sql, params);
        return findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    @Override
    public void delete(Long id) {
        String sql = "delete from reservation where id = :id";
        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        String sql = "select count(*) from reservation where time_id = :time_id";
        Map<String, Object> params = Map.of("time_id", timeId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count > 0;
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        String sql = "select count(*) from reservation where theme_id = :theme_id";
        Map<String, Object> params = Map.of("theme_id", themeId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count > 0;
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme) {
        String sql = "select count(*) from reservation where date = :date AND time_id = :time_id AND theme_id = :theme_id";
        SqlParameterSource params = new MapSqlParameterSource().addValue("date", date)
                .addValue("time_id", time.getId()).addValue("theme_id", theme.getId());
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count > 0;
    }
}
