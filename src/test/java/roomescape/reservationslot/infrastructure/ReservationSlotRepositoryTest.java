package roomescape.reservationslot.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.TestFixture.FUTURE_DATE;

import java.time.LocalTime;
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

    private ReservationTime reservationTime;

    private Theme theme;

    private ReservationSlot reservationSlot;

    @BeforeEach
    public void setup() {
        Member member = memberRepository.save(TestFixture.makeMember());
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme());
        reservationSlot = reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE, reservationTime, member, theme));
    }

    @Test
    void hasSingleReservation_whenValidRequest_returnsTrue() {
        assertThat(reservationSlotRepository.hasSingleReservation(reservationSlot.getId())).isTrue();
    }
}
