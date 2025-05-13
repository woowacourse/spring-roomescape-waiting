package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixtures.NORMAL_MEMBER_1;
import static roomescape.TestFixtures.NORMAL_MEMBER_2;
import static roomescape.TestFixtures.RESERVATION_TIME_1;
import static roomescape.TestFixtures.RESERVATION_TIME_1_RESULT;
import static roomescape.TestFixtures.RESERVATION_TIME_2;
import static roomescape.TestFixtures.RESERVATION_TIME_3;
import static roomescape.TestFixtures.THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.application.reservation.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.dto.CreateReservationTimeParam;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;

class ReservationTimeServiceTest extends ServiceIntegrationTest {

    @Test
    void 예약_시간을_생성할_수_있다() {
        //given & when
        Long createdId = reservationTimeService.create(new CreateReservationTimeParam(LocalTime.of(12, 1)));

        //then
        assertThat(reservationTimeRepository.findById(createdId))
                .hasValue(new ReservationTime(1L, LocalTime.of(12, 1)));
    }

    @Test
    void id에_해당하는_예약_시간을_찾을_수_있다() {
        //given
        reservationTimeRepository.create(RESERVATION_TIME_1);

        //when
        ReservationTimeResult reservationTimeResult = reservationTimeService.findById(1L);

        //then
        assertThat(reservationTimeResult).isEqualTo(RESERVATION_TIME_1_RESULT);
    }

    @Test
    void id에_해당하는_예약_시간이_없는경우_예외가_발생한다() {
        //given & when & then
        assertThatThrownBy(() -> reservationTimeService.findById(1L))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("1에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        reservationTimeRepository.create(RESERVATION_TIME_1);

        //when
        List<ReservationTimeResult> reservationTimeResults = reservationTimeService.findAll();

        //then
        assertThat(reservationTimeResults).isEqualTo(List.of(RESERVATION_TIME_1_RESULT));
    }

    @Test
    void id에_해당하는_예약_시간을_삭제한다() {
        //given
        reservationTimeRepository.create(RESERVATION_TIME_1);

        //when
        reservationTimeService.deleteById(1L);

        //then
        assertThat(reservationTimeRepository.findById(1L)).isEmpty();
    }

    @Test
    void time_id를_사용하는_예약이_존재하면_예외를_던진다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        themeRepository.create(THEME_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        reservationRepository.create(new Reservation(
                1L,
                NORMAL_MEMBER_1,
                LocalDate.of(2025, 4, 30),
                RESERVATION_TIME_1,
                THEME_1));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 예약 시간에 예약이 존재합니다.");
    }

    @Test
    void 특정테마의_특정날짜에_예약된_시간_정보를_조회할_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        insertMember(NORMAL_MEMBER_2);
        themeRepository.create(THEME_1);

        reservationTimeRepository.create(RESERVATION_TIME_1);
        reservationTimeRepository.create(RESERVATION_TIME_2);
        reservationTimeRepository.create(RESERVATION_TIME_3);

        insertReservation(1L, LocalDate.now(clock), 1L, 1L);
        insertReservation(2L, LocalDate.now(clock), 3L, 1L);

        //when
        List<AvailableReservationTimeResult> availableTimesByThemeIdAndDate = reservationTimeService.findAvailableTimesByThemeIdAndDate(
                1L, LocalDate.now(clock));

        //then
        assertThat(availableTimesByThemeIdAndDate).isEqualTo(List.of(
                new AvailableReservationTimeResult(1L, RESERVATION_TIME_1.startAt(), true),
                new AvailableReservationTimeResult(2L, RESERVATION_TIME_2.startAt(), false),
                new AvailableReservationTimeResult(3L, RESERVATION_TIME_3.startAt(), true)
        ));
    }
}