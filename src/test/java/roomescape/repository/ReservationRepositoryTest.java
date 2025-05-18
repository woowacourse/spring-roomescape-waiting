package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("해당하는 날짜와 시간의 예약이 있다면 반환한다.")
    @Test
    void findByDateAndReservationTime() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        Optional<Reservation> actual = reservationRepository.findByDateAndReservationTime(date, reservationTime);

        //then
        assertThat(actual.get()).isEqualTo(reservation);
    }

    @DisplayName("해당하는 날짜와 시간의 예약이 없다면 빈 값을 반환한다.")
    @Test
    void emptyFindByDateAndReservationTime() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        LocalDate otherDate = date.plusDays(1);
        Optional<Reservation> actual = reservationRepository.findByDateAndReservationTime(otherDate, reservationTime);

        //then
        assertThat(actual).isEmpty();
    }

    @DisplayName("memberId에 해당하는 예약이 있다면 반환한다.")
    @Test
    void findByMemberId() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation1 = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation1);

        Reservation reservation2 = new Reservation(
                date.plusDays(1),
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation2);

        //when
        List<Reservation> actual = reservationRepository.findByMemberId(member.getId());

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getMember()).isEqualTo(member);
        assertThat(actual.get(1).getMember()).isEqualTo(member);
    }

    @DisplayName("해당하는 테마와 날짜의 예약이 존재하면 반환한다.")
    @Test
    void findByThemeIdAndDate() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime1 = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime1);

        ReservationTime reservationTime2 = new ReservationTime(LocalTime.of(22, 30));
        reservationTimeRepository.save(reservationTime2);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation1 = new Reservation(
                date,
                reservationTime1,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation1);

        Reservation reservation2 = new Reservation(
                date,
                reservationTime2,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation2);

        //when
        List<Reservation> actual = reservationRepository.findByThemeIdAndDate(theme.getId(), date);

        //then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).getTheme()).isEqualTo(theme);
        assertThat(actual.get(0).getReservationTime()).isEqualTo(reservationTime1);
        assertThat(actual.get(1).getTheme()).isEqualTo(theme);
        assertThat(actual.get(1).getReservationTime()).isEqualTo(reservationTime2);
    }

    @DisplayName("해당하는 테마와 날짜의 예약이 존재하지 않으면 빈 값이 반환한다.")
    @Test
    void emptyFindByThemeIdAndDate() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime1 = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime1);

        ReservationTime reservationTime2 = new ReservationTime(LocalTime.of(22, 30));
        reservationTimeRepository.save(reservationTime2);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation1 = new Reservation(
                date,
                reservationTime1,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation1);

        Reservation reservation2 = new Reservation(
                date,
                reservationTime2,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation2);

        //when
        List<Reservation> actual = reservationRepository.findByThemeIdAndDate(null, date);

        //then
        assertThat(actual).isEmpty();
    }

    @DisplayName("일정 기간, 회원, 테마의 예약이 존재하면 반환한다.")
    @Test
    void findByThemeIdAndMemberIdAndDateBetween() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        List<Reservation> actual = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                theme.getId(),
                member.getId(),
                LocalDate.of(2025, 5, 1),
                date.plusDays(7)
        );

        //then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(reservation);
    }

    @DisplayName("일정 기간, 회원, 테마의 예약이 존재하지 않으면 빈 값이 반환한다.")
    @Test
    void emptyFindByThemeIdAndMemberIdAndDateBetween() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        List<Reservation> actual = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                null,
                null,
                LocalDate.of(2025, 5, 1),
                date.plusDays(7)
        );

        //then
        assertThat(actual).isEmpty();
    }

    @DisplayName("해당하는 테마가 예약에 존재하면 true를 반환한다.")
    @Test
    void existsByThemeId() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        boolean actual = reservationRepository.existsByThemeId(theme.getId());

        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("해당하는 테마가 예약에 존재하지 않으면 false를 반환한다.")
    @Test
    void nonExistsByThemeId() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(23, 30));
        reservationTimeRepository.save(reservationTime);

        Member member = new Member("도기", "email@example.com", "1234", Role.USER);
        memberRepository.save(member);

        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation = new Reservation(
                date,
                reservationTime,
                theme,
                member,
                LocalDate.of(2025, 1, 1)
        );
        reservationRepository.save(reservation);

        //when
        boolean actual = reservationRepository.existsByThemeId(null);

        //then
        assertThat(actual).isFalse();
    }

}
