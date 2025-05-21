package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.service.ReservationServiceTest.ReservationConfig;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepositoryAdaptor;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdaptor;

@DataJpaTest
@Import(ReservationConfig.class)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @DisplayName("멤버별 예약을 조회 할 수 있다.")
    @Test
    void can_find_my_reservation() {
        // given
        Theme theme = jpaThemeRepository.save(new Theme("test", "test", "test"));
        ReservationTime time1 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        ReservationTime time2 = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member = jpaMemberRepository.save(
            new Member(new Name("율무"), new Email("test@email.com"), new Password("password"))
        );
        LocalDate nowLater4Days = LocalDate.now().plusDays(4);

        Reservation reservation1 = jpaReservationRepository.save(
            new Reservation(nowLater4Days, time1, theme, member, ReservationStatus.RESERVED)
        );
        Reservation reservation2 = jpaReservationRepository.save(
            new Reservation(nowLater4Days, time2, theme, member, ReservationStatus.RESERVED)
        );

        // when
        List<MyReservationResponse> result = reservationService.getMemberReservations(
            new LoginMemberInfo(member.getId())
        );

        // then
        List<MyReservationResponse> expected = List.of(
            new MyReservationResponse(reservation1.getId(), "test", nowLater4Days, time1.getStartAt(), "예약"),
            new MyReservationResponse(reservation2.getId(), "test", nowLater4Days, time2.getStartAt(), "예약"));
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약을 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource
    void cant_not_reserve_before_now(final long days, final Long timeId) {
        LocalDate date = LocalDate.now().minusDays(days);

        Assertions.assertThatThrownBy(
                        () -> reservationService.createReservation(new ReservationRequest(date, timeId, 1L), 1L))
                .isInstanceOf(BusinessException.class);
    }

    private static Stream<Arguments> cant_not_reserve_before_now() {
        return Stream.of(
                Arguments.of(1L, 1L),
                Arguments.of(3L, 1L),
                Arguments.of(6L, 1L),
                Arguments.of(2L, 2L)
        );
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        Assertions.assertThatThrownBy(() -> reservationService.createReservation(
                        new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(BusinessException.class);
    }

    static class ReservationConfig {

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
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
        ) {
            return new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository);
        }
    }
}
