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

    private static final String INSERT_TWO_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점');
            """;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL
    })
    void 모든_예약을_조회한다() {
        // given
        Reservation brownReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        Reservation jeongkongReservation = reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));

        // when
        List<Reservation> reservations = reservationDao.findAllReservations();

        // then
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
                        tuple(brownReservation.getId(), 1L, LocalDate.of(2026, 5, 1), 1L, LocalTime.of(10, 0), 1L),
                        tuple(jeongkongReservation.getId(), 2L, LocalDate.of(2026, 5, 2), 2L, LocalTime.of(11, 0), 1L)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL
    })
    void memberId로_예약을_조회한다() {
        // given
        reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));

        // when
        List<Reservation> reservations = reservationDao.findAllReservationsByMemberId(1L);

        // then
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
            INSERT_DEFAULT_STORE_SQL
    })
    void ID에_해당하는_예약을_조회한다() {
        // given
        Reservation brownReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));

        // when
        Reservation reservation = reservationDao.findReservationByIdForUpdate(brownReservation.getId());

        // then
        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        r -> r.getTime().getId(),
                        r -> r.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactly(
                        brownReservation.getId(),
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
            INSERT_DEFAULT_STORE_SQL
    })
    void 예약을_수정한다() {
        // given
        Reservation brownReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        Reservation modified = new Reservation(
                brownReservation.getId(),
                brownReservation.getMemberId(),
                LocalDate.of(2026, 5, 3),
                new ReservationTime(2L, LocalTime.of(11, 0)),
                brownReservation.getThemeId(),
                brownReservation.getStoreId()
        );

        // when
        reservationDao.update(modified);

        // then
        Reservation updatedReservation = reservationDao.findReservationByIdForUpdate(brownReservation.getId());
        assertThat(updatedReservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        r -> r.getTime().getId(),
                        r -> r.getTime().getStartAt(),
                        Reservation::getThemeId
                )
                .containsExactly(
                        brownReservation.getId(),
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
        // given
        Reservation candidate = new Reservation(
                null,
                1L,
                LocalDate.of(2026, 5, 1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                1L,
                1L
        );

        // when
        Reservation inserted = reservationDao.insert(candidate);

        // then
        Reservation reservation = reservationDao.findReservationByIdForUpdate(inserted.getId());

        assertThat(inserted.getId()).isPositive();
        assertThat(reservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getMemberId,
                        Reservation::getDate,
                        r -> r.getTime().getId(),
                        r -> r.getTime().getStartAt(),
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
            INSERT_DEFAULT_STORE_SQL
    })
    void 예약을_삭제한다() {
        // given
        Reservation brownReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));

        // when
        int deletedCount = reservationDao.delete(brownReservation.getId());

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(reservationDao.findAllReservations()).hasSize(1);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_DEFAULT_STORE_SQL
    })
    void 같은_날짜_시간_테마의_예약을_추가하면_예외가_발생한다() {
        // given
        reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        Reservation duplicate = new Reservation(
                null, 2L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationDao.insert(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL
    })
    void findReservationsByStoreId는_해당_매장의_예약만_조회한다() {
        // given
        Reservation gangnam1 = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        Reservation gangnam2 = reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));
        reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 3), new ReservationTime(3L, LocalTime.of(13, 0)), 1L, 2L));

        // when
        List<Reservation> gangnamReservations = reservationDao.findReservationsByStoreId(1L);

        // then
        assertThat(gangnamReservations).hasSize(2);
        assertThat(gangnamReservations)
                .extracting(Reservation::getId, Reservation::getStoreId)
                .containsExactlyInAnyOrder(
                        tuple(gangnam1.getId(), 1L),
                        tuple(gangnam2.getId(), 1L)
                );
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL
    })
    void 다른_매장의_예약은_findReservationsByStoreId_결과에_포함되지_않는다() {
        // given
        reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        reservationDao.insert(new Reservation(
                null, 2L, LocalDate.of(2026, 5, 2), new ReservationTime(2L, LocalTime.of(11, 0)), 1L, 1L));
        Reservation hongdaeReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 3), new ReservationTime(3L, LocalTime.of(13, 0)), 1L, 2L));

        // when
        List<Reservation> hongdaeReservations = reservationDao.findReservationsByStoreId(2L);

        // then
        assertThat(hongdaeReservations).hasSize(1);
        assertThat(hongdaeReservations)
                .extracting(Reservation::getId, Reservation::getStoreId)
                .containsExactly(tuple(hongdaeReservation.getId(), 2L));
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL
    })
    void 예약이_없는_매장은_findReservationsByStoreId가_빈_리스트를_반환한다() {
        // when
        List<Reservation> reservations = reservationDao.findReservationsByStoreId(1L);

        // then
        assertThat(reservations).isEmpty();
    }

    @Test
    @Sql(statements = {
            INSERT_THREE_TIMES_SQL,
            INSERT_SINGLE_THEME_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_TWO_STORES_SQL
    })
    void 예약한_사용자id를_수정한다() {
        // given
        Reservation brownReservation = reservationDao.insert(new Reservation(
                null, 1L, LocalDate.of(2026, 5, 1), new ReservationTime(1L, LocalTime.of(10, 0)), 1L, 1L));
        Reservation promoted = brownReservation.promoteTo(2L);

        // when
        reservationDao.update(promoted);

        // then
        Reservation updatedReservation = reservationDao.findReservationByIdForUpdate(brownReservation.getId());
        assertThat(updatedReservation.getMemberId()).isEqualTo(2L);
    }
}
