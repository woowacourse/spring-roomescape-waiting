package roomescape.repository.reservation.legacy;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.ReservationRepository;

public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNumber) -> {
        long reservationId = resultSet.getLong("reservation_id");
        String name = resultSet.getString("name");
        LocalDate date = resultSet.getDate("date").toLocalDate();
        long timeId = resultSet.getLong("time_id");
        LocalTime timeValue = resultSet.getTime("time_value").toLocalTime();

        ReservationTime reservationTime = new ReservationTime(timeId, timeValue);

        long themeId = resultSet.getLong("theme_id");
        String themeName = resultSet.getString("theme_name");
        String themeDescription = resultSet.getString("theme_description");
        String themeThumbnail = resultSet.getString("theme_thumbnail");
        Theme theme = new Theme(themeId, themeName, themeDescription, themeThumbnail);

        return new Reservation(reservationId, name, date, reservationTime, theme);
    };

    @Override
    public long save(Reservation reservation) {
        String sql = "insert into reservation (name,date,time_id, theme_id) values(?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getReservationTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                select 
                    r.id as reservation_id,
                        r.name,
                        r.date,
                        rt.id as time_id,
                        rt.start_at as time_value,
                        t.id as theme_id,
                        t.name as theme_name,
                        t.description as theme_description,
                        t.thumbnail as theme_thumbnail
                    from reservation as r
                    inner join reservation_time as rt
                    on r.time_id = rt.id
                    inner join theme as t
                    on r.theme_id = t.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation where id=?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(Reservation reservation) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class,
                reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId());
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, themeId);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = """
                select 
                    r.id as reservation_id,
                        r.name,
                        r.date,
                        rt.id as time_id,
                        rt.start_at as time_value,
                        t.id as theme_id,
                        t.name as theme_name,
                        t.description as theme_description,
                        t.thumbnail as theme_thumbnail
                    from reservation as r
                    inner join reservation_time as rt
                    on r.time_id = rt.id
                    inner join theme as t
                    on r.theme_id = t.id
                where date = ? and theme_id = ?               
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, date, themeId);
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String sql = """
                select 
                    r.id as reservation_id,
                        r.name,
                        r.date,
                        rt.id as time_id,
                        rt.start_at as time_value,
                        t.id as theme_id,
                        t.name as theme_name,
                        t.description as theme_description,
                        t.thumbnail as theme_thumbnail
                    from reservation as r
                    inner join reservation_time as rt
                    on r.time_id = rt.id
                    inner join theme as t
                    on r.theme_id = t.id
                where r.id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, reservationRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end) {
        String sql = """
                select 
                    r.id as reservation_id,
                        r.name,
                        r.date,
                        rt.id as time_id,
                        rt.start_at as time_value,
                        t.id as theme_id,
                        t.name as theme_name,
                        t.description as theme_description,
                        t.thumbnail as theme_thumbnail
                    from reservation as r
                    inner join reservation_time as rt
                    on r.time_id = rt.id
                    inner join theme as t
                    on r.theme_id = t.id
                    where r.date between ? and ?
                """;
        return jdbcTemplate.query(sql, reservationRowMapper, start, end);
    }
}
