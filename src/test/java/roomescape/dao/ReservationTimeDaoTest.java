package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;

@JdbcTest
@ActiveProfiles("test")
@Import(ReservationTimeDao.class)
public class ReservationTimeDaoTest {

    private static final String INSERT_SINGLE_TIME_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00');
            """;

    private static final String INSERT_THREE_TIMES_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00'),
                   (2, '11:00'),
                   (3, '13:00');
            """;

    private static final String INSERT_SINGLE_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg');
            """;

    private static final String INSERT_TWO_THEMES_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg'),
                   (2, '정콩이의 방탈출', '정콩이가 지키는 미스터리 방탈출',
                    'https://images.example.com/themes/jungkong-room.jpg');
            """;

    private static final String INSERT_SINGLE_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name)
            VALUES (1, 'brown@email.com', 'password', '브라운');
            """;

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_RESERVED_TIME_ON_TARGET_DATE_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 1, 1),
                   (2, 1, '2026-05-01', 2, 1, 1);
            """;

    private static final String INSERT_RESERVED_TIME_ON_OTHER_DATE_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-02', 1, 1, 1);
            """;

    private static final String INSERT_RESERVED_TIME_ON_OTHER_THEME_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 2, 1);
            """;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Test
    @Sql(statements = INSERT_THREE_TIMES_SQL)
    void 모든_예약시간을_조회한다() {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAllReservationTimes();

        assertThat(reservationTimes).hasSize(3);
        assertThat(reservationTimes)
                .extracting(
                        ReservationTime::getId,
                        ReservationTime::getStartAt
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, LocalTime.of(10, 0)),
                        tuple(2L, LocalTime.of(11, 0)),
                        tuple(3L, LocalTime.of(13, 0))
                );
    }

    @Test
    @Sql(statements = INSERT_SINGLE_TIME_SQL)
    void ID에_해당하는_예약시간을_조회한다() {
        ReservationTime reservationTime = reservationTimeDao.findReservationTimeById(1L);

        assertThat(reservationTime)
                .extracting(
                        ReservationTime::getId,
                        ReservationTime::getStartAt
                )
                .containsExactly(1L, LocalTime.of(10, 0));
    }

    @Test
    void 예약시간을_추가한다() {
        Long id = reservationTimeDao.insertWithKeyHolder(LocalTime.of(10, 0));

        ReservationTime reservationTime = reservationTimeDao.findReservationTimeById(id);

        assertThat(id).isNotNull();
        assertThat(id).isPositive();
        assertThat(reservationTime)
                .extracting(
                        ReservationTime::getId,
                        ReservationTime::getStartAt
                )
                .containsExactly(id, LocalTime.of(10, 0));
    }

    @Test
    @Sql(statements = INSERT_SINGLE_TIME_SQL)
    void 예약시간을_삭제한다() {
        int deletedCount = reservationTimeDao.delete(1L);

        assertThat(deletedCount).isEqualTo(1);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_SINGLE_MEMBER_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_RESERVED_TIME_ON_TARGET_DATE_SQL
    })
    void 특정_날짜와_테마에_이미_예약된_시간은_예약불가능하다() {
        Map<ReservationTime, Boolean> availableTimes = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1),
                1L
        );

        assertThat(availableTimes).hasSize(3);
        assertThat(availableTimes.entrySet())
                .extracting(
                        entry -> entry.getKey().getId(),
                        entry -> entry.getKey().getStartAt(),
                        Map.Entry::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, LocalTime.of(10, 0), false),
                        tuple(2L, LocalTime.of(11, 0), false),
                        tuple(3L, LocalTime.of(13, 0), true)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_SINGLE_MEMBER_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_RESERVED_TIME_ON_OTHER_DATE_SQL
    })
    void 다른_날짜의_예약은_예약가능여부에_영향을_주지_않는다() {
        Map<ReservationTime, Boolean> availableTimes = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1),
                1L
        );

        assertThat(availableTimes.entrySet())
                .extracting(
                        entry -> entry.getKey().getId(),
                        Map.Entry::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, true),
                        tuple(2L, true),
                        tuple(3L, true)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_TWO_THEMES_SQL,
            INSERT_SINGLE_MEMBER_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_RESERVED_TIME_ON_OTHER_THEME_SQL
    })
    void 다른_테마의_예약은_예약가능여부에_영향을_주지_않는다() {
        Map<ReservationTime, Boolean> availableTimes = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1),
                1L
        );

        assertThat(availableTimes.entrySet())
                .extracting(
                        entry -> entry.getKey().getId(),
                        Map.Entry::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, true),
                        tuple(2L, true),
                        tuple(3L, true)
                );
    }
}
