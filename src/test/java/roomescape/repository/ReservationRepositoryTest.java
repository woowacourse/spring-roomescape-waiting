package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@Sql("/member-theme-time-test-data.sql")
@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationRepositoryTest {

    @Autowired
    ReservationTimeRepository timeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void 주어진_id에_해당하는_예약_조회() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = createReservation(date, 1L, 1L, 1L);
        Reservation savedReservation = reservationRepository.save(reservation);

        //when
        Reservation findReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();

        //then
        assertThat(findReservation.getId()).isEqualTo(savedReservation.getId());
    }

    @Test
    void 모든_예약_조회() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation1 = createReservation(date, 1L, 1L, 1L);
        Reservation reservation2 = createReservation(date, 2L, 1L, 1L);

        Reservation savedReservation1 = reservationRepository.save(reservation1);
        Reservation savedReservation2 = reservationRepository.save(reservation2);

        //when
        List<Reservation> allReservations = reservationRepository.findAll();

        //then
        assertThat(allReservations).extracting(Reservation::getId)
                .containsOnly(savedReservation1.getId(), savedReservation2.getId());
    }

    @Test
    void 주어진_멤버로_예약된_모든_예약_조회() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation1 = createReservation(date, 1L, 1L, 1L);
        Reservation reservation2 = createReservation(date, 2L, 1L, 1L);
        Reservation reservation3 = createReservation(date, 2L, 1L, 2L);

        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        //when
        List<Reservation> reservations = reservationRepository.findByMemberId(1L);

        //then
        assertThat(reservations).isNotEmpty()
                .allMatch(r -> r.getMember().getId().equals(1L));
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 주어진_멤버_또는_테마_또는_기간에_해당하는_모든_예약_조회시_모든_항목_제공() {
        //given
        LocalDate startDate = LocalDate.of(2024, 5, 1);
        LocalDate endDate = LocalDate.of(2024, 5, 2);

        //when
        List<Reservation> findByAllFilter = reservationRepository.findByMemberOrThemeOrDateRange(1L, 1L,
                startDate, endDate);

        //then
        assertThat(findByAllFilter).isNotEmpty()
                .allMatch(r -> r.getMember().getId().equals(1L) &&
                        r.getTheme().getId().equals(1L) &&
                        (!r.getDate().isBefore(startDate) && !r.getDate().isAfter(endDate))
                );
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 주어진_멤버_또는_테마_또는_기간에_해당하는_모든_예약_조회시_멤버와_테마_제공() {
        // given, when
        List<Reservation> findByMemberAndTheme  = reservationRepository.findByMemberOrThemeOrDateRange(1L, 1L,
                null, null);

        //then
        assertThat(findByMemberAndTheme).isNotEmpty()
                .allMatch(r -> r.getMember().getId().equals(1L) && r.getTheme().getId().equals(1L));
    }

    @Sql("/popular-theme-test-data.sql")
    @Test
    void 주어진_기간_동안_가장_예약이_많은_테마를_10개까지_조회() {
        //given
        LocalDate startDate =LocalDate.now().minusDays(7);
        LocalDate endDate =LocalDate.now().minusDays(1);

        //when
        List<Long> topThemeIds = reservationRepository.findTopThemeIdsByReservationCountsForDate(
                startDate, endDate);

        //then
        assertAll(
                () -> assertThat(topThemeIds).hasSize(10),
                () -> assertThat(topThemeIds.get(0)).isEqualTo(1L),
                () -> assertThat(topThemeIds.get(1)).isEqualTo(2L),
                () -> assertThat(topThemeIds).doesNotContain(12L)
        );
    }

    @Test
    void 주어진_시간에_예약이_있는지_확인() {
        //given
        Reservation reservation = createReservation(LocalDate.now(), 1L, 1L, 1L);
        reservationRepository.save(reservation);

        //when
        boolean result = reservationRepository.existsByTimeId(1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_테마와_일치하는_예약이_있는지_확인() {
        Reservation reservation = createReservation(LocalDate.now(), 1L, 1L, 1L);
        reservationRepository.save(reservation);

        //when
        boolean result = reservationRepository.existsByThemeId(1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_날짜_시간_테마와_일치하는_예약이_있는지_확인() {
        Reservation reservation = createReservation(LocalDate.now(), 1L, 1L, 1L);
        reservationRepository.save(reservation);

        //when
        boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.now(), 1L, 1L);

        //then
        assertThat(result).isTrue();
    }

    private Reservation createReservation(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationTime reservationTime = timeRepository.findById(timeId).orElseThrow();
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow();

        return new Reservation(date, reservationTime, theme, member);
    }
}
