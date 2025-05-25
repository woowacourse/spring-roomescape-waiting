package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.dto.CreateWaitingParam;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;

class WaitingServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private WaitingService waitingService;

    @Test
    void 예약_대기를_생성할_수_있다() {
        //given
        Member member1 = memberRepository.save(Member.create("벨로", new Email("test1@email.com"), "pw", Role.NORMAL));
        Member member2 = memberRepository.save(Member.create("서프", new Email("test2@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        reservationRepository.save(Reservation.create(
                LocalDateTime.now(clock).minusDays(1),
                member1,
                new ReservationSlot(LocalDate.now(clock), time, theme))
        );
        CreateWaitingParam createWaitingParam = new CreateWaitingParam(
                LocalDate.now(clock),
                member2.getId(),
                time.getId(),
                theme.getId()
        );

        //when
        waitingService.create(createWaitingParam);

        //then
        assertThat(waitingRepository.findById(1L))
                .isPresent().get()
                .extracting("member", "reservationSlot")
                .contains(member2, new ReservationSlot(LocalDate.now(clock), time, theme));
    }

    @Test
    void 예약_대기를_할때_이미_해당_사용자가_예약중이라면_예외가_발생한다() {
        //given
        Member member1 = memberRepository.save(Member.create("벨로", new Email("test1@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        reservationRepository.save(Reservation.create(
                LocalDateTime.now(clock).minusDays(1),
                member1,
                new ReservationSlot(LocalDate.now(clock), time, theme))
        );
        CreateWaitingParam createWaitingParam = new CreateWaitingParam(
                LocalDate.now(clock),
                member1.getId(),
                time.getId(),
                theme.getId()
        );

        //when
        //then
        assertThatThrownBy(() -> waitingService.create(createWaitingParam))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이미 예약중입니다.");
    }

    @Test
    void 예약_대기를_할때_바로_예약이_가능하다면_예외가_발생한다() {
        //given
        Member member1 = memberRepository.save(Member.create("벨로", new Email("test1@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        CreateWaitingParam createWaitingParam = new CreateWaitingParam(
                LocalDate.now(clock),
                member1.getId(),
                time.getId(),
                theme.getId()
        );

        //when
        //then
        assertThatThrownBy(() -> waitingService.create(createWaitingParam))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("예약이 바로 가능해 예약 대기를 할 수 없습니다.");
    }

    @Test
    void 예약_대기를_할때_이미_예약에_대기중이라면_예외가_발생한다() {
        //given
        Member member1 = memberRepository.save(Member.create("벨로", new Email("test1@email.com"), "pw", Role.NORMAL));
        Member member2 = memberRepository.save(Member.create("서프", new Email("test2@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        reservationRepository.save(Reservation.create(
                LocalDateTime.now(clock).minusDays(1),
                member1,
                new ReservationSlot(LocalDate.now(clock), time, theme))
        );
        waitingRepository.save(Waiting.create(
                LocalDateTime.now(clock).minusDays(1),
                new ReservationSlot(LocalDate.now(clock), time, theme),
                member2
        ));
        CreateWaitingParam createWaitingParam = new CreateWaitingParam(
                LocalDate.now(clock),
                member2.getId(),
                time.getId(),
                theme.getId()
        );

        //when
        //then
        assertThatThrownBy(() -> waitingService.create(createWaitingParam))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이미 예약 대기 중입니다.");
    }
}
