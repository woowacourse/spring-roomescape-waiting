package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.ReservationTime;

@JdbcTest
@ActiveProfiles("test")
@Import(ReservationDao.class)
public class ReservationDaoTest {

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

    private static final String INSERT_TWO_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name)
            VALUES (1, 'brown@email.com', 'password', '브라운'),
                   (2, 'jeongkong@email.com', 'password', '정콩이');
            """;

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_TWO_RESERVATIONS_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 1, 1),
                   (2, 2, '2026-05-02', 2, 1, 1);
            """;

    private static final String INSERT_TWO_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점');
            """;

    private static final String INSERT_RESERVATIONS_ACROSS_STORES_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-05-01', 1, 1, 1),
                   (2, 2, '2026-05-02', 2, 1, 1),
                   (3, 1, '2026-05-03', 3, 1, 2);
            """;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 모든_예약을_조회한다() {
        List<Reservation> reservations = reservationDao.findAllReservations();

        assertThat(reservations).hasSize(2);
        assertThat(reservations)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getId(),
                        reservation -> reservation.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, LocalDate.of(2026, 5, 1), 1L, LocalTime.of(10, 0), 1L),
                        tuple(2L, 2L, LocalDate.of(2026, 5, 2), 2L, LocalTime.of(11, 0), 1L)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void memberId로_예약을_조회한다() {
        List<Reservation> reservations = reservationDao.findAllReservationsByMemberId(1L);

        assertThat(reservations).hasSize(1);
        assertThat(reservations)
                .extracting(
                        Reservation::getMemberId,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getId(),
                        reservation -> reservation.getTime().getStartAt()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, LocalDate.of(2026, 5, 1), 1L, LocalTime.of(10, 0))
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void ID에_해당하는_예약을_조회한다() {
        Reservation reservation = reservationDao.findReservationById(1L);

        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        reservationTime -> reservationTime.getTime().getId(),
                        reservationTime -> reservationTime.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactly(
                        1L,
                        1L,
                        LocalDate.of(2026, 5, 1),
                        1L,
                        LocalTime.of(10, 0),
                        1L
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 예약을_수정한다() {
        // given: 기존 예약 조회 후 수정본 준비
        Reservation current = reservationDao.findReservationById(1L);
        Reservation modified = new Reservation(
                current.getId(),
                current.getMemberId(),
                LocalDate.of(2026, 5, 3),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                current.getThemeId(),
                current.getStoreId()
        );

        // when: 수정 실행
        reservationDao.update(modified);

        // then: DB 상태가 수정본대로 변경됨
        Reservation updatedReservation = reservationDao.findReservationById(1L);
        assertThat(updatedReservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getId(),
                        reservation -> reservation.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactly(
                        1L,
                        1L,
                        LocalDate.of(2026, 5, 3),
                        2L,
                        LocalTime.of(11, 0),
                        1L
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL
    })
    void 예약을_추가한다() {
        // given: 신규 예약 후보 (id null)
        Reservation candidate = new Reservation(
                null,
                1L,
                LocalDate.of(2026, 5, 1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );

        // when: insert 실행 (DB 가 id 채워서 반환)
        Reservation inserted = reservationDao.insert(candidate);

        // then: DB 에 저장된 row 검증
        Reservation reservation = reservationDao.findReservationById(inserted.getId());

        assertThat(inserted.getId()).isPositive();
        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        reservationTime -> reservationTime.getTime().getId(),
                        reservationTime -> reservationTime.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactly(
                        inserted.getId(),
                        1L,
                        LocalDate.of(2026, 5, 1),
                        1L,
                        LocalTime.of(10, 0),
                        1L
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 예약을_삭제한다() {
        int deletedCount = reservationDao.delete(1L);

        assertThat(deletedCount).isEqualTo(1);

        assertThat(reservationDao.findAllReservations()).hasSize(1);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_RESERVATIONS_SQL
    })
    void 같은_날짜_시간_테마의_예약을_추가하면_예외가_발생한다() {
        Reservation duplicate = new Reservation(
                null,
                2L,
                LocalDate.of(2026, 5, 1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );

        assertThatThrownBy(() -> reservationDao.insert(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL,
            INSERT_RESERVATIONS_ACROSS_STORES_SQL
    })
    void findReservationsByStoreId는_해당_매장의_예약만_조회한다() {
        List<Reservation> gangnamReservations = reservationDao.findReservationsByStoreId(1L);

        assertThat(gangnamReservations).hasSize(2);
        assertThat(gangnamReservations)
                .extracting(Reservation::getId, Reservation::getStoreId)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L),
                        tuple(2L, 1L)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL,
            INSERT_RESERVATIONS_ACROSS_STORES_SQL
    })
    void 다른_매장의_예약은_findReservationsByStoreId_결과에_포함되지_않는다() {
        List<Reservation> hongdaeReservations = reservationDao.findReservationsByStoreId(2L);

        assertThat(hongdaeReservations).hasSize(1);
        assertThat(hongdaeReservations)
                .extracting(Reservation::getId, Reservation::getStoreId)
                .containsExactly(tuple(3L, 2L));
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL
    })
    void 예약이_없는_매장은_findReservationsByStoreId가_빈_리스트를_반환한다() {
        List<Reservation> reservations = reservationDao.findReservationsByStoreId(1L);

        assertThat(reservations).isEmpty();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL,
            INSERT_RESERVATIONS_ACROSS_STORES_SQL
    })
    void 예약한_사용자id를_수정한다() {
        // given: 기존 예약 조회 + 양도 후보 생성
        Reservation current = reservationDao.findReservationById(1L);
        Reservation promoted = current.promoteTo(2L);

        // when: 양도 적용
        reservationDao.update(promoted);

        // then: member_id 가 2 로 변경됨
        Reservation updatedReservation = reservationDao.findReservationById(1L);
        assertThat(updatedReservation.getMemberId()).isEqualTo(2L);
    }
}
