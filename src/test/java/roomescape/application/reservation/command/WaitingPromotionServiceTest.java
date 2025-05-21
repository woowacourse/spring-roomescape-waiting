package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

class WaitingPromotionServiceTest extends AbstractServiceIntegrationTest {

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

    private WaitingPromotionService waitingPromotionService;

    @BeforeEach
    void setUp() {
        waitingPromotionService = new WaitingPromotionService(
                waitingRepository,
                reservationRepository,
                memberRepository,
                clock
        );
    }

    @Test
    void 관리자는_첫번째_대기를_예약으로_승인할_수_있다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate reservationDate = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        Waiting waiting = waitingRepository.save(new Waiting(member, reservationDate, time, theme));

        // when
        waitingPromotionService.approve(waiting.getId(), admin.getId());

        // then
        assertAll(
                () -> assertThat(waitingRepository.findById(waiting.getId())).isNotPresent(),
                () -> assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(reservationDate, time.getId(),
                        theme.getId())).isTrue()
        );
    }

    @Test
    void 관리자가_아니면_대기를_승인할_수_없다() {
        // given
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate date = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        Waiting waiting = waitingRepository.save(new Waiting(member, date, time, theme));

        // when & then
        assertThatThrownBy(() -> waitingPromotionService.approve(waiting.getId(), member.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("대기 승인 권한이 없습니다.");
    }

    @Test
    void 이미_예약이_존재하면_대기를_승인할_수_없다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        Member member = memberRepository.save(
                new Member("member", new Email("member@email.com"), "pw", MemberRole.NORMAL)
        );
        LocalDate date = LocalDate.now(clock).plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        reservationRepository.save(new Reservation(member, date, time, theme));
        Waiting waiting = waitingRepository.save(new Waiting(member, date, time, theme));

        // when & then
        assertThatCode(() -> waitingPromotionService.approve(waiting.getId(), admin.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("예약이 이미 존재하여 승인할 수 없습니다.");
    }

    @Test
    void 첫번째_대기가_아니면_승인할_수_없다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        Member first = memberRepository.save(new Member("1st", new Email("fi@email.com"), "pw", MemberRole.NORMAL));
        Member second = memberRepository.save(new Member("2nd", new Email("se@email.com"), "pw", MemberRole.NORMAL));
        LocalDate date = LocalDate.now(clock).plusDays(1);
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        waitingRepository.save(new Waiting(first, date, time, theme));
        Waiting secondWaiting = waitingRepository.save(new Waiting(second, date, time, theme));

        // when & then
        assertThatCode(() -> waitingPromotionService.approve(secondWaiting.getId(), admin.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("승인 가능한 첫 번째 대기가 아닙니다.");
    }

    @Test
    void 대기_정보가_존재하지_않으면_예외가_발생한다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        long invalidId = 999L;

        // when & then
        assertThatCode(() -> waitingPromotionService.approve(invalidId, admin.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("대기 정보가 존재하지 않습니다.");
    }
}
