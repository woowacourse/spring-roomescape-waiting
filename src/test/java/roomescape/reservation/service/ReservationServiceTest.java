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
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.exception.ReservationException;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservation.presentation.dto.ReservationRequest;
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
                        new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(ReservationException.class);
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
        public ReservationService reservationService(
            DateTime dateTime,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
        ) {
            return new ReservationService(
                dateTime,
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository);
        }
    }
}
