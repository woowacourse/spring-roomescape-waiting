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
import roomescape.domain.reservation.Reservation;

@Sql("/all-test-data.sql")
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
        //when
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //then
        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void 모든_예약_조회() {
        //when
        List<Reservation> allReservations = reservationRepository.findAll();

        //then
        assertThat(allReservations).hasSize(4);
    }

    @Test
    void 주어진_멤버로_예약된_모든_예약_조회() {
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

    @Sql("/waiting-test-data.sql")
    @Test
    void 주어진_예약의_멤버정보를_제외하고_동일한_조건에_해당하는_모든_예약_조회() {
        //given
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();

        //when
        List<Reservation> reservationsBySameConditions = reservationRepository.findByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStatus()
        );

        //then
        assertThat(reservationsBySameConditions)
                .isNotNull()
                .isNotEmpty()
                .allMatch(res -> res.getDate().equals(reservation.getDate()) &&
                        res.getTime().getId().equals(reservation.getTime().getId()) &&
                        res.getTheme().getId().equals(reservation.getTheme().getId()) &&
                        res.getStatus().equals(reservation.getStatus()));
    }

    @Sql("/reservation-filter-api-test-data.sql")
    @Test
    void 주어진_멤버_또는_테마_또는_기간에_해당하는_모든_예약_조회시_멤버와_테마_제공() {
        // given, when
        List<Reservation> findByMemberAndTheme = reservationRepository.findByMemberOrThemeOrDateRange(1L, 1L,
                null, null);

        //then
        assertThat(findByMemberAndTheme).isNotEmpty()
                .allMatch(r -> r.getMember().getId().equals(1L) && r.getTheme().getId().equals(1L));
    }

    @Sql("/popular-theme-test-data.sql")
    @Test
    void 주어진_기간_동안_가장_예약이_많은_테마를_10개까지_조회() {
        //given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);

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

    @Sql("/waiting-test-data.sql")
    @Test
    void 주어진_예약의_멤버정보를_제외하고_동일한_조건에_해당하는_예약의_수를_확인_조회() {
        //given
        Reservation reservation = reservationRepository.findById(2L).orElseThrow();

        //when
        int result = reservationRepository.countByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getStatus()
        );

        //then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void 주어진_시간에_예약이_있는지_확인() {
        //when
        boolean result = reservationRepository.existsByTimeId(1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_테마와_일치하는_예약이_있는지_확인() {
        //when
        boolean result = reservationRepository.existsByThemeId(1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_날짜_시간_테마와_일치하는_예약이_있는지_확인() {
        //when
        boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(
                LocalDate.now().plusDays(1), 1L, 1L);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 주어진_날짜_시간_테마_멤버와_일치하는_예약이_있는지_확인() {
        //when
        boolean result = reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                LocalDate.now().plusDays(1), 1L, 1L, 1L);

        //then
        assertThat(result).isTrue();
    }
}
