package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.command.dto.CreateWaitingCommand;
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
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.WaitingException;

class CreateWaitingServiceTest extends AbstractServiceIntegrationTest {

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

    private CreateWaitingService createWaitingService;

    @BeforeEach
    void setUp() {
        createWaitingService = new CreateWaitingService(
                waitingRepository,
                reservationRepository,
                memberRepository
        );
    }

    @Test
    void 예약이_존재해야_대기신청을_할_수_있다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Member requestMember = memberRepository.save(
                new Member("서프", new Email("sf@email.com"), "pw", MemberRole.NORMAL)
        );
        CreateWaitingCommand command = new CreateWaitingCommand(
                now.toLocalDate(),
                theme.getId(),
                time.getId(),
                requestMember.getId()
        );

        // when
        Long waitingId = createWaitingService.request(command);

        // then
        assertThat(waitingRepository.findById(waitingId))
                .isPresent();
    }

    @Test
    void 존재하지_않는_사용자는_대기신청을_할_수_없다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        long invalidMemberId = 999L;
        CreateWaitingCommand command = new CreateWaitingCommand(
                now.toLocalDate(),
                theme.getId(),
                time.getId(),
                invalidMemberId
        );

        // when
        // then
        assertThatCode(() -> createWaitingService.request(command))
                .isInstanceOf(MemberException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    void 존재하지_않는_예약시간으로_대기신청을_할_수_없다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        long invalidTimeId = 999L;
        CreateWaitingCommand command = new CreateWaitingCommand(
                now.toLocalDate(),
                theme.getId(),
                invalidTimeId,
                member.getId()
        );

        // when
        // then
        assertThatCode(() -> createWaitingService.request(command))
                .isInstanceOf(WaitingException.class)
                .hasMessage("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요.");
    }

    @Test
    void 존재하지_않는_테마로_대기신청을_할_수_없다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        long invalidThemeId = 999L;
        CreateWaitingCommand command = new CreateWaitingCommand(
                now.toLocalDate(),
                invalidThemeId,
                time.getId(),
                member.getId()
        );

        // when
        // then
        assertThatCode(() -> createWaitingService.request(command))
                .isInstanceOf(WaitingException.class)
                .hasMessage("예약이 존재하지 않아 대기를 신청할 수 없습니다. 바로 예약을 진행해주세요.");
    }

    @Test
    void 대기신청을_중복으로_할_수_없다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Member requestMember = memberRepository.save(
                new Member("서프", new Email("sf@email.com"), "pw", MemberRole.NORMAL)
        );
        CreateWaitingCommand command = new CreateWaitingCommand(
                now.toLocalDate(),
                theme.getId(),
                time.getId(),
                requestMember.getId()
        );
        createWaitingService.request(command);

        // when
        // then
        assertThatCode(() -> createWaitingService.request(command))
                .isInstanceOf(WaitingException.class)
                .hasMessage("이미 예약했거나 대기 중입니다.");
    }
}
