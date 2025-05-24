package roomescape.application.reservation.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.application.reservation.command.DeleteReservationService;
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

@SpringBootTest
class DeleteReservationEventListenerTest {

    @Autowired
    private DeleteReservationService deleteReservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약이 삭제되면 가장 빠른 대기를 예약으로 자동 승인한다.")
    @Test
    void 예약_삭제시_가장_빠른_대기를_자동_승인한다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        Member member = memberRepository.save(new Member("user", new Email("user@email.com"), "pw", MemberRole.NORMAL));
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("공포의 하수도", "설명", "썸네일"));

        Reservation reservation = reservationRepository.save(new Reservation(admin, date, time, theme));
        Waiting firstWaiting = waitingRepository.save(new Waiting(member, date, time, theme));
        Member another = memberRepository.save(
                new Member("another", new Email("another@email.com"), "pw", MemberRole.NORMAL)
        );
        waitingRepository.save(new Waiting(another, date, time, theme));

        // when: 예약 삭제 → 이벤트 발행 → 자동 승인 기대
        deleteReservationService.cancelById(reservation.getId());

        // then
        assertAll(
                () -> assertThat(waitingRepository.findById(firstWaiting.getId())).isNotPresent(),
                () -> assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(),
                        theme.getId())).isTrue()
        );
    }

    @DisplayName("예약이 삭제되면 대기가 없으면 아무 일도 일어나지 않는다.")
    @Test
    void 예약_삭제시_대기가_없으면_아무일도_일어나지_않는다() {
        // given
        Member admin = memberRepository.save(new Member("admin", new Email("admin@email.com"), "pw", MemberRole.ADMIN));
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        Theme theme = themeRepository.save(new Theme("공포의 하수도", "설명", "썸네일"));

        Reservation reservation = reservationRepository.save(new Reservation(admin, date, time, theme));

        // when: 예약 삭제 → 이벤트 발행 → 아무 일도 일어나지 않음
        deleteReservationService.cancelById(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId()))
                .isNotPresent();
    }
}
