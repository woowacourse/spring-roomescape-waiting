package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.unit.fake.FakeMemberRepository;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;
import roomescape.unit.fake.FakeThemeRepository;
import roomescape.unit.fake.FakeWaitingRepository;

public class WaitingServiceTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private WaitingRepository waitingRepository;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository(reservationRepository);
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        waitingRepository = new FakeWaitingRepository();
        waitingService = new WaitingService(reservationTimeRepository, themeRepository, memberRepository,
                waitingRepository);
    }

    @Test
    void 예약대기를_추가할_수_있다() {
        // given
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Waiting waiting = Waiting.of(null, member1, LocalDate.of(2025, 7, 25),
                reservationTime1, theme1);
        // when
        waitingRepository.save(waiting);

        // then
        List<WaitingResponse> all = waitingService.findAll();
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getLast().memberName()).isEqualTo("name1");
    }
}
