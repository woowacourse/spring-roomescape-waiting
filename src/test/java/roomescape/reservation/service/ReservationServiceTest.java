package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.common.util.time.DateTime;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservation.infrastructure.JpaWaitingRepository;
import roomescape.reservation.infrastructure.JpaWaitingRepositoryAdapter;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationServiceTest.ReservationConfig;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepositoryAdaptor;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdaptor;

@DataJpaTest
@Import(ReservationConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @DisplayName("멤버별 예약을 조회 할 수 있다.")
    @Test
    void can_find_my_reservation() {
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(1L);

        List<MyReservationResponse> result = reservationService.getMemberReservations(loginMemberInfo);

        List<MyReservationResponse> expected = List.of(
            new MyReservationResponse(1L, "테마1", LocalDate.of(2025, 4, 28), LocalTime.of(10, 0), "예약"),
            new MyReservationResponse(2L, "테마1", LocalDate.of(2025, 4, 28), LocalTime.of(11, 0), "예약"));
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_before_now(final LocalDate date, final Long timeId) {
        Assertions.assertThatThrownBy(
            () -> reservationService.createReservation(new ReservationRequest(date, timeId, 1L), 1L))
            .isInstanceOf(ReservationException.class);
    }

    private static Stream<Arguments> cant_not_reserve_before_now() {
        return Stream.of(
            Arguments.of(LocalDate.of(2024, 10, 5), 1L),
            Arguments.of(LocalDate.of(2024, 9, 5), 1L),
            Arguments.of(LocalDate.of(2024, 10, 4), 1L),
            Arguments.of(LocalDate.of(2024, 10, 5), 2L)
        );
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        Assertions.assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(LocalDate.of(2025, 4, 28), 1L, 1L), 1L))
            .isInstanceOf(ReservationException.class);
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약대기를 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_waiting_before_now(final LocalDate date, final Long timeId, final Long themeId, final Long memberId) {
        Assertions.assertThatThrownBy(
                () -> reservationService.createWaiting(new ReservationRequest(date, timeId, themeId), memberId))
            .isInstanceOf(ReservationException.class)
            .hasMessage("예약할 수 없는 날짜와 시간입니다.");
    }

    private static Stream<Arguments> cant_not_reserve_waiting_before_now() {
        return Stream.of(
            Arguments.of(LocalDate.of(2025, 4, 26), 1L, 3L, 1L),
            Arguments.of(LocalDate.of(2025, 4, 18), 1L, 2L, 1L)
        );
    }

    @DisplayName("예약한 멤버는 예약대기를 생성할 수 없다.")
    @Test
    void cant_reserve_waiting_by_reservation_owner() {
        Assertions.assertThatThrownBy(
                () -> reservationService.createWaiting(
                    new ReservationRequest(LocalDate.of(2025, 4, 28), 1L, 1L), 1L))
            .isInstanceOf(ReservationException.class)
            .hasMessage("예약자는 예약대기를 할 수 없습니다.");
    }

    @DisplayName("동일한 사용자가 중복 예약대기를 할 수 없다.")
    @Test
    void cant_reserve_waiting_by_duplicate_member() {
        Assertions.assertThatThrownBy(
                () -> reservationService.createWaiting(
                    new ReservationRequest(LocalDate.of(2025, 4, 28), 1L, 1L), 2L))
            .isInstanceOf(ReservationException.class)
            .hasMessage("이미 예약대기 중입니다.");
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void can_create_waiting() {
        ReservationRequest request = new ReservationRequest(
            LocalDate.of(2025, 4, 28),
            2L,
            1L
        );
        Long memberId = 2L;

        WaitingResponse response = reservationService.createWaiting(request, memberId);

        assertThat(response.theme()).isEqualTo("테마1");
        assertThat(response.date()).isEqualTo(LocalDate.of(2025, 4, 28));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(11, 0));
    }

    @DisplayName("멤버별 예약과 대기 목록을 조회할 수 있다.")
    @Test
    void can_find_member_reservations_and_waitings() {
        List<MyReservationResponse> responses = reservationService.getMemberReservations(new LoginMemberInfo(2L));

        assertThat(responses).containsExactly(
            new MyReservationResponse(3L, "테마3", LocalDate.of(2025, 4, 26), LocalTime.of(10, 0), "예약"),
            new MyReservationResponse(1L, "테마1", LocalDate.of(2025, 4, 28), LocalTime.of(10, 0), "1번째 예약대기")
        );
    }

    @DisplayName("예약 대기를 삭제할 수 있다.")
    @Test
    void can_delete_waiting() {
        // given
        Long waitingId = 1L;
        Long memberId = 2L;

        // when
        reservationService.deleteWaiting(waitingId);

        // then
        List<MyReservationResponse> responses = reservationService.getMemberReservations(new LoginMemberInfo(memberId));
        assertThat(responses).containsExactly(
            new MyReservationResponse(3L, "테마3", LocalDate.of(2025, 4, 26), LocalTime.of(10, 0), "예약")
        );
    }

    @DisplayName("존재하지 않는 예약 대기는 삭제할 수 없다.")
    @Test
    void cannot_delete_non_existent_waiting() {
        // given
        Long nonExistentWaitingId = 999L;

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteWaiting(nonExistentWaitingId))
            .isInstanceOf(ReservationException.class)
            .hasMessage("예약 대기를 찾을 수 없습니다.");
    }

    static class ReservationConfig {

        @Bean
        public DateTime dateTime() {
            return () -> LocalDateTime.of(2025, 4, 28, 10, 0);
        }

        @Bean
        public ReservationRepository reservationRepository(JpaReservationRepository jpaReservationRepository) {
            return new JpaReservationRepositoryAdapter(jpaReservationRepository);
        }

        @Bean
        public ReservationTimeRepository reservationTimeRepository(JpaReservationTimeRepository jpaReservationTimeRepository) {
            return new JpaReservationTimeRepositoryAdaptor(jpaReservationTimeRepository);
        }

        @Bean
        public ThemeRepository themeRepository(JpaThemeRepository jpaThemeRepository) {
            return new JpaThemeRepositoryAdaptor(jpaThemeRepository);
        }

        @Bean
        public MemberRepository memberRepository(JpaMemberRepository jpaMemberRepository) {
            return new JpaMemberRepositoryAdapter(jpaMemberRepository);
        }

        @Bean
        public WaitingRepository waitingRepository(JpaWaitingRepository jpaWaitingRepository) {
            return new JpaWaitingRepositoryAdapter(jpaWaitingRepository);
        }

        @Bean
        public ReservationService reservationService(
            DateTime dateTime,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository
        ) {
            return new ReservationService(
                dateTime,
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository,
                waitingRepository);
        }
    }
}
