package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.LoginMember;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

@Repository
public class ReservationH2Repository implements ReservationRepository {

    private static final String TABLE_NAME = "RESERVATION";
    private static final int ANY_INTEGER_FOR_COUNTING = 0;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ReservationH2Repository(JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", reservation.getDate(DateTimeFormatter.ISO_DATE))
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("member_id", reservation.getLoginMember().getId());
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Reservation(id, reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(
                "SELECT r.id, r.date, r.time_id, r.theme_id, r.member_id, rt.start_at, t.name as theme_name, t.description, t.thumbnail, m.name as member_name, m.email, m.role "
                        + "FROM reservation as r "
                        + "inner join reservation_time as rt on r.time_id = rt.id "
                        + "inner join theme as t on r.theme_id = t.id "
                        + "inner join member as m on r.member_id = m.id ",
                getReservationRowMapper()
        );
    }

    @Override
    public List<Reservation> findAll(Long themeId, Long memberId) {
        return jdbcTemplate.query(
                "SELECT r.id, r.date, r.time_id, r.theme_id, r.member_id, rt.start_at, t.name as theme_name, t.description, t.thumbnail, m.name as member_name, m.email, m.role "
                        + "FROM reservation as r "
                        + "inner join reservation_time as rt on r.time_id = rt.id "
                        + "inner join theme as t on r.theme_id = t.id "
                        + "inner join member as m on r.member_id = m.id "
                        + "WHERE r.theme_id = ? AND r.member_id = ?",
                getReservationRowMapper(),
                themeId,
                memberId
        );
    }

    private RowMapper<Reservation> getReservationRowMapper() {
        return (resultSet, rowNum) -> {
            ReservationTime reservationTime = new ReservationTime(
                    resultSet.getLong("time_id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );
            Theme theme = new Theme(
                    resultSet.getLong("theme_id"),
                    new Name(resultSet.getString("theme_name")),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail")
            );
            LoginMember loginMember = new LoginMember(
                    resultSet.getLong("member_id"),
                    new roomescape.member.domain.Name(resultSet.getString("member_name")),
                    new Email(resultSet.getString("email")),
                    Role.getByDbValue(resultSet.getString("role"))
            );
            return new Reservation(
                    resultSet.getLong("id"),
                    LocalDate.parse(resultSet.getString("date")),
                    reservationTime,
                    theme,
                    loginMember
            );
        };
    }

    @Override
    public List<Long> findAlreadyBookedTimeIds(LocalDate date, Long themeId) {
        return jdbcTemplate.query(
                "SELECT time_id FROM reservation WHERE date = ? AND theme_id = ?",
                (resultSet, rowNum) -> resultSet.getLong("time_id"),
                date,
                themeId
        );
    }

    @Override
    public boolean isAlreadyBooked(Reservation reservation) {
        return !jdbcTemplate.query(
                "SELECT * FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                (resultSet, rowNum) -> ANY_INTEGER_FOR_COUNTING,
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        ).isEmpty();
    }


    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM RESERVATION WHERE id = ?", id);
    }
}
