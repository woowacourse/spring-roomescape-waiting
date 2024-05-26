package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.*;

import java.util.List;

import static roomescape.fixture.fixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("member를 기준으로 해당 member의 모든 예약 목록을 날짜 순으로 조회한다.")
    @Test
    void findAllByMemberOrderByDateAsc() {
        //given, when
        List<Reservation> reservations = reservationRepository.findAllByMemberOrderByDateAsc(ADMIN_MEMBER);

        //then
        assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(reservations.get(0).getDate()).isEqualTo("2024-05-01"),
                () -> assertThat(reservations.get(2).getDate()).isEqualTo("2024-05-18")
        );
    }

    @DisplayName("date, theme를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByDateAndTheme() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByDateAndTheme(FROM_DATE, THEME_ONE);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("member, theme, date를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByMemberAndThemeAndDateBetween() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByMemberAndThemeAndDateBetween(ADMIN_MEMBER, THEME_ONE, FROM_DATE, TO_DATE);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("해당 theme에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByTheme_isTrue() {
        //when
        boolean isReservationExistsAtThemeOne = reservationRepository.existsByTheme(THEME_ONE);

        //then
        assertThat(isReservationExistsAtThemeOne).isTrue();
    }

    @DisplayName("해당 theme에 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByTheme_isFalse() {
        //when
        boolean isReservationExistsAtThemeTwo = reservationRepository.existsByTheme(THEME_TWO);

        //then
        assertThat(isReservationExistsAtThemeTwo).isFalse();
    }

    @DisplayName("해당 time에 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByTime_isTrue() {
        //when
        boolean isReservationExistsAtTimeOne = reservationRepository.existsByTime(TIME_ONE);

        //then
        assertThat(isReservationExistsAtTimeOne).isTrue();
    }

    @DisplayName("해당 time에 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByTime_isFalse() {
        //when
        boolean isReservationExistsAtTimeTwo = reservationRepository.existsByTime(TIME_TWO);

        //then
       assertThat(isReservationExistsAtTimeTwo).isFalse();
    }

    @DisplayName("해당 date와 member와 time에 해당하는 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByDateAndTimeAndMember_isTrue() {
        //when
        boolean isReservationExists_true = reservationRepository
                .existsByDateAndTimeAndMember(FROM_DATE, TIME_ONE, ADMIN_MEMBER);

        //then
        assertThat(isReservationExists_true).isTrue();
    }

    @DisplayName("해당 date와 member와 time에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByDateAndTimeAndMember_isFalse() {
        //when
       boolean isReservationExists_false = reservationRepository
                .existsByDateAndTimeAndMember(FROM_DATE, TIME_TWO, USER_MEMBER);

        //then
        assertThat(isReservationExists_false).isFalse();
    }

    @DisplayName("해당 date와 theme와 time과 member에 해당하는 예약이 존재하면 true를 반환한다.")
    @Test
    void existsByDateAndTimeAndThemeAndMember_isTrue() {
        //when
        boolean isReservationExists_true = reservationRepository
                .existsByDateAndTimeAndThemeAndMember(FROM_DATE, TIME_ONE, THEME_ONE, ADMIN_MEMBER);

        //then
        assertThat(isReservationExists_true).isTrue();
    }

    @DisplayName("해당 date와 theme와 time과 member에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByDateAndTimeAndThemeAndMember_isFalse() {
        //when
        boolean isReservationExists_false = reservationRepository
                .existsByDateAndTimeAndThemeAndMember(FROM_DATE, TIME_TWO, THEME_TWO, USER_MEMBER);

        //then
        assertThat(isReservationExists_false).isFalse();
    }
}
