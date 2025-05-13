package roomescape.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String DEFUALT_SELECT_SQL = """
            select
                r.id as reservation_id,
                m.id as member_id,
                m.name,
                m.email,
                m.password,
                m.role,
                r.`date`,
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
            inner join member as m
            on r.member_id = m.id
            """;

    private static final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNumber) -> {
        long reservationId = resultSet.getLong("reservation_id");

        Member member = new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                Role.valueOf(resultSet.getString("role"))
        );

        LocalDate date = resultSet.getDate("date").toLocalDate();
        long timeId = resultSet.getLong("time_id");
        LocalTime timeValue = resultSet.getTime("time_value").toLocalTime();

        ReservationTime reservationTime = new ReservationTime(timeId, timeValue);

        long themeId = resultSet.getLong("theme_id");
        String themeName = resultSet.getString("theme_name");
        String themeDescription = resultSet.getString("theme_description");
        String themeThumbnail = resultSet.getString("theme_thumbnail");
        Theme theme = new Theme(themeId, themeName, themeDescription, themeThumbnail);

        return new Reservation(reservationId, member, date, reservationTime, theme);
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Reservation add(Reservation reservation) {
        String sql = "insert into reservation (member_id, `date`, time_id, theme_id) values(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getMember().getId());
            ps.setDate(2, Date.valueOf(reservation.getDate()));
            ps.setLong(3, reservation.getReservationTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();

        return new Reservation(
                generatedId,
                reservation.getMember(),
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(DEFUALT_SELECT_SQL, reservationRowMapper);
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String sql = DEFUALT_SELECT_SQL + " where r.id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, reservationRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        String sql = DEFUALT_SELECT_SQL + " where `date` = ? and theme_id = ?";
        return jdbcTemplate.query(sql, reservationRowMapper, date, themeId);
    }

    @Override
    public List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end) {
        String sql = DEFUALT_SELECT_SQL + " where r.`date` between ? and ?";
        return jdbcTemplate.query(sql, reservationRowMapper, start, end);
    }

    @Override
    public List<Reservation> findAllByFilter(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sqlBuilder = new StringBuilder(DEFUALT_SELECT_SQL);
        List<Object> args = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (memberId != null) {
            conditions.add("r.member_id = ?");
            args.add(memberId);
        }
        if (themeId != null) {
            conditions.add("r.theme_id = ?");
            args.add(themeId);
        }
        if (dateFrom != null && dateTo != null) {
            conditions.add("r.`date` between ? and ?");
            args.add(dateFrom);
            args.add(dateTo);
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append(" where ").append(String.join(" and ", conditions));
        }

        return jdbcTemplate.query(
                sqlBuilder.toString(),
                reservationRowMapper,
                args.toArray()
        );
    }

    @Override
    public boolean existsByTimeId(Long id) {
        String sql = "select count(id) from reservation where time_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        String sql = "select count(id) from reservation where theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDateAndTimeIdAndTheme(Reservation reservation) {
        String sql = "select count(id) from reservation where `date` = ? and time_id = ? and theme_id = ?";

        Integer count = jdbcTemplate.queryForObject(sql,
                Integer.class,
                reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId());
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
