package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.domain.Reservation;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTimeServiceTest extends FixtureUsingTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @DisplayName("저장된 시간을 모두 조회할 수 있다.")
    @Test
    void findAllTest() {
        //when
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.findAll();

        //then
        assertThat(reservationTimeResponses)
                .hasSize(countOfSavedReservationTime);
    }

    @DisplayName("날짜와 테마, 시간에 대한 예약 내역을 확인할 수 있다.")
    @Test
    void findAvailableTimeTest() {
        //given
        LocalDate selectedDate = LocalDate.of(2024, 1, 1);

        reservationRepository.save(new Reservation(selectedDate, reservationTime_10_0, theme1,USER));
        reservationRepository.save(new Reservation(selectedDate, reservationTime_12_0, theme1,USER));

        //when
        List<AvailableTimeResponse> availableTimeResponses = reservationTimeService.findByThemeAndDate(selectedDate,
                theme1.getId());

        //then
        assertThat(availableTimeResponses).containsExactlyInAnyOrder(
                new AvailableTimeResponse(reservationTime_10_0.getId(), reservationTime_10_0.getStartAt(), true),
                new AvailableTimeResponse(reservationTime_11_0.getId(), reservationTime_11_0.getStartAt(), false),
                new AvailableTimeResponse(reservationTime_12_0.getId(), reservationTime_12_0.getStartAt(), true),
                new AvailableTimeResponse(reservationTime_13_0.getId(), reservationTime_13_0.getStartAt(), false)
        );
    }


    @DisplayName("정상적으로 시간을 생성할 수 있다.")
    @Test
    void saveReservationTimeTest() {
        assertThatCode(() ->
                reservationTimeService.save(new ReservationTimeRequest(reservationTimeNotSaved.getStartAt())))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중복된 시간은 생성할 수 없는지 검증")
    void saveFailCauseDuplicate() {
        assertThatThrownBy(() -> reservationTimeService.save(new ReservationTimeRequest(
                reservationTime_10_0.getStartAt())))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_RESERVATION_TIME.getMessage());
    }

    @DisplayName("저장된 시간을 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        //when
        reservationTimeService.delete(reservationTime_11_0.getId());

        //then
        assertThat(reservationTimeRepository.findAll())
                .hasSize(countOfSavedReservationTime - 1);
    }

    @DisplayName("예약 시간을 사용하는 예약이 있으면 예약을 삭제할 수 없다.")
    @Test
    void usedReservationTimeDeleteTest() {
        //given
        reservationRepository.save(new Reservation(LocalDate.now(), reservationTime_10_0, theme1, USER));

        //when & then
        assertThatCode(() -> reservationTimeService.delete(reservationTime_10_0.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_TIME.getMessage());
    }
}
