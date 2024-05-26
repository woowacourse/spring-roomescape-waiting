package roomescape.service.reservationwait;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.reservationwait.ReservationWaitStatus;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;

class ReservationWaitFindServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitRepository reservationWaitRepository;

    @Autowired
    private ReservationWaitFindService reservationWaitFindService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
    }

    @Test
    @DisplayName("예약 대기 된 모든 목록을 조회한다.")
    void findAllReservationWaits() {
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        reservationWaitRepository.save(new ReservationWait(member, LocalDate.now().plusDays(1L), time, theme, ReservationWaitStatus.WAITING));
        Member member2 = memberRepository.save(new Member(new MemberName("사용자2"),
                new MemberEmail("user2@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
        reservationWaitRepository.save(new ReservationWait(member2, LocalDate.now().plusDays(1L), time, theme, ReservationWaitStatus.WAITING));

        assertThat(reservationWaitFindService.findReservationWaits()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 아이디로 예약 대기 된 목록을 찾는다.")
    void findUserReservationWaits() {
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        reservationWaitRepository.save(new ReservationWait(member, LocalDate.now().plusDays(1L), time, theme, ReservationWaitStatus.WAITING));
        reservationWaitRepository.save(new ReservationWait(member, LocalDate.now().plusDays(1L), time, theme, ReservationWaitStatus.WAITING));

        assertThat(reservationWaitFindService.findUserReservationWaits(1L)).hasSize(2);
    }
}
