package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.event.TestReservationCancelListener;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;

class DeleteReservationServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestReservationCancelListener testListener;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private DeleteReservationService deleteReservationService;

    @BeforeEach
    void setUp() {
        deleteReservationService = new DeleteReservationService(reservationRepository, eventPublisher);
        testListener.reset();
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time, theme)
        );

        // when
        deleteReservationService.cancelById(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId())).isNotPresent();
    }

    @Test
    void 예약_삭제시_이벤트가_발생한다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time, theme)
        );

        // when
        deleteReservationService.cancelById(reservation.getId());

        // then
        assertThat(testListener.isCalled())
                .isTrue();
    }

    @TestConfiguration
    static class TestListenerConfig {

        @Bean
        public TestReservationCancelListener testReservationCancelListener() {
            return new TestReservationCancelListener();
        }
    }
}
