package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
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
    private static final Reservation DUMMY_RESERVATION = new Reservation(LocalDate.of(2023, 1, 1),
            new ReservationTime(LocalTime.of(1, 0)), new Theme("name", "desc", "thumb"),
            new Member("name", "aa@aa.aa", "aa"));

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약에 대한 영속성을 저장한다")
    void save_ShouldStorePersistence() {
        // when
        reservationRepository.save(DUMMY_RESERVATION);

        // then
        Assertions.assertThat(reservationRepository.findAll())
                .hasSize(1);
    }

    @Test
    void findTimeIdByDateAndThemeId_ShouldGetSpecificPersistence() {
        // given
        createReservation(2, 1L, 1L);
        createReservation(2, 2L, 2L);
        createReservation(2, 3L, 3L);
        createReservation(3, 4L, 4L);

        // when
        List<Long> timeIds = reservationRepository.findTimeIdByDateAndThemeId(LocalDate.of(2023, 1, 2), 1L);

        // then
        Assertions.assertThat(timeIds)
                .containsExactlyInAnyOrder(1L, 2L);

    }

    private void createReservation(int dayOfMonth, long themeId, long timeId) {
        Theme theme = new Theme(themeId, "name", "desc", "thumb");
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(1, 0));
        Member member = new Member("name", "aa@aa.aa", "aa");
        Reservation reservation = new Reservation(LocalDate.of(2023, 1, dayOfMonth), time, theme, member);

        themeRepository.save(theme);
        reservationTimeRepository.save(time);
        memberRepository.save(member);

        reservationRepository.save(reservation);
    }
}
