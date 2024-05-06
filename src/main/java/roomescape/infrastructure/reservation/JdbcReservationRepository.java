package roomescape.infrastructure.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.infrastructure.reservation.rowmapper.ReservationRowMapper;
import roomescape.infrastructure.utils.DynamicQueryBuilder;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private static final String FIND_ALL_SQL = """
            select r.id as reservation_id, member_id, date, time_id, start_at, created_at, theme_id,
            m.id as member_id, m.name as member_name, email, password,
            t.name as theme_name, description, thumbnail
            from reservation as r
            left join member as m on member_id = m.id
            left join reservation_time as rt on time_id = rt.id
            left join theme as t on theme_id = t.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingColumns("member_id", "date", "time_id", "theme_id", "created_at")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Reservation> findById(long id) {
        String whereClause = "where r.id = ?";
        try {
            Reservation reservation = jdbcTemplate.queryForObject(
                    FIND_ALL_SQL + whereClause, (rs, rowNum) -> ReservationRowMapper.joinedMapRow(rs), id
            );
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, (rs, rowNum) -> ReservationRowMapper.joinedMapRow(rs));
    }

    @Override
    public Reservation create(Reservation reservation) {
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();
        Member member = reservation.getMember();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("member_id", member.getId())
                .addValue("date", reservation.getDate())
                .addValue("created_at", reservation.getCreatedAt())
                .addValue("time_id", time.getId())
                .addValue("theme_id", theme.getId());
        long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
        return reservation.withId(id);
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from reservation where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        String sql = "select exists(select 1 from reservation where time_id = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, timeId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean existsBy(LocalDate date, long timeId, long themeId) {
        String sql = "select exists(select 1 from reservation where date = ? and time_id = ? and theme_id = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public List<Reservation> findByMemberAndThemeBetweenDates(Long memberId, Long themeId,
                                                              LocalDate startDate, LocalDate endDate) {
        DynamicQueryBuilder query = DynamicQueryBuilder.where()
                .equals("member_id", memberId)
                .equals("theme_id", themeId)
                .greaterOrEqualThan("date", startDate)
                .lessOrEqualThan("date", endDate);

        return jdbcTemplate.query(
                FIND_ALL_SQL + query.toSql(),
                (rs, rowNum) -> ReservationRowMapper.joinedMapRow(rs),
                query.getParameters()
        );
    }
}
