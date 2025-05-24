package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.WaitingException;

class AutoWaitingPromotionServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private AutoWaitingPromotionService autoWaitingPromotionService;

    @BeforeEach
    void setUp() {
        autoWaitingPromotionService = new AutoWaitingPromotionService(
                waitingRepository,
                reservationRepository,
                clock
        );
    }

    @Test
    void 예약이_없는_대기를_승인할_수_있다() {
        // given
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate reservationDate = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        Waiting waiting = waitingRepository.save(new Waiting(member, reservationDate, time, theme));

        // when
        autoWaitingPromotionService.promote(reservationDate, time.getId(), theme.getId());

        // then
        assertAll(
                () -> assertThat(waitingRepository.findById(waiting.getId())).isNotPresent(),
                () -> assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(
                        reservationDate,
                        time.getId(),
                        theme.getId()
                ))
                        .isTrue()
        );
    }

    @Test
    void 예약이_있는_대기를_승인할_수_없다() {
        // given
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate reservationDate = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        reservationRepository.save(new Reservation(member, reservationDate, time, theme));
        waitingRepository.save(new Waiting(member, reservationDate, time, theme));

        // when
        // then
        assertThatCode(() -> autoWaitingPromotionService.promote(reservationDate, time.getId(), theme.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("예약이 존재하여 자동 대기 승인이 실패했습니다.");
    }

    @Test
    void 대기_승인이_가능한_경우_첫번째_대기를_승인한다() {
        // given
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        Member another = memberRepository.save(
                new Member("another", new Email("a@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate reservationDate = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        Waiting firstWaiting = waitingRepository.save(new Waiting(member, reservationDate, time, theme));
        Waiting secondWaiting = waitingRepository.save(new Waiting(another, reservationDate, time, theme));

        // when
        autoWaitingPromotionService.promote(reservationDate, time.getId(), theme.getId());

        // then
        assertAll(
                () -> assertThat(waitingRepository.findById(firstWaiting.getId())).isNotPresent(),
                () -> assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(
                        reservationDate,
                        time.getId(),
                        theme.getId()
                )).isTrue(),
                () -> assertThat(waitingRepository.findById(secondWaiting.getId())).isPresent()
        );
    }
}
