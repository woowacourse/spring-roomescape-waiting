package roomescape.reservationtime;

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
        // given: 10:00 시작 시간

        // when: insert (DB 가 id 채워서 반환)
        long id = reservationTimeDao.insertWithKeyHolder(LocalTime.of(10, 0));

        // then: DB 에 저장된 row 검증
        ReservationTime reservationTime = reservationTimeDao.findReservationTimeById(id);

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
        Map<ReservationTime, Long> availableTimes = reservationTimeDao.findAvailableTimes(
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
                        tuple(1L, LocalTime.of(10, 0), 1L),
                        tuple(2L, LocalTime.of(11, 0), 2L),
                        tuple(3L, LocalTime.of(13, 0), null)
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
        Map<ReservationTime, Long> availableTimes = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1),
                1L
        );

        assertThat(availableTimes.entrySet())
                .extracting(
                        entry -> entry.getKey().getId(),
                        Map.Entry::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, null),
                        tuple(2L, null),
                        tuple(3L, null)
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
        Map<ReservationTime, Long> availableTimes = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1),
                1L
        );

        assertThat(availableTimes.entrySet())
                .extracting(
                        entry -> entry.getKey().getId(),
                        Map.Entry::getValue
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, null),
                        tuple(2L, null),
                        tuple(3L, null)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_SINGLE_MEMBER_SQL,
            "INSERT INTO store (id, name) VALUES (1, '강남점'), (2, '홍대점')",
            "INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id, status) "
                    + "VALUES (1, 1, '2026-05-01', 1, 1, 1, 'PENDING')"
    })
    void 매장별로_가용시간을_구분하고_PENDING_예약은_대기대상이_아니다() {
        List<ReservationTimeAvailability> gangnam = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1), 1L, 1L);
        List<ReservationTimeAvailability> hongdae = reservationTimeDao.findAvailableTimes(
                LocalDate.of(2026, 5, 1), 1L, 2L);

        ReservationTimeAvailability gangnamTen = gangnam.get(0);
        ReservationTimeAvailability hongdaeTen = hongdae.get(0);
        assertThat(gangnamTen.isAvailable()).isFalse();
        assertThat(gangnamTen.isWaitable()).isFalse();
        assertThat(hongdaeTen.isAvailable()).isTrue();
    }
}
