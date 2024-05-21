package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    private final Member adminMember =
            new Member((long) 1, "어드민", "testDB@email.com", "1234", Role.ADMIN);
    private final Member userMember =
            new Member((long) 2, "사용자", "test2DB@email.com", "1234", Role.USER);
    private final Theme themeOne =
            new Theme((long) 1, "레벨1 탈출", "우테코 레벨2를 탈출하는 내용입니다",
                    "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    private final Theme themeTwo =
            new Theme((long) 2, "레벨2 탈출", "우테코 레벨3를 탈출하는 내용입니다",
                    "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    private final LocalDate fromDate = LocalDate.of(2024, 5, 18);
    private final LocalDate toDate = LocalDate.of(2024, 5, 20);
    private final TimeSlot timeOne = new TimeSlot((long) 1, LocalTime.of(10, 0));
    private final TimeSlot timeTwo = new TimeSlot((long) 2, LocalTime.of(11, 0));

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("member를 기준으로 해당 member의 모든 예약 목록을 조회한다.")
    @Test
    void findAllByMember() {
        //given, when
        List<Reservation> reservations = reservationRepository.findAllByMember(adminMember);

        //then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("date, theme를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByDateAndTheme() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByDateAndTheme(fromDate, themeOne);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("member, theme, date를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByMemberAndThemeAndDateBetween() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByMemberAndThemeAndDateBetween(adminMember, themeOne, fromDate, toDate);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("해당 theme에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByTheme_isTrue() {
        //given, when
        boolean isReservationExistsAtThemeOne = reservationRepository.existsByTheme(themeOne);

        //then
        assertThat(isReservationExistsAtThemeOne).isTrue();
    }

    @DisplayName("해당 theme에 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByTheme_isFalse() {
        //given, when
        boolean isReservationExistsAtThemeTwo = reservationRepository.existsByTheme(themeTwo);

        //then
        assertThat(isReservationExistsAtThemeTwo).isFalse();
    }

    @DisplayName("해당 time에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByTime_isTrue() {
        //given, when
        boolean isReservationExistsAtTimeOne = reservationRepository.existsByTime(timeOne);

        //then
        assertThat(isReservationExistsAtTimeOne).isTrue();
    }

    @DisplayName("해당 time에 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByTime_isFalse() {
        //given, when
        boolean isReservationExistsAtTimeTwo = reservationRepository.existsByTime(timeTwo);

        //then
       assertThat(isReservationExistsAtTimeTwo).isFalse();
    }

    @DisplayName("해당 date와 theme와 time에 해당하는 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByDateAndTimeAndTheme_isTrue() {
        //given, when
        boolean isReservationExists_true = reservationRepository
                .existsByDateAndTimeAndTheme(fromDate, timeOne, themeOne);

        //then
        assertThat(isReservationExists_true).isTrue();
    }

    @DisplayName("해당 date와 theme와 time에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByDateAndTimeAndTheme_isFalse() {
        //given, when
       boolean isReservationExists_false = reservationRepository
                .existsByDateAndTimeAndTheme(fromDate, timeTwo, themeTwo);

        //then
        assertThat(isReservationExists_false).isFalse();
    }
}
