package roomescape.waiting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.TestFixture.FUTURE_DATE;
import static roomescape.fixture.TestFixture.NOW_DATETIME;

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
import roomescape.member.domain.repository.MemberRepository;
import roomescape.bookingslot.domain.BookingSlot;
import roomescape.bookingslot.domain.repository.BookingSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@DataJpaTest
@Import(TestConfig.class)
class JpaWaitingRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private BookingSlotRepository bookingSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    private Member member;

    private ReservationTime reservationTime;

    private Theme theme;

    @BeforeEach
    public void setup() {
        member = memberRepository.save(TestFixture.makeMember());
        reservationTime = reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme());
        bookingSlotRepository.save(
                BookingSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime, theme, NOW_DATETIME));
    }

    @Test
    void findByWaitingsMemberId() {
        // Given
        Long memberId = member.getId();

        // When
        List<Waiting> waitings = waitingRepository.findByWaitingMemberId(memberId);

        // Then
        assertThat(waitings.size()).isEqualTo(1);
    }
}
