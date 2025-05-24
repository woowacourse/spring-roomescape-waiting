package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import roomescape.member.dto.response.ReservationMemberResponse;
import roomescape.member.service.FakeMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.request.ReservationWaitingRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingReservationResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.dto.response.ThemeResponse;

class ReservationServiceTest {

    private DateTime dateTime = new DateTime() {
        @Override
        public LocalDateTime now() {
            return LocalDateTime.of(2024, 1, 5, 10, 0);
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

    @BeforeEach
    void beforeEach() {
        Theme theme1 = Theme.createWithId(1L, "테스트1", "설명", "localhost:8080");
        Theme theme2 = Theme.createWithId(2L, "테스트2", "설명", "localhost:8080");
        Theme theme3 = Theme.createWithId(3L, "테스트3", "설명", "localhost:8080");
        theme1 = themeRepository.save(theme1);
        theme2 = themeRepository.save(theme2);
        themeRepository.save(theme3);
        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        Member member1 = Member.createWithId(1L, "홍길동", "a@com", "a", Role.USER);
        Member member2 = Member.createWithId(1L, "홍길동2", "a@com", "a", Role.USER);
        member1 = memberRepository.save(member1);
        member2 = memberRepository.save(member2);
        reservationTime1 = reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member1, LocalDate.of(2024, 10, 6),
                        reservationTime1, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member1, LocalDate.of(2024, 10, 7),
                        reservationTime1, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member1, LocalDate.of(2024, 10, 8),
                        reservationTime1, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member2, LocalDate.of(2025, 5, 22),
                        reservationTime1, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(
                Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 13), member1, LocalDate.of(2025, 5, 22),
                        reservationTime1, theme2, ReservationStatus.WAITED));
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_before_now(final LocalDate date, final Long timeId) {
        assertThatThrownBy(
                () -> reservationService.createReservation(new ReservationRequest(date, timeId, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약할 수 없는 날짜와 시간입니다.");
    }

    private static Stream<Arguments> cant_not_reserve_before_now() {
        return Stream.of(
                Arguments.of(LocalDate.of(2023, 10, 5), 1L),
                Arguments.of(LocalDate.of(2024, 1, 3), 1L),
                Arguments.of(LocalDate.of(2024, 1, 2), 1L),
                Arguments.of(LocalDate.of(2024, 1, 1), 2L)
        );
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약이 존재합니다.");
    }

    @Test
    @DisplayName("예약을 정상적으로 삭제한다.")
    void deleteReservationByGetId_test() {
        // when
        reservationService.deleteReservationById(1L);
        // then
        List<ReservationResponse> reservations = reservationService.getAllReservations(
                new ReservationConditionRequest(null, null, null, null));
        assertThat(reservations).hasSize(4);
    }

    @Test
    @DisplayName("조건이 없을 때는 모든 예약을 들고 온다.")
    void getAllReservations_test() {
        // given
        ReservationConditionRequest request = new ReservationConditionRequest(null, null, null, null);
        // when
        List<ReservationResponse> reservations = reservationService.getAllReservations(request);
        // then
        assertThat(reservations).hasSize(5);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("조건이 있을 경우 조건에 맞는 예약을 들고 온다.")
    void getConditionalReservations_test(Long memberId, Long themeId, LocalDate dateForm, LocalDate dateTo,
                                         int expectedCount) {
        // given
        ReservationConditionRequest request = new ReservationConditionRequest(memberId, themeId, dateForm, dateTo);
        // when
        List<ReservationResponse> reservations = reservationService.getAllReservations(request);
        // then
        assertThat(reservations).hasSize(expectedCount);
    }

    private static Stream<Arguments> getConditionalReservations_test() {
        return Stream.of(
                Arguments.of(1L, null, null, null, 4),
                Arguments.of(2L, null, null, null, 1),
                Arguments.of(null, 1L, null, null, 1),
                Arguments.of(null, 2L, null, null, 4),
                Arguments.of(null, 3L, null, null, 0),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), null, 5),
                Arguments.of(null, null, LocalDate.of(2024, 10, 7), null, 4),
                Arguments.of(null, null, LocalDate.of(2024, 10, 8), null, 3),
                Arguments.of(null, null, LocalDate.of(2024, 10, 9), null, 2),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 8), 3),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 7), 2),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 6), 1),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 5), 0),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), LocalDate.of(2024, 10, 7), 2)
        );
    }

    @Test
    @DisplayName("예약 가능한 상태에서는 대기 상태로 예약할 수 없다.")
    void createWaitingReservation_whenCanReserve() {
        assertThatThrownBy(() -> reservationService.createWaitingReservation(
                new ReservationWaitingRequest(LocalDate.of(2025, 5, 21), 1L, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 가능한 상태에서는 대기할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 대기 상태로 예약한 경우 대기 상태로 예약할 수 없다.")
    void createWaitingReservation_whenAlreadyWaiting() {
        assertThatThrownBy(() -> reservationService.createWaitingReservation(
                new ReservationWaitingRequest(LocalDate.of(2025, 5, 22), 1L, 2L), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약 대기를 신청했습니다");
    }

    @Test
    @DisplayName("정상적인 대기 신청인 경우 dto를 만든다.")
    void createWaitingReservation_test() {
        // given
        ReservationResponse expected = new ReservationResponse(
                6L, "대기", new ReservationMemberResponse("홍길동2"),
                LocalDate.of(2024, 10, 8),
                new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                new ThemeResponse(2L, "테스트2", "설명", "localhost:8080")
        );
        // when
        ReservationResponse response = reservationService.createWaitingReservation(
                new ReservationWaitingRequest(LocalDate.of(2024, 10, 8), 1L, 2L), 2L);
        // then
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("본인 예약들을 dto로 변환한다.")
    void getMyReservations_dto_test() {
        // given
        MyReservationResponse expected1 = MyReservationResponse.from(new ReservationWithRank(reservations.get(0), 0L));
        MyReservationResponse expected2 = MyReservationResponse.from(new ReservationWithRank(reservations.get(1), 0L));
        MyReservationResponse expected3 = MyReservationResponse.from(new ReservationWithRank(reservations.get(2), 0L));
        MyReservationResponse expected4 = MyReservationResponse.from(new ReservationWithRank(reservations.get(4), 1L));

        // when
        List<MyReservationResponse> responses = reservationService.getMyReservations(1L);
        // then
        assertThat(responses).hasSize(4);
        assertThat(responses.get(0)).isEqualTo(expected1);
        assertThat(responses.get(1)).isEqualTo(expected2);
        assertThat(responses.get(2)).isEqualTo(expected3);
        assertThat(responses.get(3)).isEqualTo(expected4);
    }

    @Test
    @DisplayName("정상적으로 예약 대기를 dto로 변환한다.")
    void getWaitingReservations_test() {
        // given
        WaitingReservationResponse expected = new WaitingReservationResponse(5L, "홍길동", "테스트2",
                LocalDate.of(2025, 5, 22), LocalTime.of(10, 0));
        // when
        List<WaitingReservationResponse> waitingReservations = reservationService.getWaitingReservations();
        // then
        assertThat(waitingReservations).hasSize(1);
        assertThat(waitingReservations.get(0)).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않은 예약 번호로 예약 상태를 변경하려고하면 예외가 발생한다.")
    void changeWaitStatusToReserved_exception_noId() {
        assertThatThrownBy(() -> reservationService.changeWaitStatusToReserved(100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 예약대기입니다.");
    }

    @Test
    @DisplayName("기존 확정된 예약을 삭제하지 않고 확정으로 변경하려고하면 예외가 발생한다.")
    void changeWaitStatusToReserved_exception_hasReservedReservation() {
        assertThatThrownBy(() -> reservationService.changeWaitStatusToReserved(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기존 확정 예약이 취소 되지 않았습니다.");
    }

    @Test
    @DisplayName("정상적인 상황이면 예약을 확정시킨다.")
    void changeWaitStatusToReserved_test() {
        // given
        reservationService.deleteReservationById(4L);
        // when
        reservationService.changeWaitStatusToReserved(5L);
        // then
        Optional<Reservation> findReservation = reservations.stream()
                .filter(reservation -> reservation.getId().equals(5L))
                .findAny();
        assertThat(findReservation).isPresent();
        assertThat(findReservation.get().getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}
