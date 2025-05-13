package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixtures.NORMAL_MEMBER_1;
import static roomescape.TestFixtures.NORMAL_MEMBER_1_RESULT;
import static roomescape.TestFixtures.RESERVATION_TIME_1;
import static roomescape.TestFixtures.RESERVATION_TIME_1_RESULT;
import static roomescape.TestFixtures.RESERVATION_TIME_2;
import static roomescape.TestFixtures.RESERVATION_TIME_2_RESULT;
import static roomescape.TestFixtures.THEME_1;
import static roomescape.TestFixtures.THEME_1_RESULT;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.application.reservation.dto.CreateReservationParam;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.reservation.Reservation;

class ReservationServiceTest extends ServiceIntegrationTest {

    public static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);

    @Test
    void 예약을_생성한다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        themeRepository.create(THEME_1);
        CreateReservationParam createReservationParam = new CreateReservationParam(RESERVATION_DATE, 1L, 1L, 1L);

        //when
        Long createdId = reservationService.create(createReservationParam);

        //then
        assertThat(reservationRepository.findById(createdId))
                .hasValue(new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        RESERVATION_DATE,
                        RESERVATION_TIME_1,
                        THEME_1));
    }

    @Test
    void 예약을_생성할때_timeId가_데이터베이스에_존재하지_않는다면_예외가_발생한다() {
        //give
        insertMember(NORMAL_MEMBER_1);
        themeRepository.create(THEME_1);
        CreateReservationParam createReservationParam = new CreateReservationParam(
                RESERVATION_DATE,
                1L,
                THEME_1.getId(),
                NORMAL_MEMBER_1.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(createReservationParam))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("1에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void id값으로_예약을_삭제할_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        themeRepository.create(THEME_1);
        reservationRepository.create(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        RESERVATION_DATE,
                        RESERVATION_TIME_1,
                        THEME_1));

        //when
        reservationService.deleteById(1L);

        //then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        themeRepository.create(THEME_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        reservationTimeRepository.create(RESERVATION_TIME_2);
        reservationRepository.create(new Reservation(
                1L,
                NORMAL_MEMBER_1,
                RESERVATION_DATE,
                RESERVATION_TIME_1,
                THEME_1));
        reservationRepository.create(new Reservation(
                2L,
                NORMAL_MEMBER_1,
                RESERVATION_DATE,
                RESERVATION_TIME_2,
                THEME_1));

        //when
        List<ReservationResult> reservationResults = reservationService.findAll();

        //then
        assertThat(reservationResults).isEqualTo(List.of(
                new ReservationResult(1L, NORMAL_MEMBER_1_RESULT, RESERVATION_DATE,
                        RESERVATION_TIME_1_RESULT,
                        THEME_1_RESULT),
                new ReservationResult(2L, NORMAL_MEMBER_1_RESULT, RESERVATION_DATE,
                        RESERVATION_TIME_2_RESULT,
                        THEME_1_RESULT)
        ));
    }

    @Test
    void id_로_예약을_찾을_수_있다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        themeRepository.create(THEME_1);
        reservationRepository.create(new Reservation(
                1L,
                NORMAL_MEMBER_1,
                RESERVATION_DATE,
                RESERVATION_TIME_1,
                THEME_1));

        //when
        ReservationResult reservationResult = reservationService.findById(1L);

        //then
        assertThat(reservationResult).isEqualTo(
                new ReservationResult(
                        1L,
                        NORMAL_MEMBER_1_RESULT,
                        RESERVATION_DATE,
                        RESERVATION_TIME_1_RESULT,
                        THEME_1_RESULT)
        );
    }

    @Test
    void id에_해당하는_예약이_없는경우_예외가_발생한다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        themeRepository.create(THEME_1);
        reservationRepository.create(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        RESERVATION_DATE,
                        RESERVATION_TIME_1,
                        THEME_1));

        //when & then
        assertThatThrownBy(() -> reservationService.findById(2L))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage("2에 해당하는 reservation 튜플이 없습니다.");
    }

    @Test
    void 날짜와_시간이_중복된_예약이_있으면_예외가_발생한다() {
        //given
        insertMember(NORMAL_MEMBER_1);
        reservationTimeRepository.create(RESERVATION_TIME_1);
        themeRepository.create(THEME_1);
        reservationRepository.create(
                new Reservation(
                        1L,
                        NORMAL_MEMBER_1,
                        RESERVATION_DATE,
                        RESERVATION_TIME_1,
                        THEME_1));

        //when & then
        assertThatThrownBy(
                () -> reservationService.create(new CreateReservationParam(RESERVATION_DATE, 1L, 1L, 1L)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("날짜와 시간이 중복된 예약이 존재합니다.");
    }
}