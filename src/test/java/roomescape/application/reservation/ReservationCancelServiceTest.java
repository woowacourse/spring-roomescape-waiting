package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
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
}
