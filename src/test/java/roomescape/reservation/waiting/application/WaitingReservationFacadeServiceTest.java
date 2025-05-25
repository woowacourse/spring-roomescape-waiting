package roomescape.reservation.waiting.application;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.common.exception.BusinessException;
import roomescape.member.application.service.MemberQueryService;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservation.infrastructure.JpaReservationRepositoryAdapter;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.time.application.service.ReservationTimeQueryService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.domain.ReservationTimeRepository;
import roomescape.reservation.time.infrastructure.JpaReservationTimeRepository;
import roomescape.reservation.time.infrastructure.JpaReservationTimeRepositoryAdaptor;
import roomescape.reservation.waiting.application.WaitingReservationFacadeServiceTest.WaitingReservationConfig;
import roomescape.reservation.waiting.application.service.WaitingReservationCommandService;
import roomescape.reservation.waiting.application.service.WaitingReservationQueryService;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.infrastructure.JpaWaitingReservationRepository;
import roomescape.reservation.waiting.infrastructure.JpaWaitingReservationRepositoryAdaptor;
import roomescape.reservation.waiting.presentation.dto.WaitingReservationResponse;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepository;
import roomescape.theme.infrastructure.JpaThemeRepositoryAdaptor;

@DataJpaTest
@Import(WaitingReservationConfig.class)
class WaitingReservationFacadeServiceTest {

    @Autowired
    private WaitingReservationFacadeService waitingReservationFacadeService;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약이 있는 경우 예약 대기를 할 수 있다.")
    @Test
    void can_wait_for_reserve() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when
        WaitingReservationResponse waitingReservation = waitingReservationFacadeService.createWaitingReservation(
            reservationRequest, member.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.findById(waitingReservation.id()))
            .isNotNull();
    }

    @DisplayName("중복 예약 대기를 할 수 없다.")
    @Test
    void not_accept_duplicate_wait() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when
        waitingReservationFacadeService.createWaitingReservation(reservationRequest, member.getId());

        // then
        Assertions.assertThatThrownBy(() -> waitingReservationFacadeService.createWaitingReservation(reservationRequest, member.getId()))
            .isInstanceOf(BusinessException.class);
    }

    @DisplayName("없는 예약을 대기할 수 없다.")
    @Test
    void not_accept_waiting_if_reservation_not_exist() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when-then
        Assertions.assertThatThrownBy(() -> waitingReservationFacadeService.createWaitingReservation(reservationRequest, member.getId()))
            .isInstanceOf(BusinessException.class);
    }

    @DisplayName("대기 예약을 승인할 수 있다.")
    @Test
    void can_accept_waiting_reservation() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        WaitingReservation waitingReservation = waitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));

        // when
        waitingReservationFacadeService.acceptWaiting(waitingReservation.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.existsById(waitingReservation.getId()))
            .isFalse();
        Assertions.assertThat(reservationRepository.findAll())
            .isNotEmpty();
    }

    @DisplayName("대기 예약을 거절할 수 있다.")
    @Test
    void can_deny_waiting_reservation() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        WaitingReservation waitingReservation = waitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));

        // when
        waitingReservationFacadeService.denyWaiting(waitingReservation.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.existsById(waitingReservation.getId()))
            .isFalse();
        Assertions.assertThat(reservationRepository.findAll())
            .isEmpty();
    }

    static class WaitingReservationConfig {

        @Bean
        public ReservationRepository reservationRepository(JpaReservationRepository jpaReservationRepository) {
            return new JpaReservationRepositoryAdapter(jpaReservationRepository);
        }

        @Bean
        public WaitingReservationRepository waitingReservationRepository(JpaWaitingReservationRepository jpaWaitingReservationRepository) {
            return new JpaWaitingReservationRepositoryAdaptor(jpaWaitingReservationRepository);
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
        public ReservationQueryService reservationQueryService(ReservationRepository reservationRepository) {
            return new ReservationQueryService(reservationRepository);
        }

        @Bean
        public ReservationCommandService reservationCommandService(ReservationRepository reservationRepository) {
            return new ReservationCommandService(reservationRepository);
        }

        @Bean
        public WaitingReservationCommandService waitingReservationCommandService(WaitingReservationRepository waitingReservationRepository) {
            return new WaitingReservationCommandService(waitingReservationRepository);
        }

        @Bean
        public WaitingReservationQueryService waitingReservationQueryService(WaitingReservationRepository waitingReservationRepository) {
            return new WaitingReservationQueryService(waitingReservationRepository);
        }

        @Bean
        public ReservationTimeQueryService reservationTimeQueryService(ReservationTimeRepository reservationTimeRepository) {
            return new ReservationTimeQueryService(reservationTimeRepository);
        }

        @Bean
        public ThemeQueryService themeQueryService(ThemeRepository themeRepository) {
            return new ThemeQueryService(themeRepository);
        }

        @Bean
        public MemberQueryService memberQueryService(MemberRepository memberRepository) {
            return new MemberQueryService(memberRepository);
        }

        @Bean
        public WaitingReservationFacadeService waitingReservationFacadeService(ReservationQueryService reservationQueryService,
                                                                               ReservationCommandService reservationCommandService,
                                                                               WaitingReservationCommandService waitingReservationCommandService,
                                                                               WaitingReservationQueryService waitingReservationQueryService,
                                                                               ReservationTimeQueryService timeQueryService,
                                                                               ThemeQueryService themeQueryService,
                                                                               MemberQueryService memberQueryService) {
            return new WaitingReservationFacadeService(
                reservationQueryService,
                reservationCommandService,
                waitingReservationCommandService,
                waitingReservationQueryService,
                timeQueryService,
                themeQueryService,
                memberQueryService
            );
        }
    }
}
