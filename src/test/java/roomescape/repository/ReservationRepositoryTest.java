package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.fixture.MemberFixtures;
import roomescape.fixture.ReservationFixtures;
import roomescape.fixture.ThemeFixtures;
import roomescape.fixture.TimeSlotFixtures;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("회원과 테마 정보가 일치하고 두 기간 사이의 예약을 모두 조회한다.")
    @Sql("classpath:test-data.sql")
    @Test
    void findAllByMemberAndThemeAndDateBetween() {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusWeeks(1);
        Member member = memberRepository.getMemberById(2L);
        Theme theme = themeRepository.getThemeById(5L);

        List<Reservation> results =
                reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, lastWeek, today);

        assertThat(results).hasSize(3);
    }

    @DisplayName("회원과 테마 정보가 일치하고 두 기간 사이의 예약을 모두 조회한다.")
    @Sql("classpath:test-data.sql")
    @Test
    void findAllByDateAndTheme() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Theme theme = themeRepository.getThemeById(5L);

        List<Reservation> results =
                reservationRepository.findAllByDateAndTheme(yesterday, theme);

        assertThat(results).hasSize(9);
    }

    @DisplayName("예약내역과 순위를 반환한다.")
    @Sql("classpath:test-data.sql")
    @Test
    void findReservationRanksWithMember() {
        Member member = memberRepository.getMemberById(2L);

        List<ReservationRank> reservationRanksWithMember =
                reservationRepository.findReservationRanksWithMember(member);

        List<Long> pendingRanks = reservationRanksWithMember.stream()
                .filter(reservationRank -> reservationRank.reservation().getStatus().isPending())
                .map(ReservationRank::rank)
                .toList();

        assertAll(
                () -> assertThat(reservationRanksWithMember).hasSize(9),
                () -> assertThat(pendingRanks).hasSize(3)
        );
    }

    @DisplayName("예약 대기 중인 예약들을 조회한다.")
    @Sql("classpath:test-data.sql")
    @Test
    void findAllPendingOrderByDateAscTime() {
        List<ReservationRank> reservationRanks = reservationRepository.findAllPendingOrderByDateAscTime();

        assertThat(reservationRanks).hasSize(10);
    }

    @DisplayName("날짜, 시간, 테마, 예약 상태가 동일한 예약을 조회힌다.")
    @Sql("classpath:test-data.sql")
    @Test
    void findFirstByDateAndTimeAndThemeAndStatusOrderById() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(1L);
        Theme theme = themeRepository.getThemeById(5L);

        Optional<Reservation> result = reservationRepository.findFirstByDateAndTimeAndThemeAndStatusOrderById(
                yesterday,
                timeSlot,
                theme,
                ReservationStatus.BOOKING
        );

        assertThat(result).isPresent();
    }

    @DisplayName("테마로 예약이 존재하면 참이다.")
    @Test
    void existsByTheme() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Reservation bookingReservation = ReservationFixtures.createBookingReservation(daon, now, time, theme);
        memberRepository.save(daon);
        timeSlotRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(bookingReservation);

        boolean result = reservationRepository.existsByTheme(theme);

        assertThat(result).isTrue();
    }

    @DisplayName("테마로 예약이 존재하지 않으면 거짓이다.")
    @Test
    void existsByThemeNotExist() {
        Theme theme = ThemeFixtures.createDefaultTheme();
        themeRepository.save(theme);

        boolean result = reservationRepository.existsByTheme(theme);

        assertThat(result).isFalse();
    }

    @DisplayName("시간으로 예약이 존재하면 참이다.")
    @Test
    void existsByTime() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Reservation bookingReservation = ReservationFixtures.createBookingReservation(daon, now, time, theme);
        memberRepository.save(daon);
        timeSlotRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(bookingReservation);

        boolean result = reservationRepository.existsByTime(time);

        assertThat(result).isTrue();
    }

    @DisplayName("시간으로 예약이 존재하지 않으면 거짓이다.")
    @Test
    void existsByTimeNotExist() {
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        timeSlotRepository.save(time);

        boolean result = reservationRepository.existsByTime(time);

        assertThat(result).isFalse();
    }

    @DisplayName("회원, 날짜, 시간, 테마로 예약이 존재하면 참이다.")
    @Test
    void existsByMemberAndDateAndTimeAndTheme() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Reservation bookingReservation = ReservationFixtures.createBookingReservation(daon, now, time, theme);
        Member savedMember = memberRepository.save(daon);
        TimeSlot savedTime = timeSlotRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);
        reservationRepository.save(bookingReservation);

        boolean result = reservationRepository.existsByMemberAndDateAndTimeAndTheme(savedMember, now, savedTime, savedTheme);

        assertThat(result).isTrue();
    }

    @DisplayName("회원, 날짜, 시간, 테마로 예약이 존재하지 않으면 거짓이다.")
    @Test
    void existsByMemberAndDateAndTimeAndThemeNotExist() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Member savedMember = memberRepository.save(daon);
        TimeSlot savedTime = timeSlotRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        boolean result = reservationRepository.existsByMemberAndDateAndTimeAndTheme(savedMember, now, savedTime, savedTheme);

        assertThat(result).isFalse();
    }

    @DisplayName("날짜, 시간, 테마로 예약이 존재하면 참이다.")
    @Test
    void existsByDateAndTimeAndTheme() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Reservation bookingReservation = ReservationFixtures.createBookingReservation(daon, now, time, theme);
        memberRepository.save(daon);
        TimeSlot savedTime = timeSlotRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);
        reservationRepository.save(bookingReservation);

        boolean result = reservationRepository.existsByDateAndTimeAndTheme(now, savedTime, savedTheme);

        assertThat(result).isTrue();
    }

    @DisplayName("날짜, 시간, 테마로 예약이 존재하지 않으면 거짓이다.")
    @Test
    void existsByDateAndTimeAndThemeNotExist() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        memberRepository.save(daon);
        TimeSlot savedTime = timeSlotRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        boolean result = reservationRepository.existsByDateAndTimeAndTheme(now, savedTime, savedTheme);

        assertThat(result).isFalse();
    }

    @DisplayName("id로 예약 정보를 조회한다.")
    @Test
    void getReservationById() {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("test@email.com");
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Theme theme = ThemeFixtures.createDefaultTheme();
        Reservation bookingReservation = ReservationFixtures.createBookingReservation(daon, now, time, theme);
        memberRepository.save(daon);
        timeSlotRepository.save(time);
        themeRepository.save(theme);
        Reservation savedReservation = reservationRepository.save(bookingReservation);

        Reservation reservation = reservationRepository.getReservationBy(savedReservation.getId());

        assertThat(reservation.getDate()).isEqualTo(now);
    }

    @DisplayName("id가 존재하지 않는다면 예외가 발생한다.")
    @Test
    void getReservationByIdWhenNotExist() {
        assertThatThrownBy(() -> reservationRepository.getReservationBy(1L))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 예약 입니다");
    }
}
