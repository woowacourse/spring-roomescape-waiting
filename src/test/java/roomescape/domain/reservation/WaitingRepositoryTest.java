package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 사용자의_예약_대기_순서를_모두_조회할_수_있다() {
        //given
        Theme theme = themeRepository.save(Theme.create("테마", "description", "image"));
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(13, 0)));
        Waiting waiting1 = createWaiting(
                LocalDateTime.of(2025, 11, 22, 12, 22),
                reservationTime,
                theme,
                createMember("name1", new Email("email@email.com")));
        Waiting waiting2 = createWaiting(
                LocalDateTime.of(2025, 11, 23, 12, 22),
                reservationTime,
                theme,
                createMember("name2", new Email("email2@email.com")));
        Member member = createMember("name3", new Email("email3@email.com"));
        Waiting waiting3 = createWaiting(
                LocalDateTime.of(2025, 11, 24, 12, 22),
                reservationTime,
                theme,
                member);

        //when
        List<WaitingRank> waitingRanks = waitingRepository.findWaitingRankByMember(member);

        //then
        assertThat(waitingRanks)
                .isEqualTo(List.of(new WaitingRank(waiting3, 3)));
    }

    private Waiting createWaiting(LocalDateTime startedAt, ReservationTime reservationTime, Theme theme,
                                  Member member) {
        Waiting waiting = Waiting.create(
                startedAt,
                new ReservationSlot(
                        LocalDate.of(2025, 12, 25),
                        reservationTime,
                        theme
                ),
                member);
        return waitingRepository.save(waiting);
    }

    private Member createMember(String name, Email email) {
        return memberRepository.save(Member.create(name, email, "password", Role.NORMAL));
    }
}
