package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.service.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate,
                                     @Qualifier("reservationJdbcInsert") SimpleJdbcInsert simpleJdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = simpleJdbcInsert;
    }

    public List<Reservation> findAll() {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail,
                       u.id as user_id,
                       u.name as user_name,
                       u.email as user_email,
                       u.password as user_password,
                       u.role as user_role
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join users as u on r.user_id = u.id
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Reservation(
                        resultSet.getLong("reservation_id"),
                        LocalDate.parse(resultSet.getString("date")),
                        new ReservationTime(
                                resultSet.getLong("time_id"),
                                LocalTime.parse(resultSet.getString("time_value"))
                        ),
                        new Theme(
                                resultSet.getLong("theme_id"),
                                resultSet.getString("theme_name"),
                                resultSet.getString("theme_description"),
                                resultSet.getString("theme_thumbnail")
                        ),
                        new Member(
                                resultSet.getLong("user_id"),
                                new Name(resultSet.getString("user_name")),
                                new Email(resultSet.getString("user_email")),
                                new Password(resultSet.getString("user_password")),
                                Role.valueOf(resultSet.getString("user_role"))
                        )));
    }

    @Override
    public Reservation save(Reservation reservation) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getReservationTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("user_id", reservation.getMember().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Reservation(id, reservation.getDate(),
                new ReservationTime(reservation.getReservationTime().getId(),
                        reservation.getStartAt()),
                reservation.getTheme(), reservation.getMember());
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                select r.id as reservation_id, 
                       r.date, 
                       t.id as time_id, 
                       t.start_at as time_value, 
                       th.id as theme_id, 
                       th.name as theme_name, 
                       th.description as theme_description, 
                       th.thumbnail as theme_thumbnail,
                       u.id as user_id,
                       u.name as user_name,
                       u.email as user_email,
                       u.password as user_password,
                       u.role as user_role
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join users as u on r.user_id  = u.id
                where r.id = ?
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                        new Reservation(
                                resultSet.getLong("reservation_id"),
                                LocalDate.parse(resultSet.getString("date")),
                                new ReservationTime(
                                        resultSet.getLong("time_id"),
                                        LocalTime.parse(resultSet.getString("time_value"))
                                ),
                                new Theme(
                                        resultSet.getLong("theme_id"),
                                        resultSet.getString("theme_name"),
                                        resultSet.getString("theme_description"),
                                        resultSet.getString("theme_thumbnail")
                                ),
                                new Member(
                                        resultSet.getLong("user_id"),
                                        new Name(resultSet.getString("user_name")),
                                        new Email(resultSet.getString("user_email")),
                                        new Password(resultSet.getString("user_password")),
                                        Role.valueOf(resultSet.getString("user_role"))
                                )), id)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByReservationDateAndReservationTimeId(ReservationDate reservationDate, Long timeId) {
        String sql = "SELECT 1 FROM reservation WHERE date = ? AND time_id = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), reservationDate.getDate(), timeId);
        return !result.isEmpty();
    }

    @Override
    public boolean existsByReservationTimeId(Long timeId) {
        String sql = "SELECT 1 FROM reservation WHERE time_id = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), timeId);
        return !result.isEmpty();
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT 1 FROM reservation WHERE theme_id = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), themeId);
        return !result.isEmpty();
    }

    @Override
    public List<Reservation> findByFilter(Long memberId, Long themeId, LocalDate start, LocalDate end) {
        String sql = """
                select r.id as reservation_id,
                       r.date,
                       t.id as time_id,
                       t.start_at as time_value,
                       th.id as theme_id,
                       th.name as theme_name,
                       th.description as theme_description,
                       th.thumbnail as theme_thumbnail,
                       u.id as user_id,
                       u.name as user_name,
                       u.email as user_email,
                       u.password as user_password,
                       u.role as user_role
                from reservation as r
                inner join reservation_time as t on r.time_id = t.id
                inner join theme as th on r.theme_id = th.id
                inner join users as u on r.user_id = u.id
                where r.user_id = ?
                  and r.theme_id = ?
                  and r.date between ? and ?
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Reservation(
                        resultSet.getLong("reservation_id"),
                        LocalDate.parse(resultSet.getString("date")),
                        new ReservationTime(
                                resultSet.getLong("time_id"),
                                LocalTime.parse(resultSet.getString("time_value"))
                        ),
                        new Theme(
                                resultSet.getLong("theme_id"),
                                resultSet.getString("theme_name"),
                                resultSet.getString("theme_description"),
                                resultSet.getString("theme_thumbnail")
                        ),
                        new Member(
                                resultSet.getLong("user_id"),
                                new Name(resultSet.getString("user_name")),
                                new Email(resultSet.getString("user_email")),
                                new Password(resultSet.getString("user_password")),
                                Role.valueOf(resultSet.getString("user_role"))
                        )), memberId, themeId, start.toString(), end.toString());
    }
}
