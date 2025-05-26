package roomescape.reservationslot.infrastructure;

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
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
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
        reservationTime = reservationTimeRepository.save(ReservationTime.withUnassignedId(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme());
        reservationSlotRepository.save(
                ReservationSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime, theme, NOW_DATETIME));
    }

    @Test
    void findByThemeIdAndDateBetweenAndReservationMemberId() {
        Theme theme2 = themeRepository.save(Theme.of("논리", "셜록 논리 게임 with Vector", "image.png"));

        ReservationTime reservationTime2 = ReservationTime.withUnassignedId(LocalTime.of(11, 0));
        reservationTime2 = reservationTimeRepository.save(reservationTime2);

        ReservationSlot reservationSlot2 = ReservationSlot.createUpcomingReservation(member, FUTURE_DATE,
                reservationTime2, theme2,
                NOW_DATETIME);
        reservationSlotRepository.save(reservationSlot2);

        List<ReservationSlot> filteredReservationSlots = reservationSlotRepository.findByThemeIdAndDateBetweenAndReservationMemberId(
                theme.getId(),
                FUTURE_DATE, FUTURE_DATE.plusDays(1), member.getId()
        );

        assertThat(filteredReservationSlots.size()).isEqualTo(1);
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
    void findAvailableTimesByDateAndThemeId() {
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                ReservationTime.withUnassignedId(LocalTime.of(11, 0)));
        ReservationTime reservationTime3 = reservationTimeRepository.save(
                ReservationTime.withUnassignedId(LocalTime.of(12, 0)));

        reservationSlotRepository.save(
                ReservationSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime2, theme, NOW_DATETIME));
        reservationSlotRepository.save(
                ReservationSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime3, theme, NOW_DATETIME));

        List<AvailableReservationTimeResponse> bookedTimesByDateAndThemeId = reservationSlotRepository.findBookedTimesByDateAndThemeId(
                FUTURE_DATE, theme.getId());

        assertThat(bookedTimesByDateAndThemeId.size()).isEqualTo(3);
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
