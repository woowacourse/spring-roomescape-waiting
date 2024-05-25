package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.BaseRepositoryTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private Member member;

    private Theme theme;

    private ReservationTime time;

    @BeforeEach
    void setUp() {
        member = save(MemberFixture.user());
        theme = save(ThemeFixture.theme());
        time = save(ReservationTimeFixture.ten());
    }

    @Test
    @DisplayName("조건에 해당하는 모든 예약들을 조회한다.")
    void findAll() {
        save(ReservationFixture.create("2024-04-09", member, time, theme));
        save(ReservationFixture.create("2024-04-10", member, time, theme));
        save(ReservationFixture.create("2024-04-11", member, time, theme));

        List<Reservation> reservations = reservationRepository.findAllByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 4, 9),
                LocalDate.of(2024, 4, 10)
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(2);
            softly.assertThat(reservations.get(0).getDate()).isEqualTo("2024-04-09");
            softly.assertThat(reservations.get(1).getDate()).isEqualTo("2024-04-10");
        });
    }

    @Test
    @DisplayName("예약 시간을 사용하는 예약이 존재하는지 확인한다.")
    void existsByTime() {
        save(ReservationFixture.create(member, time, theme));

        assertThat(reservationRepository.existsByTime(time)).isTrue();
    }

    @Test
    @DisplayName("테마를 사용하는 예약이 존재하는지 확인한다.")
    void existsByTheme() {
        save(ReservationFixture.create(member, time, theme));

        assertThat(reservationRepository.existsByTheme(theme)).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간, 테마로 예약을 조회한다.")
    void findByDateAndTimeAndTheme() {
        LocalDate date = LocalDate.parse("2024-04-09");
        save(ReservationFixture.create(date, member, time, theme));

        Reservation reservation = reservationRepository.findByDateAndTimeAndTheme(date, time, theme).orElseThrow();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getDate()).isEqualTo("2024-04-09");
            softly.assertThat(reservation.getMember()).isEqualTo(member);
            softly.assertThat(reservation.getTime()).isEqualTo(time);
            softly.assertThat(reservation.getTheme()).isEqualTo(theme);
        });
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 다르면 조회된 예약이 없다. [다른 날짜]")
    void findByOtherDate() {
        save(ReservationFixture.create("2024-04-09", member, time, theme));

        LocalDate otherDate = LocalDate.parse("2024-04-10");
        assertThat(reservationRepository.findByDateAndTimeAndTheme(otherDate, time, theme)).isEmpty();
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 다르면 조회된 예약이 없다. [다른 시간]")
    void findByOtherTime() {
        LocalDate date = LocalDate.parse("2024-04-09");
        save(ReservationFixture.create(date, member, time, theme));

        ReservationTime otherTime = save(ReservationTimeFixture.create("11:00"));
        assertThat(reservationRepository.findByDateAndTimeAndTheme(date, otherTime, theme)).isEmpty();
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 다르면 조회된 예약이 없다. [다른 테마]")
    void findByOtherTheme() {
        LocalDate date = LocalDate.parse("2024-04-09");
        save(ReservationFixture.create(date, member, time, theme));

        Theme otherTheme = save(ThemeFixture.create("다른 테마"));
        assertThat(reservationRepository.findByDateAndTimeAndTheme(date, time, otherTheme)).isEmpty();
    }

    @Test
    @DisplayName("멤버의 모든 예약을 조회한다.")
    void findAllByMember() {
        save(ReservationFixture.create("2024-04-09", member, time, theme));
        save(ReservationFixture.create("2024-04-10", member, time, theme));
        save(ReservationFixture.create("2024-04-11", member, time, theme));

        List<Reservation> reservations = reservationRepository.findAllByMember(member);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(3);
            softly.assertThat(reservations.get(0).getMember()).isEqualTo(member);
            softly.assertThat(reservations.get(0).getDate()).isEqualTo("2024-04-09");
            softly.assertThat(reservations.get(1).getMember()).isEqualTo(member);
            softly.assertThat(reservations.get(1).getDate()).isEqualTo("2024-04-10");
            softly.assertThat(reservations.get(2).getMember()).isEqualTo(member);
            softly.assertThat(reservations.get(2).getDate()).isEqualTo("2024-04-11");
        });
    }
}
