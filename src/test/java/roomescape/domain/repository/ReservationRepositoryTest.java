package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        themeRepository.deleteAll();
        memberRepository.deleteAll();
        reservationTimeRepository.deleteAll();
    }

    @Test
    @DisplayName("예약에 대한 영속성을 저장한다")
    void save_ShouldStorePersistence() {
        // given
        LocalDate date = LocalDate.of(2023, 1, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Theme theme = new Theme("name", "desc", "thumb");
        Member member = new Member("name", "aa@aa.aa", "aa");
        Reservation reservation = new Reservation(date, time, theme, member);
        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);

        // when
        reservationRepository.save(reservation);

        // then
        Assertions.assertThat(reservationRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("예약 단권을 조회한다")
    void findById_ShouldGetSinglePersistence() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        Member member = new Member("memberName", "email", "password");
        ReservationTime savedTime = reservationTimeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);
        Member savedMember = memberRepository.save(member);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(LocalDate.of(2023, 2, 1), savedTime, savedTheme, savedMember));

        //when &then
        Assertions.assertThat(reservationRepository.findById(savedReservation.getId()))
                .isPresent();
    }


    @Test
    @DisplayName("특정 시간대, 테마이름, 예약자명의 예약들을 조회할 수 있다.")
    void findTimeIdByDateAndThemeId_ShouldGetSpecificPersistence() {
        // given
        Long themeId = createReservation(2);
        createReservation(2);
        createReservation(2);
        createReservation(3);

        // when
        List<Long> timeIds = reservationRepository.findTimeIdByDateAndThemeId(LocalDate.of(2023, 1, 2), themeId);

        // then
        Assertions.assertThat(timeIds)
                .hasSize(1);

    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete_ShouldRemovePersistence() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        ReservationTime savedTime = reservationTimeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);
        Member member = new Member("a", "b", "c");
        memberRepository.save(member);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(LocalDate.of(2023, 2, 1), savedTime, savedTheme, member));

        // when
        reservationRepository.delete(savedReservation);

        // then
        Assertions.assertThat(reservationRepository.findById(savedReservation.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("날짜, 시간, 테마를 기준으로 예약을 조회할 수 있다")
    void findByDateAndTimeIdAndThemeId_ShouldGetSpecificPersistence() {
        // given
        Theme theme1 = new Theme("theme_name", "desc", "thumbnail");
        Theme theme2 = new Theme("theme_name2", "desc", "thumbnail");
        Theme savedTheme1 = themeRepository.save(theme1);
        Theme savedTheme2 = themeRepository.save(theme2);

        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        Member member1 = new Member("name1", "email", "password");
        Member savedMember1 = memberRepository.save(member1);
        Reservation reservation1 = new Reservation(LocalDate.of(2023, 1, 1), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation2 = new Reservation(LocalDate.of(2023, 1, 2), savedTime, savedTheme2,
                savedMember1);

        Reservation savedReservation1 = reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        // when
        List<Reservation> findReservations = reservationRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.of(2023, 1, 1), savedTime.getId(), savedTheme1.getId());

        // then
        Assertions.assertThat(findReservations)
                .containsExactlyInAnyOrder(savedReservation1);
    }

    private Long createReservation(int dayOfMonth) {
        Theme theme = new Theme("name", "desc", "thumb");
        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        Member member = new Member("name", "aa@aa.aa", "aa");
        Reservation reservation = new Reservation(LocalDate.of(2023, 1, dayOfMonth), time, theme, member);

        themeRepository.save(theme);
        reservationTimeRepository.save(time);
        memberRepository.save(member);

        reservationRepository.save(reservation);
        return theme.getId();
    }
}
