package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.Role;
import roomescape.auth.exception.WrongStoreAccessException;
import roomescape.member.Member;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationhistory.ReservationHistoryDao;
import roomescape.reservationhistory.ReservationHistoryService;
import roomescape.reservationtime.ReservationTimeDao;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;
import roomescape.reservationwait.ReservationWaitDao;
import roomescape.payment.infrastructure.JdbcPaymentOrderRepository;

@JdbcTest
@ActiveProfiles("test")
@Import({ReservationService.class, ReservationDao.class, ReservationTimeDao.class,
        ReservationWaitDao.class, ReservationHistoryDao.class, ReservationHistoryService.class, JdbcPaymentOrderRepository.class})
public class ReservationServiceIntegrationTest {

    private static final long BROWN_ID = 1L;
    private static final long JEONGKONG_ID = 2L;
    private static final long RESERVATION_ID = 1L;
    private static final long TIME_ID = 1L;
    private static final long OTHER_TIME_ID = 2L;

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_TWO_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name, role)
            VALUES (1, 'brown@email.com', 'password', '브라운', 'USER'),
                   (2, 'jeongkong@email.com', 'password', '정콩이', 'USER');
            """;

    private static final String INSERT_DEFAULT_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '테마', '설명', 'https://example.com/img.jpg');
            """;

    private static final String INSERT_TWO_TIMES_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00'),
                   (2, '11:00');
            """;

    private static final String INSERT_BROWN_RESERVATION_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1);
            """;

    private static final String INSERT_JEONGKONG_WAIT_SQL = """
            INSERT INTO reservation_wait (id, reservation_id, member_id)
            VALUES (1, 1, 2);
            """;

    private static final String INSERT_TWO_RESERVATIONS_SAME_THEME_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1),
                   (2, 2, '2026-12-01', 2, 1, 1);
            """;

    private static final String INSERT_GANGNAM_MANAGER_SQL = """
            INSERT INTO member (id, email, password, name, role, store_id)
            VALUES (10, 'gangnam@email.com', 'password', '강남매니저', 'MANAGER', 1);
            """;

    private static final String INSERT_BROWN_PAST_RESERVATION_SQL = """
          INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
          VALUES (1, 1, '2020-01-01', 1, 1, 1);
          """;

    private final ReservationService reservationService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationServiceIntegrationTest(ReservationService reservationService,
                                             JdbcTemplate jdbcTemplate) {
        this.reservationService = reservationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nested
    class 예약_생성 {

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_TWO_MEMBERS_SQL,
                INSERT_DEFAULT_THEME_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_BROWN_RESERVATION_SQL
        })
        void 예약이_존재할_경우_새_예약을_생성할_수_없다() {
            // given: BROWN 이 이미 슬롯 점유
            LocalDate sameDate = LocalDate.of(2026, 12, 1);

            // when & then: 정콩이가 같은 슬롯 예약 시도 → UNIQUE 위반
            assertThatThrownBy(() -> reservationService.createReservation(
                    JEONGKONG_ID, sameDate, TIME_ID, 1L, 1L))
                    .isInstanceOf(ReservationAlreadyExistsException.class);

            // then: 원 예약 1건 그대로 유지
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM reservation", Integer.class);
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    class 예약_변경 {

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_TWO_MEMBERS_SQL,
                INSERT_DEFAULT_THEME_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_TWO_RESERVATIONS_SAME_THEME_SQL
        })
        void 이미_예약된_시간으로는_예약을_변경할_수_없다() {
            // given: BROWN 10:00, 정콩이 11:00 (같은 매장/날짜/테마)
            LocalDate sameDate = LocalDate.of(2026, 12, 1);

            // when & then: BROWN 이 정콩이의 슬롯으로 변경 시도 → UNIQUE 위반
            assertThatThrownBy(() -> reservationService.updateReservation(
                    RESERVATION_ID, sameDate, BROWN_ID, OTHER_TIME_ID))
                    .isInstanceOf(ReservationAlreadyExistsException.class);

            // then: BROWN 예약은 원 시간 그대로
            Long currentTimeId = jdbcTemplate.queryForObject(
                    "SELECT time_id FROM reservation WHERE id = ?",
                    Long.class, RESERVATION_ID);
            assertThat(currentTimeId).isEqualTo(TIME_ID);
        }

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_TWO_MEMBERS_SQL,
                INSERT_DEFAULT_THEME_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_BROWN_RESERVATION_SQL
        })
        void 존재하지_않는_예약시간으로는_예약을_변경할_수_없다() {
            // given: BROWN 예약 존재, time(999) 은 없음

            // when & then: 없는 time 으로 변경 시도
            assertThatThrownBy(() -> reservationService.updateReservation(
                    RESERVATION_ID, LocalDate.of(2026, 12, 5), BROWN_ID, 999L))
                    .isInstanceOf(ReservationTimeNotFoundException.class);
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_TIMES_SQL
        })
        void 존재하지_않는_예약은_변경할_수_없다() {
            // given: time 만 있고 reservation 없음

            // when & then: 없는 reservation(999) 변경 시도
            assertThatThrownBy(() -> reservationService.updateReservation(
                    999L, LocalDate.of(2026, 12, 1), BROWN_ID, TIME_ID))
                    .isInstanceOf(ReservationNotFoundException.class);
        }

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_TWO_MEMBERS_SQL,
                INSERT_GANGNAM_MANAGER_SQL,
                INSERT_DEFAULT_THEME_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_BROWN_RESERVATION_SQL
        })
        void 매니저가_자기_매장_예약을_변경할_수_있다() {
            // given: 강남 매니저 + BROWN 예약
            Member gangnamManager = new Member(
                    10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);
            LocalDate newDate = LocalDate.of(2026, 12, 5);

            // when: 매니저가 다른 시간으로 변경
            reservationService.updateReservationByManager(
                    RESERVATION_ID, newDate, OTHER_TIME_ID, gangnamManager);

            // then: DB 의 time_id 가 실제로 바뀜
            Long currentTimeId = jdbcTemplate.queryForObject(
                    "SELECT time_id FROM reservation WHERE id = ?",
                    Long.class, RESERVATION_ID);
            assertThat(currentTimeId).isEqualTo(OTHER_TIME_ID);
        }

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_TWO_MEMBERS_SQL,
                INSERT_DEFAULT_THEME_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_BROWN_RESERVATION_SQL
        })
        void 다른_매장_매니저는_예약을_변경할_수_없다() {
            // given: 다른 매장(2번) 소속 매니저 + 강남점(1번) BROWN 예약
            Member otherStoreManager = new Member(
                    99L, "other@email.com", "password", "다른매니저", Role.MANAGER, 2L);
            LocalDate newDate = LocalDate.of(2026, 12, 5);

            // when & then: 권한 없는 매장의 예약 변경 시도
            assertThatThrownBy(() -> reservationService.updateReservationByManager(
                    RESERVATION_ID, newDate, OTHER_TIME_ID, otherStoreManager))
                    .isInstanceOf(WrongStoreAccessException.class);
        }
    }

    @Nested
    class 예약_삭제 {

        @Nested
        class 사용자_취소 {

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL
            })
            void 대기자_없는_예약을_취소하면_삭제된다() {
                // given: BROWN 예약, 대기자 없음

                // when: BROWN 이 본인 예약 취소
                reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);

                // then: reservation row 가 DB 에서 사라짐
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation WHERE id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(count).isZero();
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL,
                    INSERT_JEONGKONG_WAIT_SQL
            })
            void 대기자_있는_예약을_취소하면_대기_1번에게_양도된다() {
                // given: BROWN 예약 + 정콩이 대기

                // when: BROWN 이 본인 예약 취소
                reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);

                // then: reservation 주인이 정콩이로 양도됨
                Long currentOwner = jdbcTemplate.queryForObject(
                        "SELECT member_id FROM reservation WHERE id = ?",
                        Long.class, RESERVATION_ID);
                assertThat(currentOwner).isEqualTo(JEONGKONG_ID);

                // then: 정콩이의 대기 row 사라짐
                Integer waitCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(waitCount).isZero();
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL,
                    """
                    INSERT INTO member (id, email, password, name, role)
                    VALUES (3, 'third@email.com', 'password', '세번째', 'USER');
                    """,
                    """
                    INSERT INTO reservation_wait (id, reservation_id, member_id, created_at)
                    VALUES (1, 1, 2, '2026-06-05 10:00:00'),
                           (2, 1, 3, '2026-06-05 10:00:00');
                    """
            })
            void 대기_created_at이_같으면_id가_작은_대기자에게_예약을_양도한다() {
                reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);

                Long currentOwner = jdbcTemplate.queryForObject(
                        "SELECT member_id FROM reservation WHERE id = ?",
                        Long.class,
                        RESERVATION_ID
                );

                assertThat(currentOwner).isEqualTo(2L);
            }

            @Test
            void 존재하지_않는_예약은_취소할_수_없다() {
                // given: 빈 DB

                // when & then: 없는 reservation(999) 취소 시도
                assertThatThrownBy(() -> reservationService.deleteReservation(999L, BROWN_ID))
                        .isInstanceOf(ReservationNotFoundException.class);
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL,
                    INSERT_JEONGKONG_WAIT_SQL
            })
            void 양도_사이클로_원소유자에게_재양도되어도_예외없이_완료된다() {
                // given: BROWN 예약 + 정콩이 대기

                // when 1: BROWN 취소 → 정콩이로 양도
                reservationService.deleteReservation(RESERVATION_ID, BROWN_ID);

                Long ownerAfterFirst = jdbcTemplate.queryForObject(
                        "SELECT member_id FROM reservation WHERE id = ?",
                        Long.class, RESERVATION_ID);
                assertThat(ownerAfterFirst).isEqualTo(JEONGKONG_ID);

                // when 2: BROWN 이 같은 예약에 재대기 등록
                jdbcTemplate.update(
                        "INSERT INTO reservation_wait (reservation_id, member_id) VALUES (?, ?)",
                        RESERVATION_ID, BROWN_ID);

                // when 3: 정콩이 취소 → BROWN 에게 재양도
                reservationService.deleteReservation(RESERVATION_ID, JEONGKONG_ID);

                // then: 예약 주인이 다시 BROWN — UK 제약이 양도 사이클을 막지 않음
                Long ownerAfterSecond = jdbcTemplate.queryForObject(
                        "SELECT member_id FROM reservation WHERE id = ?",
                        Long.class, RESERVATION_ID);
                assertThat(ownerAfterSecond).isEqualTo(BROWN_ID);
            }
        }

        @Nested
        class 매니저_취소 {

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_GANGNAM_MANAGER_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL
            })
            void 미래_예약을_대기자_없이_취소하면_삭제된다() {
                // given: 강남 매니저 + BROWN 미래 예약 (대기자 없음)
                Member gangnamManager = new Member(
                        10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

                // when: 매니저가 예약 취소
                reservationService.deleteReservationByManager(RESERVATION_ID, gangnamManager);

                // then: reservation row 가 DB 에서 사라짐
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation WHERE id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(count).isZero();
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_GANGNAM_MANAGER_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL,
                    INSERT_JEONGKONG_WAIT_SQL
            })
            void 미래_예약을_취소하면_대기_1번에게_양도된다() {
                // given: 강남 매니저 + BROWN 미래 예약 + 정콩이 대기
                Member gangnamManager = new Member(
                        10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

                // when: 매니저가 미래 예약 취소
                reservationService.deleteReservationByManager(RESERVATION_ID, gangnamManager);

                // then: reservation 주인이 정콩이로 양도됨
                Long currentOwner = jdbcTemplate.queryForObject(
                        "SELECT member_id FROM reservation WHERE id = ?",
                        Long.class, RESERVATION_ID);
                assertThat(currentOwner).isEqualTo(JEONGKONG_ID);

                // then: 정콩이의 대기 row 사라짐
                Integer waitCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(waitCount).isZero();
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_GANGNAM_MANAGER_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_PAST_RESERVATION_SQL,
                    INSERT_JEONGKONG_WAIT_SQL
            })
            void 과거_예약을_취소하면_대기자가_있어도_양도되지_않는다() {
                // given: 강남 매니저 + BROWN 과거 예약 + 정콩이 대기
                Member gangnamManager = new Member(
                        10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

                // when: 매니저가 과거 예약 취소
                reservationService.deleteReservationByManager(RESERVATION_ID, gangnamManager);

                // then: reservation row 가 사라짐
                Integer reservationCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation WHERE id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(reservationCount).isZero();

                // then: 정콩이에게 양도되지 않음 (이용 이력에 허위 예약이 생기지 않음)
                Integer jeongkongOwnedCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation WHERE member_id = ?",
                        Integer.class, JEONGKONG_ID);
                assertThat(jeongkongOwnedCount).isZero();

                // then: 정콩이의 대기 row 도 함께 정리됨
                Integer waitCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation_wait WHERE reservation_id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(waitCount).isZero();
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_GANGNAM_MANAGER_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_PAST_RESERVATION_SQL
            })
            void 과거_예약을_대기자_없이_취소하면_삭제된다() {
                // given: 강남 매니저 + BROWN 과거 예약 (대기자 없음)
                Member gangnamManager = new Member(
                        10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

                // when: 매니저가 과거 예약 취소
                reservationService.deleteReservationByManager(RESERVATION_ID, gangnamManager);

                // then: reservation row 가 사라짐
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM reservation WHERE id = ?",
                        Integer.class, RESERVATION_ID);
                assertThat(count).isZero();
            }

            @Test
            void 존재하지_않는_예약은_취소할_수_없다() {
                // given: 빈 DB + 매니저
                Member gangnamManager = new Member(
                        10L, "gangnam@email.com", "password", "강남매니저", Role.MANAGER, 1L);

                // when & then: 없는 reservation 취소 시도
                assertThatThrownBy(() ->
                        reservationService.deleteReservationByManager(999L, gangnamManager))
                        .isInstanceOf(ReservationNotFoundException.class);
            }

            @Test
            @Sql(statements = {
                    INSERT_DEFAULT_STORE_SQL,
                    INSERT_TWO_MEMBERS_SQL,
                    INSERT_DEFAULT_THEME_SQL,
                    INSERT_TWO_TIMES_SQL,
                    INSERT_BROWN_RESERVATION_SQL
            })
            void 다른_매장_매니저는_예약을_취소할_수_없다() {
                // given: 다른 매장(2번) 소속 매니저 + 강남점(1번) BROWN 예약
                Member otherStoreManager = new Member(
                        99L, "other@email.com", "password", "다른매니저", Role.MANAGER, 2L);

                // when & then: 권한 없는 매장의 예약 취소 시도
                assertThatThrownBy(() ->
                        reservationService.deleteReservationByManager(RESERVATION_ID, otherStoreManager))
                        .isInstanceOf(WrongStoreAccessException.class);
            }
        }
    }
}
