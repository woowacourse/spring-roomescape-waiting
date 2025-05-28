package roomescape.reservationtime.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.TestFixture.FUTURE_DATE;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class JpaReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void existsByStartAt() {
        ReservationTime reservationTime = TestFixture.makeReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime);

        boolean existsByStartAt = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());
        assertThat(existsByStartAt).isTrue();
    }

    @Test
    void findAvailableTimesByDateAndThemeId() {
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Member member = memberRepository.save(TestFixture.makeMember());
        Theme theme = themeRepository.save(TestFixture.makeTheme());

        reservationSlotRepository.save(
                TestFixture.makeConfirmedReservation(FUTURE_DATE, reservationTime2, member, theme));

        List<AvailableReservationTimeWebResponse> availableReservationTimes = reservationTimeRepository.findAvailable(
                FUTURE_DATE, theme.getId());

        long count = availableReservationTimes.stream()
                .filter(AvailableReservationTimeWebResponse::alreadyBooked)
                .count();
        assertThat(count).isEqualTo(1);
    }
}
