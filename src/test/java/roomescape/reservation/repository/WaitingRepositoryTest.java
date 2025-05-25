package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import roomescape.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationInfo;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class WaitingRepositoryTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Member member;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(TestFixture.makeMember());
        time = reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme(1L));

        Waiting w1 = new Waiting(member, new ReservationInfo(futureDate, time, theme),1);
        waitingRepository.save(w1);
    }

    @Test
    void existsByDateAndTimeIdAndThemeId() {
        boolean exists = waitingRepository.existsByDateAndTimeIdAndThemeId(
            futureDate, time.getId(), theme.getId()
        );
        assertThat(exists).isTrue();
    }

    @Test
    void findMaxOrderByDateAndTimeAndTheme() {
        int initialMax = waitingRepository.findMaxOrderByDateAndTimeAndTheme(
            futureDate, time.getId(), theme.getId()
        );
        assertThat(initialMax).isEqualTo(1);

        Waiting w2 = waitingRepository.save(
            new Waiting(member, new ReservationInfo(futureDate, time, theme),2)
        );

        int updatedMax = waitingRepository.findMaxOrderByDateAndTimeAndTheme(
            futureDate, time.getId(), theme.getId()
        );
        assertThat(updatedMax).isEqualTo(2);
    }

    @Test
    void findWaitingsWithRankByMemberId() {
        Waiting w2 = waitingRepository.save(
            new Waiting(member, new ReservationInfo(futureDate, time, theme),2)
        );

        List<WaitingWithRank> ranks = waitingRepository.findWaitingsWithRankByMemberId(member.getId());
        assertThat(ranks).hasSize(2);

        assertThat(ranks)
            .extracting(WaitingWithRank::getRank)
            .containsExactly(1L, 2L);
    }

    @Test
    void findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc() {
        Waiting w2 = waitingRepository.save(
            new Waiting(member, new ReservationInfo(futureDate, time, theme),2)
        );

        Optional<Waiting> first = waitingRepository
            .findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(
                futureDate, time, theme
            );

        assertThat(first).isPresent();
        assertThat(first.get().getTurn()).isEqualTo(1);
    }
}
