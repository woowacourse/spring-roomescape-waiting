package roomescape.reservation.infrastructure;

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
import roomescape.reservation.domain.Reservation;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member member;

    private ReservationTime reservationTime;

    private Theme theme;

    @BeforeEach
    public void setup() {
        member = memberRepository.save(TestFixture.makeMember());
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme());

        ReservationSlot reservationSlot = new ReservationSlot(FUTURE_DATE, reservationTime, theme);
        reservationSlot.addReservation(member, NOW_DATETIME);
        reservationSlotRepository.save(reservationSlot);
    }

    @Test
    void findByReservationMemberId() {
        // Given
        Long memberId = member.getId();

        // When
        List<Reservation> reservations = reservationRepository.findByReservationMemberId(memberId);

        // Then
        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    void findByThemeIdAndDateBetweenAndReservationMemberId() {
        Theme theme2 = themeRepository.save(new Theme("논리", "셜록 논리 게임 with Vector", "image.png"));

        ReservationTime reservationTime2 = new ReservationTime(LocalTime.of(11, 0));
        reservationTime2 = reservationTimeRepository.save(reservationTime2);

        ReservationSlot reservationSlot2 = TestFixture.makeConfirmedReservation(FUTURE_DATE, reservationTime2, member,
                theme2);
        reservationSlotRepository.save(reservationSlot2);

        List<Reservation> filteredReservations = reservationRepository.findByThemeIdAndDateBetweenAndReservationMemberId(
                theme.getId(),
                FUTURE_DATE, FUTURE_DATE.plusDays(1), member.getId()
        );

        assertThat(filteredReservations.size()).isEqualTo(1);
    }
}
