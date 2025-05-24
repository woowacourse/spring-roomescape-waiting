package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
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
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.WaitingException;

class DeleteWaitingServiceTest extends AbstractServiceIntegrationTest {

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


    private DeleteWaitingService deleteWaitingService;

    @BeforeEach
    void setUp() {
        deleteWaitingService = new DeleteWaitingService(
                waitingRepository,
                memberRepository
        );
    }

    @Test
    void 대기신청을_취소할_수_있다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Member admin = memberRepository.save(new Member("관리자", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Long waitingId = waitingRepository.save(waiting).getId();

        // when
        deleteWaitingService.cancel(waitingId, admin.getId());

        // then
        assertThat(waitingRepository.findById(waitingId))
                .isEmpty();
    }

    @Test
    void 자신의_대기가_아닌_경우_예외를_발생시킨다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Member another = memberRepository.save(new Member("서프", new Email("test2@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Long waitingId = waitingRepository.save(waiting).getId();

        // when
        // then
        assertThatCode(() -> deleteWaitingService.cancel(waitingId, another.getId()))
                .isInstanceOf(WaitingException.class)
                .hasMessage("대기 취소 권한이 없습니다.");
    }

    @Test
    void 존재하지_않는_회원은_대기신청을_취소할_수_없다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Long waitingId = waitingRepository.save(waiting).getId();
        long invalidMemberId = 999L;

        // when
        // then
        assertThatCode(() -> deleteWaitingService.cancel(waitingId, invalidMemberId))
                .isInstanceOf(MemberException.class)
                .hasMessage("회원 정보가 존재하지 않습니다.");
    }
}