package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("테마와 멤버의 아이디 및 특정 날짜 사이에 있는 예약들을 가져온다")
    void test1() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "설명", "썸네일");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "email@gmail.com", "password", Role.ADMIN);
        Member savedMember = memberRepository.save(member);

        Reservation reservationInRange = new Reservation(
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme,
                savedMember,
                LocalDate.now()
        );

        Reservation secondReservationOutOfRange = new Reservation(
                LocalDate.now().plusDays(3),
                savedReservationTime,
                savedTheme,
                savedMember,
                LocalDate.now()
        );

        reservationRepository.save(reservationInRange);
        reservationRepository.save(secondReservationOutOfRange);

        // when
        List<Reservation> actual = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                savedTheme.getId(),
                savedMember.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        // then
        List<Member> actualMembers = actual.stream()
                .map(Reservation::getMember)
                .toList();

        List<Theme> actualThemes = actual.stream()
                .map(Reservation::getTheme)
                .toList();

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actualMembers).containsOnly(member),
                () -> assertThat(actualThemes).containsOnly(theme)
        );
    }

    @Test
    @DisplayName("예약 시각과 날짜로 예약을 가져온다")
    void test2() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "설명", "썸네일");
        Theme savedTheme = themeRepository.save(theme);

        Member member = new Member("도기", "email@gmail.com", "password", Role.ADMIN);
        Member savedMember = memberRepository.save(member);

        Reservation reservationInRange = new Reservation(
                LocalDate.now().plusDays(1),
                savedReservationTime,
                savedTheme,
                savedMember,
                LocalDate.now()
        );

        Reservation secondReservationOutOfRange = new Reservation(
                LocalDate.now().plusDays(3),
                savedReservationTime,
                savedTheme,
                savedMember,
                LocalDate.now()
        );

        reservationRepository.save(reservationInRange);
        reservationRepository.save(secondReservationOutOfRange);

        // when
        Optional<Reservation> actual = reservationRepository.findByDateAndReservationTime(
                LocalDate.now().plusDays(1),
                savedReservationTime
        );

        // then
        assertAll(
                () -> assertThat(actual).isPresent(),
                () -> assertThat(actual.get().getId()).isEqualTo(reservationInRange.getId())
        );
    }

    @Test
    @DisplayName("특정 테마 ID로 모든 예약을 조회한다")
    void test3() {
        // given
        Theme theme = themeRepository.save(new Theme("공포", "무서움", "image.png"));
        Member member = memberRepository.save(new Member("히로", "hiro@example.com", "1234", Role.USER));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        Reservation reservation = new Reservation(LocalDate.now(), time, theme, member, LocalDate.now().minusDays(1));
        reservationRepository.save(reservation);

        // when
        List<Reservation> found = reservationRepository.findByThemeId(theme.getId());

        // then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getTheme().getName()).isEqualTo("공포");
    }

    @Test
    @DisplayName("특정 멤버 ID로 모든 예약을 조회한다")
    void test4() {
        // given
        Theme theme = themeRepository.save(new Theme("스릴러", "짜릿함", "thumb.jpg"));
        Member member = memberRepository.save(new Member("멤버1", "mem1@com", "pw", Role.USER));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));

        Reservation reservation = new Reservation(LocalDate.now(), time, theme, member, LocalDate.now().minusDays(1));
        reservationRepository.save(reservation);

        // when
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());

        // then
        assertThat(reservations).extracting(Reservation::getMember).allMatch(m -> m.getId().equals(member.getId()));
    }

    @Test
    @DisplayName("특정 예약 시간 ID로 예약을 조회한다")
    void test5() {
        // given
        Theme theme = themeRepository.save(new Theme("추리", "머리 아픔", "icon.png"));
        Member member = memberRepository.save(new Member("탐정", "detective@case.com", "pw", Role.USER));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(16, 0)));

        Reservation reservation = new Reservation(LocalDate.now(), time, theme, member, LocalDate.now().minusDays(1));
        reservationRepository.save(reservation);

        // when
        List<Reservation> result = reservationRepository.findByReservationTimeId(time.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationTime().getId()).isEqualTo(time.getId());
    }
}
