package roomescape.reservationslot.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.TestFixture.FUTURE_DATE;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationSlotRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    private ReservationTime reservationTime;

    private Theme theme;

    @BeforeEach
    public void setup() {
        member = memberRepository.save(TestFixture.makeMember());
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme());
        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE, reservationTime, member, theme));
    }

    @Test
    void existsByTimeId() {
        boolean existsByTimeId = reservationSlotRepository.existsByTimeId(reservationTime.getId());

        assertThat(existsByTimeId).isTrue();
    }

    @Test
    void existsByThemeId() {
        boolean existsByThemeId = reservationSlotRepository.existsByThemeId(theme.getId());

        assertThat(existsByThemeId).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId() {
        boolean existsByDateAndTimeIdAndThemeId = reservationSlotRepository.existsByDateAndTimeIdAndThemeId(FUTURE_DATE,
                reservationTime.getId(),
                theme.getId());

        assertThat(existsByDateAndTimeIdAndThemeId).isTrue();
    }

    @Test
    void findAll() {
        // Given

        // When
        List<ReservationSlot> reservationSlots = reservationSlotRepository.findAll();

        // Then
        assertThat(reservationSlots.size()).isOne();
    }
}
