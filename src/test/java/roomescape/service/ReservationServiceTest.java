package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    ReservationService service;

    @Autowired
    ReservationTimeService timeService;

    @DisplayName("같은 날짜 및 시간 예약이 존재하면 예외를 던진다")
    @Test
    void addReservationWithDuplicatedReservation() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        
        ReservationTimeResponse response = timeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.parse("10:00")));

        service.addReservation(new ReservationRequest("test", date, 1L, response.id()));

        //when & then
        ReservationRequest duplicated = new ReservationRequest("test2", date, 1L, response.id());
        assertThatThrownBy(() -> service.addReservation(duplicated))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 이미 존재하는 예약시간입니다.");

    }

    @DisplayName("현재 시점 이전의 예약을 생성할 시 예외를 던진다")
    @Test
    void addReservationBeforeCurrentDateTime() {
        // given
        timeService.addReservationTime(new ReservationTimeRequest(LocalTime.parse("10:10")));
        LocalDate date = LocalDate.now().minusDays(1);
        ReservationRequest request = new ReservationRequest("호떡", date, 1L, 1L);

        // then & when
        assertThatThrownBy(() -> service.addReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 이전 시각으로 예약할 수 없습니다.");
    }

    @DisplayName("존재하지 않는 예약을 삭제하려는 경우 예외를 던진다")
    @Test
    void removeReservation() {
        //given
        long notExistId = 999;

        //when & then
        assertThatThrownBy(() -> service.removeReservation(notExistId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("[ERROR] 예약번호 999번은 존재하지 않습니다.");
    }

}
