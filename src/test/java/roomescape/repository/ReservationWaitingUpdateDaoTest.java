package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
public class ReservationWaitingUpdateDaoTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "테스트", "설명", "url");

    private final static String SELECT_RESERVATION_WAITING_SQL = """
                select
                    w.id,
                    w.name,
                    w.date,
                    w.created_at,
                    t.id       as time_id,
                    t.start_at as time_start_at,
                    th.id          as theme_id,
                    th.name        as theme_name,
                    th.description as theme_description,
                    th.url         as theme_url,
                    ranked.sequence
                from waiting w
                join reservation_time t  ON w.time_id  = t.id
                join theme th            ON w.theme_id = th.id
                join (
                    select
                        id,
                        ROW_NUMBER() OVER (
                            PARTITION BY date, time_id, theme_id
                            ORDER BY created_at, id
                        ) as sequence
                    from waiting
                ) as ranked ON w.id = ranked.id
                """;

    private final static RowMapper<ReservationWaiting> reservationWaitingRowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getObject("start_at", LocalTime.class)
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_url")
        );

        ReservationSlot slot = new ReservationSlot(
                resultSet.getObject("date", LocalDate.class),
                reservationTime,
                theme
        );

        return new ReservationWaiting(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                slot,
                resultSet.getObject("created_at", LocalDateTime.class)
        );
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationWaitingUpdateDao reservationWaitingUpdateDao;

    @BeforeEach
    void setUp() {
        this.reservationWaitingUpdateDao = new ReservationWaitingUpdateDao(jdbcTemplate);

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
    }

    @Test
    void 예약_대기를_제대로_생성한다() {
        ReservationWaiting reservationWaiting = new ReservationWaiting("테스트", new ReservationSlot(LocalDate.parse("2027-05-27"), reservationTime, theme));

        reservationWaitingUpdateDao.create(reservationWaiting);

        Optional<ReservationWaiting> reservationWaitingOptional = jdbcTemplate.query(SELECT_RESERVATION_WAITING_SQL + "where w.name = ?", reservationWaitingRowMapper, "테스트")
                                                                                                                .stream()
                                                                                                                .findFirst();
        assertThat(reservationWaitingOptional.isPresent()).isTrue();
        assertThat(reservationWaitingOptional.get().getDate()).isEqualTo(LocalDate.parse("2027-05-27"));
    }

    @Test
    void 예약_대기를_제대로_삭제한다() {
        jdbcTemplate.update("insert into waiting (name, date, time_id, theme_id, created_at) values ('테스트', '2027-05-27', 1, 1, '2026-05-15 10:30:00')");

        String sql = """
            SELECT EXISTS (
                SELECT 1
                    FROM waiting
                    WHERE id = ?
            )
            """;

        assertThat(Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, 1L))).isTrue();

        reservationWaitingUpdateDao.delete(1L);

         assertThat(Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, 1L))).isFalse();
    }
}
