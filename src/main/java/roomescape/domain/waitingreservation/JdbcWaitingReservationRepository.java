package roomescape.domain.waitingreservation;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waitingreservation.dto.WaitingReservationWithRank;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingReservationRepository implements WaitingReservationRepository {

    private static final String INSERT_SQL = "insert into waiting_reservation(name, date_id, time_id, theme_id, created_at) values (?, ?, ?, ?, ?)";

    private static final String EXIST_BY_NAME_DATE_TIME_THEME_SQL =
            """
                    select exists(
                    select 1
                    from waiting_reservation
                    where name = ? and date_id = ? and time_id = ? and theme_id = ?
                    );
                    """;

    private static final String FIND_OLDEST_BY_SLOT_SQL =
            """
                    select wr.id, wr.name, wr.created_at,
                           rd.id as date_id, rd.play_day,
                           rt.id as time_id, rt.start_at,
                           th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
                    from waiting_reservation wr
                    join reservation_date rd on wr.date_id = rd.id
                    join reservation_time rt on wr.time_id = rt.id
                    join theme th on wr.theme_id = th.id
                    where wr.date_id = ? and wr.time_id = ? and wr.theme_id = ?
                    order by wr.created_at asc, wr.id asc
                    limit 1
                    """;

    private static final String FIND_ALL_BY_NAME_WITH_RANK_SQL =
            """
                    select ranked.id, ranked.name, ranked.created_at, ranked.waiting_rank,
                           rd.id as date_id, rd.play_day,
                           rt.id as time_id, rt.start_at,
                           th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url
                    from (
                        select wr.id, wr.name, wr.date_id, wr.time_id, wr.theme_id, wr.created_at,
                               row_number() over (
                                   partition by wr.date_id, wr.time_id, wr.theme_id
                                   order by wr.created_at asc, wr.id asc
                               ) as waiting_rank
                        from waiting_reservation wr
                    ) ranked
                    join reservation_date rd on ranked.date_id = rd.id
                    join reservation_time rt on ranked.time_id = rt.id
                    join theme th on ranked.theme_id = th.id
                    where ranked.name = ?
                    order by rd.play_day asc, rt.start_at asc, ranked.id asc
                    """;

    private static final String DELETE_BY_ID_SQL = "delete from waiting_reservation where id = ?";

    private static final String FIND_BY_ID_SQL =
            """
                    select wr.id, wr.name,
                           rd.id as date_id, rd.play_day,
                           rt.id as time_id, rt.start_at,
                           th.id as theme_id, th.name as theme_name, th.content as theme_content, th.url as theme_url,
                           wr.created_at
                    from waiting_reservation wr
                    join reservation_date rd on wr.date_id = rd.id
                    join reservation_time rt on wr.time_id = rt.id
                    join theme th on wr.theme_id = th.id
                    where wr.id  = ?
                    """;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public WaitingReservation save(WaitingReservation waitingReservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, waitingReservation.getName());
            ps.setLong(2, waitingReservation.getDate().getId());
            ps.setLong(3, waitingReservation.getTime().getId());
            ps.setLong(4, waitingReservation.getTheme().getId());
            ps.setTimestamp(5, Timestamp.valueOf(waitingReservation.getCreatedAt()));
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return WaitingReservation.of(
                id,
                waitingReservation.getName(),
                waitingReservation.getDate(),
                waitingReservation.getTime(),
                waitingReservation.getTheme(),
                waitingReservation.getCreatedAt()
        );
    }

    @Override
    public boolean existsByNameAndDateIdAndTimeIdAndThemeId(String name, long dateId, long timeId, long themeId) {
        return jdbcTemplate.queryForObject(EXIST_BY_NAME_DATE_TIME_THEME_SQL, Boolean.class, name, dateId, timeId,
                themeId);
    }

    @Override
    public Optional<WaitingReservation> findOldestBySlot(long dateId, long timeId, long themeId) {
        return jdbcTemplate.query(FIND_OLDEST_BY_SLOT_SQL, waitingReservationRowMapper(), dateId, timeId, themeId)
                .stream()
                .findFirst();
    }

    @Override
    public List<WaitingReservationWithRank> findAllByNameWithRank(String name) {
        return jdbcTemplate.query(FIND_ALL_BY_NAME_WITH_RANK_SQL, waitingReservationWithRankRowMapper(), name);
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public Optional<WaitingReservation> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, waitingReservationRowMapper(), id)
                .stream()
                .findFirst();
    }

    private RowMapper<WaitingReservationWithRank> waitingReservationWithRankRowMapper() {
        return (rs, rowNum) -> new WaitingReservationWithRank(
                waitingReservationRowMapper().mapRow(rs, rowNum),
                rs.getLong("waiting_rank")
        );
    }

    private RowMapper<WaitingReservation> waitingReservationRowMapper() {
        return (rs, rowNum) -> WaitingReservation.of(
                rs.getLong("id"),
                rs.getString("name"),
                ReservationDate.of(
                        rs.getLong("date_id"),
                        LocalDate.parse(rs.getString("play_day"))
                ),
                ReservationTime.of(
                        rs.getLong("time_id"),
                        LocalTime.parse(rs.getString("start_at"))
                ),
                Theme.of(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_content"),
                        rs.getString("theme_url")
                ),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("생성 키를 조회할 수 없습니다.");
        }
        return keyHolder.getKey().longValue();
    }
}
