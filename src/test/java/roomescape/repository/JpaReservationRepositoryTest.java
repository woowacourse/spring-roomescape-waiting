package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
@Transactional
class JpaReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void init() {
        member = memberRepository.save(DEFAULT_MEMBER);
        time = reservationTimeRepository.save(DEFAULT_TIME);
        theme = themeRepository.save(DEFAULT_THEME);
    }

    @Test
    @DisplayName("Reservation 을 잘 저장하는지 확인한다.")
    void save() {
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());
        List<Reservation> afterSave = reservationRepository.findAll();

        assertThat(afterSave)
                .extracting(Reservation::getId)
                .contains(reservation.getId());
    }

    @Test
    @DisplayName("Reservation 을 잘 조회하는지 확인한다.")
    void findAll() {
        List<Reservation> beforeSave = reservationRepository.findAll();
        reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());
        List<Reservation> afterSave = reservationRepository.findAll();

        assertThat(afterSave.size()).isEqualTo(beforeSave.size() + 1);
    }

    @Test
    @DisplayName("Reservation 을 잘 지우는지 확인한다.")
    void delete() {
        List<Reservation> beforeSaveAndDelete = reservationRepository.findAll();
        Reservation saved = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());

        reservationRepository.deleteById(saved.getId());

        List<Reservation> afterSaveAndDelete = reservationRepository.findAll();

        assertThat(beforeSaveAndDelete).containsExactlyElementsOf(afterSaveAndDelete);
    }

    @Test
    @DisplayName("특정 테마에 특정 날짜 특정 시간에 예약 여부를 잘 반환하는지 확인한다.")
    void existsByThemeAndDateAndTime() {
        LocalDate reservedDate = DEFAULT_RESERVATION.getDate();
        LocalDate notReservedDate = reservedDate.plusDays(1);
        reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(reservedDate)
                        .time(time)
                        .theme(theme)
                        .build()
        );

        assertAll(
                () -> assertThat(
                        reservationRepository.existsByThemeAndDateAndTime(theme, reservedDate, time)).isTrue(),
                () -> assertThat(
                        reservationRepository.existsByThemeAndDateAndTime(theme, notReservedDate, time)).isFalse()
        );
    }

    @Test
    @DisplayName("특정 시간에 예약이 있는지 확인한다.")
    void existsByTime() {
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());
        ReservationTime randomTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 56)));

        assertAll(
                () -> assertThat(reservationRepository.existsByTime(reservation.getReservationTime())).isTrue(),
                () -> assertThat(reservationRepository.existsByTime(randomTime)).isFalse()
        );
    }

    @Test
    @DisplayName("특정 테마에 예약이 있는지 확인한다.")
    void existsByTheme() {
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());
        Theme randomTheme = new Theme(2L, DEFAULT_THEME);

        assertAll(
                () -> assertThat(reservationRepository.existsByTheme(reservation.getTheme())).isTrue(),
                () -> assertThat(reservationRepository.existsByTheme(randomTheme)).isFalse()
        );
    }

    @Test
    @DisplayName("특정 회원의 예약을 잘 조회하는지 확인한다.")
    void findByMemberId() {
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.now().plusDays(1))
                        .time(time)
                        .theme(theme)
                        .build());

        List<Reservation> reservations = reservationRepository.findAllByMemberId(member.getId());

        assertThat(reservations)
                .extracting(Reservation::getId)
                .containsExactly(reservation.getId());
    }

    @Test
    @DisplayName("특정 회원의 특정 기간 내의 예약을 잘 조회하는지 확인한다.")
    void findByMemberAndThemeBetweenDates() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);

        LocalDate onPeriodDate = startDate;
        LocalDate notOnPeriodDate = startDate.plusDays(2);

        Reservation onPeriodreservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(onPeriodDate)
                        .time(time)
                        .theme(theme)
                        .build());
        Reservation notOnPeriodreservation = reservationRepository.save(
                Reservation.builder()
                        .member(member)
                        .date(notOnPeriodDate)
                        .time(time)
                        .theme(theme)
                        .build());

        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member.getId(), theme.getId(), startDate, endDate);

        assertThat(reservations)
                .extracting(Reservation::getId)
                .containsExactly(onPeriodreservation.getId());
    }
}
