package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.fixture.ReservationFixture.reservation;
import static roomescape.reservation.fixture.ReservationFixture.waitReservation;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.date.domain.ReservationDate;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest
@TestPropertySource(properties = {
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.orm.jdbc.bind=TRACE"
})
class ReservationRepositoryTest {

    private final String name = "한다";
    private final LocalDate date1 = LocalDate.of(2099, 1, 1);
    private final LocalDate date2 = LocalDate.of(2099, 9, 1);
    private ReservationDate reservationDate1;
    private ReservationDate reservationDate2;
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private Theme theme;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationDateRepository reservationDateRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setup() {
        ReservationTime time1 = reservationTimeRepository.save(ReservationTimeFixture.time15());
        ReservationTime time2 = reservationTimeRepository.save(ReservationTimeFixture.time16());
        reservationTime1 = reservationTimeRepository.findById(time1.getId()).get();
        reservationTime2 = reservationTimeRepository.findById(time2.getId()).get();

        reservationDate1 = reservationDateRepository.save(ReservationDate.create(date1));
        reservationDate2 = reservationDateRepository.save(ReservationDate.create(date2));
        theme = themeRepository.save(Theme.create("테마", "설명", "썸네일"));
    }

    @Nested
    @DisplayName("findByMemberIdWithWaitingTurn 메서드는")
    class FindByMemberIdWithWaitingTurn {

        @Test
        @DisplayName("대기 순번을 포함한 나의 예약 목록을 조회한다")
        void 성공1() {
            // given
            Member member = saveMember("member1");
            save(Reservation.reserved(member, reservationDate1, reservationTime1, theme))
                .getId();
            int expectedSize = 1;

            // when
            List<ReservationWithWaitingTurn> actual =
                reservationRepository.findAllByMemberIdWithWaitingTurn(member.getId());
            entityManager.flush();
            entityManager.clear();

            // then
            assertThat(actual)
                .hasSize(expectedSize);
        }
    }

    @Nested
    @DisplayName("findFirstWaitingByDateTimeAndThemeId 메서드는")
    class FindFirstWaitingByDateTimeAndThemeId {

        @Test
        @DisplayName("가장 이른 대기 요청 1개를 조회한다")
        void 성공1() {
            // given
            List<String> usernames = List.of("user1", "user2", "user3");
            List<Reservation> reservations = List.of(
                reservation(usernames.get(0), reservationDate1, reservationTime1, theme),
                waitReservation(usernames.get(1), reservationDate1, reservationTime1, theme, 1L),
                waitReservation(usernames.get(2), reservationDate1, reservationTime1, theme, 2L)
            );
            Reservation expected = reservations.get(1);
            saveAll(reservations);

            // when
            Optional<Reservation> actual = reservationRepository.findFirstWaitingByDateAndTimeAndTheme(
                reservationDate1, reservationTime1, theme);

            // then
            assertThat(actual).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);

        }

