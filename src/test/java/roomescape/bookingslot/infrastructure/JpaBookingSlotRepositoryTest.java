package roomescape.bookingslot.infrastructure;

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
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@DataJpaTest
@Import(TestConfig.class)
class JpaBookingSlotRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private BookingSlotRepository bookingSlotRepository;

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
        bookingSlotRepository.save(
                BookingSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime, theme, NOW_DATETIME));
    }

    @Test
    void findByThemeIdAndDateBetweenAndWaitingsMemberId() {
        Theme theme2 = themeRepository.save(Theme.of("논리", "셜록 논리 게임 with Vector", "image.png"));

        ReservationTime reservationTime2 = ReservationTime.withUnassignedId(LocalTime.of(11, 0));
        reservationTime2 = reservationTimeRepository.save(reservationTime2);

        BookingSlot bookingSlot2 = BookingSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime2, theme2,
                NOW_DATETIME);
        bookingSlotRepository.save(bookingSlot2);

        List<BookingSlot> filteredBookingSlots = bookingSlotRepository.findByThemeIdAndDateBetweenAndWaitingMemberId(
                theme.getId(),
                FUTURE_DATE, FUTURE_DATE.plusDays(1), member.getId()
        );

        assertThat(filteredBookingSlots.size()).isEqualTo(1);
    }

    @Test
    void existsByTimeId() {
        boolean existsByTimeId = bookingSlotRepository.existsByTimeId(reservationTime.getId());

        assertThat(existsByTimeId).isTrue();
    }

    @Test
    void existsByThemeId() {
        boolean existsByThemeId = bookingSlotRepository.existsByThemeId(theme.getId());

        assertThat(existsByThemeId).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId() {
        boolean existsByDateAndTimeIdAndThemeId = bookingSlotRepository.existsByDateAndTimeIdAndThemeId(FUTURE_DATE,
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

        bookingSlotRepository.save(
                BookingSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime2, theme, NOW_DATETIME));
        bookingSlotRepository.save(
                BookingSlot.createUpcomingReservation(member, FUTURE_DATE, reservationTime3, theme, NOW_DATETIME));

        List<AvailableReservationTimeResponse> bookedTimesByDateAndThemeId = bookingSlotRepository.findBookedTimesByDateAndThemeId(
                FUTURE_DATE, theme.getId());

        assertThat(bookedTimesByDateAndThemeId.size()).isEqualTo(3);
    }

    @Test
    void findAll() {
        // Given

        // When
        List<BookingSlot> bookingSlots = bookingSlotRepository.findAll();

        // Then
        assertThat(bookingSlots.size()).isOne();
    }
}
