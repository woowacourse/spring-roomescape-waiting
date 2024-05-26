package roomescape.reservation.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.RepositoryTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.WOOTECO_THEME;

class WaitingRepositoryTest extends RepositoryTest {
    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;
    private LocalDate tomorrow;
    private Theme theme;
    private Member roro;
    private Member sudal;

    @BeforeEach
    void setUp() {
        this.reservationTime = reservationTimeRepository.save(new ReservationTime(MIA_RESERVATION_TIME));
        this.tomorrow = LocalDate.now().plusDays(1);
        this.theme = themeRepository.save(WOOTECO_THEME());
        this.roro = memberRepository.save(new Member("roro", "1234@email.com", "1234", Role.USER));
        this.sudal = memberRepository.save(new Member("sudal", "5678@email.com", "1234", Role.USER));
    }

    @Test
    @DisplayName("사용자를 통해 대기 순위를 찾을 수 있다.")
    void findByMember() {
        waitingRepository.save(new Waiting(sudal, tomorrow, reservationTime, theme));
        waitingRepository.save(new Waiting(roro, tomorrow, reservationTime, theme));

        WaitingWithRank sudalWaitingWithRank = waitingRepository.findByMember(sudal);
        WaitingWithRank roroWaitingWithRank = waitingRepository.findByMember(roro);

        assertThat(sudalWaitingWithRank.getRank()).isEqualTo(0);
        assertThat(roroWaitingWithRank.getRank()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void deleteByMemberAndDateAndTimeAndTheme() {
        waitingRepository.save(new Waiting(sudal, tomorrow, reservationTime, theme));
        waitingRepository.save(new Waiting(roro, tomorrow, reservationTime, theme));

        waitingRepository.deleteByMemberAndDateAndTimeAndTheme(sudal, tomorrow, reservationTime, theme);
        List<Waiting> waitings = waitingRepository.findAll();

        assertThat(waitings).hasSize(1);
    }

    @Test
    @DisplayName("가장 빠른 예약 대기 순서를 찾을 수 있다.")
    void findFistByDateAndTimeAndThemeOrderByIdAsc() {
        waitingRepository.save(new Waiting(sudal, tomorrow, reservationTime, theme));
        waitingRepository.save(new Waiting(roro, tomorrow, reservationTime, theme));

        Waiting waiting = waitingRepository.findFistByDateAndTimeAndThemeOrderByIdAsc(tomorrow, reservationTime, theme).get();

        assertThat(waiting.getMember().getName()).isEqualTo("sudal");
    }
}