        @Test
        @DisplayName("슬롯에 대기 요청이 없으면 Optional.empty를 반환한다")
        void 성공2() {
            // given
            List<String> usernames = List.of("user1", "user2", "user3");
            List<Reservation> reservations = List.of(
                reservation(usernames.get(0), reservationDate1, reservationTime1, theme)
            );
            saveAll(reservations);

            // when
            Optional<Reservation> actual = reservationRepository.findFirstWaitingByDateAndTimeAndTheme(
                reservationDate1, reservationTime1, theme);

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("findNextWaitingOrderBySlot 메서드는")
    class FindNextWaitingOrderBySlotTest {

        @Test
        @DisplayName("슬롯에 대기 예약이 없으면 1을 반환한다")
        void 성공1() {
            // when
            Long actual = reservationRepository.findNextWaitingOrderByDateAndTimeAndTheme(
                reservationDate1, reservationTime1, theme);

            // then
            assertThat(actual).isEqualTo(1L);
        }

        @Test
        @DisplayName("슬롯에 존재하는 가장 큰 대기 순서보다 1 큰 값을 반환한다")
        void 성공2() {
            // given
            save(waitReservation("대기자1", reservationDate1, reservationTime1, theme, 1L));
            save(waitReservation("대기자3", reservationDate1, reservationTime1, theme, 3L));
            save(reservation("확정 예약자", reservationDate1, reservationTime1, theme));
            save(waitReservation("다른 슬롯 대기자", reservationDate2, reservationTime1, theme, 10L));

            // when
            Long actual = reservationRepository.findNextWaitingOrderByDateAndTimeAndTheme(
                reservationDate1, reservationTime1, theme);

            // then
            assertThat(actual).isEqualTo(4L);
        }
    }

    @Nested
    @DisplayName("findMyReservationsWithWaitingTurn 메서드는")
    class FindMyReservationsWithWaitingTurnTest {


        @Test
        @DisplayName("나의 예약 목록에 대기 순번을 포함해 조회한다")
        void 성공() {
            // given
            save(reservation("예약자", reservationDate1, reservationTime1, theme));
            saveWaitReservation("앞선 대기자", reservationDate1, reservationTime1, theme, 1L);
            Reservation waiting = saveWaitReservation(name, reservationDate1, reservationTime1,
                theme, 2L);
            Reservation reserved = saveReservation(name, reservationDate2, reservationTime2,
                theme);

            // when
            List<ReservationWithWaitingTurn> actual =
                reservationRepository.findAllByMemberIdWithWaitingTurn(saveMember(name).getId());

            // then
            assertAll(
                () -> assertThat(actual)
                    .hasSize(2),
                () -> assertThat(actual)
                    .filteredOn(reservation -> reservation.id().equals(waiting.getId()))
                    .singleElement()
                    .extracting("status", "waitingTurn")
                    .containsExactly(ReservationStatus.WAITING, 2L),
                () -> assertThat(actual)
                    .filteredOn(reservation -> reservation.id().equals(reserved.getId()))
                    .singleElement()
                    .extracting("status", "waitingTurn")
                    .containsExactly(ReservationStatus.RESERVED, null)
            );
        }

        @Test
        @DisplayName("지난 대기 예약은 대기 순번을 가지지 않는다")
        void 성공2() {
            // given
            ReservationDate pastDate = savePastDate();
            Reservation waiting = saveWaitReservation(name, reservationDate1, reservationTime1,
                theme, 1L);
            ReflectionTestUtils.setField(waiting, "date", pastDate);
            reservationRepository.saveAndFlush(waiting);

            // when
            List<ReservationWithWaitingTurn> actual =
                reservationRepository.findAllByMemberIdWithWaitingTurn(saveMember(name).getId());

            // then
            assertThat(actual)
                .filteredOn(reservation -> reservation.id().equals(waiting.getId()))
                .singleElement()
                .extracting("status", "waitingTurn")
                .containsExactly(ReservationStatus.WAITING, null);
        }
    }

    private List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> savedReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            savedReservations.add(save(reservation));
        }
        return savedReservations;
    }

    private Reservation save(Reservation reservation) {
        Member member = saveMember(reservation.getMember().getName());
        ReflectionTestUtils.setField(reservation, "member", member);
        return reservationRepository.save(reservation);
    }

    private Member saveMember(String name) {
        return memberRepository.findByName(name)
            .orElseGet(() -> memberRepository.save(Member.register(name, "password")));
    }

    private Reservation saveReservation(String name, ReservationDate date, ReservationTime time,
        Theme theme) {
        return save(reservation(saveMember(name), date, time, theme));
    }

    private Reservation saveWaitReservation(String name, ReservationDate date,
        ReservationTime time, Theme theme, Long waitingOrder) {
        return save(waitReservation(saveMember(name), date, time, theme, waitingOrder));
    }

    private ReservationDate savePastDate() {
        ReservationDate pastDate = reservationDateRepository.saveAndFlush(
            ReservationDate.create(LocalDate.now().plusYears(100)));
        ReflectionTestUtils.setField(pastDate, "date", LocalDate.now().minusDays(1));
        return reservationDateRepository.saveAndFlush(pastDate);
    }
}
