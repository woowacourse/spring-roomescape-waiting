package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.common.util.DateTime;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.FakeMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.infrastructure.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.FakeThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.infrastructure.FakeWaitingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReservationServiceTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 10, 5);
    private static final LocalTime TEST_TIME = LocalTime.of(10, 0);
    private static final Long TEST_THEME_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;

    private DateTime dateTime;
    private List<Reservation> reservations;
    private List<Theme> themes;
    private List<ReservationTime> reservationTimes;
    private ThemeRepository themeRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private MemberRepository memberRepository;
    private WaitingRepository waitingRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        dateTime = new DateTime() {
            @Override
            public LocalDateTime now() {
                return LocalDateTime.of(2025, 10, 5, 10, 0);
            }

            @Override
            public LocalDate nowDate() {
                return LocalDate.of(2025, 10, 5);
            }
        };

        reservations = new ArrayList<>();
        themes = new ArrayList<>();
        reservationTimes = new ArrayList<>();

        themeRepository = new FakeThemeRepository(themes, reservations);
        reservationTimeRepository = new FakeReservationTimeRepository(reservationTimes);
        reservationRepository = new FakeReservationRepository(reservations);
        memberRepository = new FakeMemberRepository(new ArrayList<>());
        waitingRepository = new FakeWaitingRepository(new ArrayList<>());

        reservationService = new ReservationService(
                dateTime,
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository,
                waitingRepository
        );
    }

    @Nested
    @DisplayName("예약 생성 테스트")
    class ReservationCreationTest {

        @BeforeEach
        void setUp() {
            Theme theme = createTestTheme("테스트1", "설명", "localhost:8080");
            ReservationTime reservationTime = createTestReservationTime(TEST_TIME);
            Member member = createTestMember("홍길동", "a@com", "a");
        }

        @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
        @ParameterizedTest
        @MethodSource("provideInvalidReservationDates")
        void shouldNotCreateReservationForPastDate(LocalDate date, Long timeId) {
            // given
            ReservationRequest request = new ReservationRequest(date, timeId, TEST_THEME_ID);

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(request, TEST_MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("중복 예약이 불가하다.")
        @Test
        void shouldNotCreateDuplicateReservation() {
            // given
            createTestReservation(TEST_DATE, TEST_TIME, TEST_THEME_ID, TEST_MEMBER_ID);
            ReservationRequest request = new ReservationRequest(TEST_DATE, 1L, TEST_THEME_ID);

            // when & then
            assertThatThrownBy(() -> reservationService.createReservation(request, TEST_MEMBER_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private static Stream<Arguments> provideInvalidReservationDates() {
            return Stream.of(
                    Arguments.of(LocalDate.of(2024, 10, 5), 1L),
                    Arguments.of(LocalDate.of(2025, 9, 5), 1L),
                    Arguments.of(LocalDate.of(2025, 10, 4), 1L),
                    Arguments.of(LocalDate.of(2025, 10, 5), 2L)
            );
        }
    }

    @Nested
    @DisplayName("예약 삭제 테스트")
    class ReservationDeletionTest {

        @BeforeEach
        void setUp() {
            Theme theme = createTestTheme("테스트1", "설명", "localhost:8080");
            ReservationTime reservationTime = createTestReservationTime(TEST_TIME);
            Member member = createTestMember("홍길동", "a@com", "a");
            createTestReservation(TEST_DATE, TEST_TIME, TEST_THEME_ID, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("존재하지 않은 예약을 삭제하려고 하면 예외가 발생한다.")
        void shouldThrowExceptionWhenDeletingNonExistentReservation() {
            // when & then
            assertThatThrownBy(() -> reservationService.deleteReservationById(100L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("예약을 정상적으로 삭제한다.")
        void shouldDeleteReservationSuccessfully() {
            // when
            reservationService.deleteReservationById(1L);

            // then
            List<ReservationResponse> reservations = reservationService.getReservations(
                    new ReservationConditionRequest(null, null, null, null));
            assertThat(reservations).hasSize(0);
        }
    }

    @Nested
    @DisplayName("예약 조회 테스트")
    class ReservationQueryTest {

        @BeforeEach
        void setUp() {
            Theme theme1 = createTestTheme("테스트1", "설명", "localhost:8080");
            Theme theme2 = createTestTheme("테스트2", "설명", "localhost:8080");
            ReservationTime reservationTime = createTestReservationTime(TEST_TIME);
            Member member = createTestMember("홍길동", "a@com", "a");

            createTestReservation(LocalDate.of(2040, 10, 6), TEST_TIME, 1L, TEST_MEMBER_ID);
            createTestReservation(LocalDate.of(2040, 10, 7), TEST_TIME, 2L, TEST_MEMBER_ID);
            createTestReservation(LocalDate.of(2040, 10, 8), TEST_TIME, 2L, TEST_MEMBER_ID);
        }

        @Test
        @DisplayName("조건이 없을 때는 모든 예약을 조회한다.")
        void shouldGetAllReservationsWhenNoCondition() {
            // given
            ReservationConditionRequest request = new ReservationConditionRequest(null, null, null, null);

            // when
            List<ReservationResponse> reservations = reservationService.getReservations(request);

            // then
            assertThat(reservations).hasSize(3);
        }

        @Test
        @DisplayName("memberId로 예약 조회 시 올바른 결과를 반환한다.")
        void shouldGetReservationsByMemberId() {
            // given
            ReservationConditionRequest request = new ReservationConditionRequest(1L, null, null, null);

            // when
            List<ReservationResponse> reservations = reservationService.getReservations(request);

            // then
            assertThat(reservations).hasSize(3);
        }

        @Test
        @DisplayName("themeId로 예약 조회 시 올바른 결과를 반환한다.")
        void shouldGetReservationsByThemeId() {
            // given
            ReservationConditionRequest request = new ReservationConditionRequest(null, 2L, null, null);

            // when
            List<ReservationResponse> reservations = reservationService.getReservations(request);

            // then
            assertThat(reservations).hasSize(2);
        }

        @ParameterizedTest
        @MethodSource("provideDateFromConditions")
        @DisplayName("dateFrom 조건으로 예약 조회 시 올바른 결과를 반환한다.")
        void shouldGetReservationsByDateFrom(LocalDate dateFrom, int expectedCount) {
            // given
            ReservationConditionRequest request = new ReservationConditionRequest(null, null, dateFrom, null);

            // when
            List<ReservationResponse> reservations = reservationService.getReservations(request);

            // then
            assertThat(reservations).hasSize(expectedCount);
        }

        private static Stream<Arguments> provideDateFromConditions() {
            return Stream.of(
                    Arguments.of(LocalDate.of(2040, 10, 6), 3),
                    Arguments.of(LocalDate.of(2040, 10, 7), 2),
                    Arguments.of(LocalDate.of(2040, 10, 8), 1),
                    Arguments.of(LocalDate.of(2040, 10, 9), 0)
            );
        }

        @ParameterizedTest
        @MethodSource("provideDateToConditions")
        @DisplayName("dateTo 조건으로 예약 조회 시 올바른 결과를 반환한다.")
        void shouldGetReservationsByDateTo(LocalDate dateTo, int expectedCount) {
            // given
            ReservationConditionRequest request = new ReservationConditionRequest(null, null, null, dateTo);

            // when
            List<ReservationResponse> reservations = reservationService.getReservations(request);

            // then
            assertThat(reservations).hasSize(expectedCount);
        }

        private static Stream<Arguments> provideDateToConditions() {
            return Stream.of(
                    Arguments.of(LocalDate.of(2040, 10, 8), 3),
                    Arguments.of(LocalDate.of(2040, 10, 7), 2),
                    Arguments.of(LocalDate.of(2040, 10, 6), 1),
                    Arguments.of(LocalDate.of(2040, 10, 5), 0)
            );
        }
    }

    @Nested
    @DisplayName("예약 취소 및 대기 처리 테스트")
    class ReservationCancellationAndWaitingTest {

        @BeforeEach
        void setUp() {
            Theme theme = createTestTheme("테스트1", "설명", "localhost:8080");
            ReservationTime reservationTime = createTestReservationTime(TEST_TIME);
            Member member1 = createTestMember("홍길동", "a@com", "a");
            Member member2 = createTestMember("포라", "fora@com", "1234");
            Member member3 = createTestMember("승연", "sy@com", "1234");

            createTestReservation(LocalDate.of(2026, 10, 6), TEST_TIME, TEST_THEME_ID, TEST_MEMBER_ID);
            createTestWaiting(member2, LocalDate.of(2026, 10, 6), TEST_TIME, TEST_THEME_ID);
            createTestWaiting(member3, LocalDate.of(2026, 10, 6), TEST_TIME, TEST_THEME_ID);
        }

        @Test
        @DisplayName("예약을 취소하면 대기 중인 예약이 자동으로 예약된다")
        void shouldAutomaticallyReserveNextWaitingWhenReservationIsCancelled() {
            // when
            reservationService.deleteReservationById(1L);

            // then
            List<ReservationResponse> reservations = reservationService.getReservations(
                    new ReservationConditionRequest(2L, TEST_THEME_ID, null, null)
            );
            List<Waiting> remainings = waitingRepository.findAll();

            assertThat(reservations).hasSize(1);
            assertThat(reservations.getFirst().member().name()).isEqualTo("포라");
            assertThat(remainings.getFirst().getMember().getName()).isEqualTo("승연");
        }
    }

    private Theme createTestTheme(String name, String description, String thumbnail) {
        Theme theme = TestFixture.createThemeWithoutId(name, description, thumbnail);
        ReflectionTestUtils.setField(theme, "id", (long) themes.size() + 1);
        themeRepository.save(theme);
        return theme;
    }

    private ReservationTime createTestReservationTime(LocalTime time) {
        ReservationTime reservationTime = ReservationTime.createWithoutId(time);
        reservationTimeRepository.save(reservationTime);
        return reservationTime;
    }

    private Member createTestMember(String name, String email, String password) {
        Member member = TestFixture.createMemberWithoutId(name, email, password);
        ReflectionTestUtils.setField(member, "id", (long) memberRepository.findAll().size() + 1);
        memberRepository.save(member);
        return member;
    }

    private Reservation createTestReservation(LocalDate date, LocalTime time, Long themeId, Long memberId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow();
        ReservationTime reservationTime = ReservationTime.createWithId(1L, time);

        Reservation reservation = Reservation.createWithoutId(
                LocalDateTime.now(),
                member,
                date,
                reservationTime,
                theme
        );
        reservationRepository.save(reservation);
        return reservation;
    }

    private Waiting createTestWaiting(Member member, LocalDate date, LocalTime time, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime reservationTime = ReservationTime.createWithId(1L, time);

        Waiting waiting = TestFixture.createWaiting(
                member,
                date,
                reservationTime,
                theme,
                LocalDateTime.now()
        );
        waitingRepository.save(waiting);
        return waiting;
    }
}
