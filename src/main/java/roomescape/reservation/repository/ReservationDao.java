package roomescape.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roomescape.common.template.AbstractDaoTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@Repository
public class ReservationDao extends AbstractDaoTemplate<Reservation> {

    private static final String TABLE_NAME = "reservation";
    private static final String BASE_SELECT_SQL = """
            select reservation.id as reservation_id, reservation.date,
                   member.id as member_id, member.name, member.email, member.password, member.role,
                   reservation_time.id as reservation_time_id, reservation_time.start_at,
                   theme.id as theme_id, theme.name as theme_name, theme.description, theme.thumbnail
            from reservation
            inner join reservation_time on reservation.time_id = reservation_time.id
            inner join theme on reservation.theme_id = theme.id
            inner join member on reservation.member_id = member.id
            """;

    @Autowired
    public ReservationDao(final NamedParameterJdbcTemplate jdbcTemplate, final DataSource dataSource) {
        super(jdbcTemplate, TABLE_NAME, dataSource);
    }

    @Override
    protected RowMapper<Reservation> rowMapper() {
        return this::mapRowToReservation;
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, rowMapper());
    }

    public Optional<Reservation> findById(final Long id) {
        String sql = BASE_SELECT_SQL + " where reservation.id = :reservation_id";
        return executeQueryForObject(sql, Map.of("reservation_id", id));
    }

    public List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId) {
        String sql = BASE_SELECT_SQL + " where theme.id = :theme_id and reservation.date = :date";
        return jdbcTemplate.query(
                sql,
                Map.of("theme_id", themeId, "date", date),
                rowMapper()
        );
    }

    public List<Reservation> findByThemeIdAndMemberIdAndBetweenDate(
            final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo
    ) {
        String sql = BASE_SELECT_SQL + """
                    where reservation.theme_id = :theme_id and reservation.member_id = :member_id
                    and reservation.date between :date_from and :date_to
                """;
        return jdbcTemplate.query(sql,
                Map.of(
                        "theme_id", themeId,
                        "member_id", memberId,
                        "date_from", dateFrom,
                        "date_to", dateTo
                ), rowMapper());
    }

    public Reservation save(final Reservation reservation) {
        Map<String, Object> params = Map.of(
                "date", reservation.getReservationDate(),
                "time_id", reservation.getReservationTimeId(),
                "theme_id", reservation.getThemeId(),
                "member_id", reservation.getMember().getId()
        );

        long reservationId = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(
                reservationId,
                reservation.getMember(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
    }

    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        String sql = "select count(*) from reservation where date = :date and time_id = :time_id and theme_id = :theme_id";
        int count = jdbcTemplate.queryForObject(
                sql,
                Map.of("date", date, "time_id", timeId, "theme_id", themeId),
                Integer.class
        );
        return count == 1;
    }

    public boolean existsByTimeId(final Long timeId) {
        return existsBy("time_id", timeId);
    }

    public boolean existsByThemeId(final Long themeId) {
        return existsBy("theme_id", themeId);
    }

    private Reservation mapRowToReservation(final ResultSet resultSet, final int rowNum) throws SQLException {
        return new Reservation(
                resultSet.getLong("reservation_id"),
                new Member(
                        resultSet.getLong("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        Role.valueOf(resultSet.getString("role"))
                ),
                LocalDate.parse(resultSet.getString("date")),
                new ReservationTime(
                        resultSet.getLong("reservation_time_id"),
                        LocalTime.parse(resultSet.getString("start_at"))
                ),
                new Theme(
                        resultSet.getLong("theme_id"),
                        resultSet.getString("theme_name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail")
                )
        );
    }
}
