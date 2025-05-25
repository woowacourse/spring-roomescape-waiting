package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
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

class ReservationCancelServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationCancelService reservationCancelService;

    @Test
    void 예약을_취소할_수_있다() {
        // given
        Member member = memberRepository.save(Member.create("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        Reservation reservation = reservationRepository.save(
                Reservation.create(member, new ReservationSlot(LocalDate.now(clock), time, theme)));

        // when
        reservationCancelService.cancel(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId())).isNotPresent();
    }

    @Test
    void 예약을_취소할때_예약_대기가_존재한다면_대기를_가장_먼저한_예약_대기가_예약으로_자동_승급된다() {
        // given
        Member member1 = memberRepository.save(Member.create("벨로", new Email("test1@email.com"), "pw", Role.NORMAL));
        Member member2 = memberRepository.save(Member.create("서프", new Email("test2@email.com"), "pw", Role.NORMAL));
        Member member3 = memberRepository.save(Member.create("샐리", new Email("test3@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(Theme.create("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));

        ReservationSlot reservationSlot = new ReservationSlot(LocalDate.now(clock), time, theme);
        Reservation reservation = reservationRepository.save(Reservation.create(member1, reservationSlot));
        LocalDateTime waitingStartedAt = LocalDateTime.now(clock).minusDays(1);
        Waiting waiting = waitingRepository.save(Waiting.create(waitingStartedAt, reservationSlot, member2));
        waitingRepository.save(Waiting.create(waitingStartedAt.plusHours(1), reservationSlot, member3));

        // when
        reservationCancelService.cancel(reservation.getId());

        // then
        assertThat(reservationRepository.findAll())
                .hasSize(1)
                .extracting("member", "reservationSlot")
                .contains(Tuple.tuple(member2, reservationSlot));
        assertThat(waitingRepository.findById(waiting.getId())).isNotPresent();
    }
}
