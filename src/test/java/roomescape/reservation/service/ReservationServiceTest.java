package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;
import roomescape.member.service.FakeMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

class ReservationServiceTest {

    private DateTime dateTime = new DateTime() {
        @Override
        public LocalDateTime now() {
            return LocalDateTime.of(2025, 10, 5, 10, 0);
        }

        @Override
        public LocalDate nowDate() {
            return LocalDate.of(2025, 10, 5);
        }
    };

    private List<Reservation> reservations = new ArrayList<>();
    private List<Theme> themes = new ArrayList<>();
    private List<ReservationTime> reservationTimes = new ArrayList<>();
    private ThemeRepository themeRepository = new FakeThemeRepository(themes, reservations);
    private ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository(reservationTimes);
    private ReservationRepository reservationRepository = new FakeReservationRepository(reservations);
    private MemberRepository memberRepository = new FakeMemberRepository(new ArrayList<>());
    private ReservationService reservationService = new ReservationService(dateTime, reservationRepository,
            reservationTimeRepository, themeRepository, memberRepository);

    private static Stream<Arguments> cant_not_reserve_before_now() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 10, 5), 1L),
                Arguments.of(LocalDate.of(2025, 9, 5), 1L),
                Arguments.of(LocalDate.of(2025, 10, 4), 1L),
                Arguments.of(LocalDate.of(2025, 10, 5), 2L)
        );
    }

    private static Stream<Arguments> getConditionalReservations_test() {
        return Stream.of(
                Arguments.of(1L, null, null, null, 3),
                Arguments.of(2L, null, null, null, 0),
                Arguments.of(null, 1L, null, null, 1),
                Arguments.of(null, 2L, null, null, 2),
                Arguments.of(null, 3L, null, null, 0),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), null, 3),
                Arguments.of(null, null, LocalDate.of(2024, 10, 7), null, 2),
                Arguments.of(null, null, LocalDate.of(2024, 10, 8), null, 1),
                Arguments.of(null, null, LocalDate.of(2024, 10, 9), null, 0),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 8), 3),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 7), 2),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 6), 1),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 5), 0),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), LocalDate.of(2024, 10, 7), 2)
        );
    }

    @BeforeEach
    void beforeEach() {
        Theme theme1 = Theme.createWithId(1L, "테스트1", "설명", "localhost:8080");
        Theme theme2 = Theme.createWithId(2L, "테스트2", "설명", "localhost:8080");
        Theme theme3 = Theme.createWithId(3L, "테스트2", "설명", "localhost:8080");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        themeRepository.save(theme3);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Member member = Member.createWithId(1L, "홍길동", "a@com", "a", Role.USER);
        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2024, 10, 6),
                        reservationTime1, theme1));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2024, 10, 7),
                        reservationTime1, theme2));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2024, 10, 8),
                        reservationTime1, theme2));
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_before_now(final LocalDate date, final Long timeId) {
        assertThatThrownBy(
                () -> reservationService.createReservation(new ReservationRequest(date, timeId, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약을 정상적으로 삭제한다.")
    void deleteReservationByGetId_test() {
        // when
        reservationService.deleteReservationById(1L);
        // then
        List<ReservationResponse> reservations = reservationService.getReservations(
                new ReservationConditionRequest(null, null, null, null));
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("조건이 없을 때는 모든 예약을 들고 온다.")
    void getAllReservations_test() {
        // given
        ReservationConditionRequest request = new ReservationConditionRequest(null, null, null, null);
        // when
        List<ReservationResponse> reservations = reservationService.getReservations(request);
        // then
        assertThat(reservations).hasSize(3);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("조건이 있을 경우 조건에 맞는 예약을 들고 온다.")
    void getConditionalReservations_test(Long memberId, Long themeId, LocalDate dateForm, LocalDate dateTo,
                                         int expectedCount) {
        // given
        ReservationConditionRequest request = new ReservationConditionRequest(memberId, themeId, dateForm, dateTo);
        // when
        List<ReservationResponse> reservations = reservationService.getReservations(request);
        // then
        assertThat(reservations).hasSize(expectedCount);
    }

    @Test
    @DisplayName("본인 예약들을 dto로 변환한다.")
    void getMyReservations_dto_test() {
        // given
        MyReservationResponse expected1 = MyReservationResponse.from(reservations.get(0));
        MyReservationResponse expected2 = MyReservationResponse.from(reservations.get(1));
        MyReservationResponse expected3 = MyReservationResponse.from(reservations.get(2));

        // when
        List<MyReservationResponse> responses = reservationService.getMyReservations(1L);
        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0)).isEqualTo(expected1);
        assertThat(responses.get(1)).isEqualTo(expected2);
        assertThat(responses.get(2)).isEqualTo(expected3);
    }
}
