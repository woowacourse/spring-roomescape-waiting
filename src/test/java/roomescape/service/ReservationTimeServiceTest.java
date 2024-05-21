package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.BookResponse;
import roomescape.domain.dto.ReservationTimeRequest;
import roomescape.domain.dto.ReservationTimeResponse;
import roomescape.exception.DeleteNotAllowException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeServiceTest {
    private final ReservationTimeService service;

    @Autowired
    public ReservationTimeServiceTest(final ReservationTimeService service) {
        this.service = service;
    }

    private long getReservationTimeSize() {
        return service.findAll().getData().size();
    }

    @Test
    @DisplayName("시간 목록을 반환한다.")
    void given_when_findAll_then_returnReservationTimeResponses() {
        //when, then
        assertThat(service.findAll().getData().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("시간 등록이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_reservationTimeRequestWithInitialSize_when_create_then_returnReservationTimeResponseAndSaveDb() {
        //given
        long initialSize = getReservationTimeSize();
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("11:22"));
        //when
        final ReservationTimeResponse reservationTimeResponse = service.create(reservationTimeRequest);
        long afterCreateSize = getReservationTimeSize();
        //then
        assertThat(reservationTimeResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("존재하는 시간을 삭제하면 Db에도 삭제된다.")
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getReservationTimeSize();
        //when
        service.delete(initialSize);
        long afterCreateSize = getReservationTimeSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    @Test
    @DisplayName("예약이 되어있는 시간을 지울 경우 예외를 발생시키고 Db에 반영하지 않는다.")
    void given_initialSize_when_createWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getReservationTimeSize();
        //when, then
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(DeleteNotAllowException.class);
        assertThat(getReservationTimeSize()).isEqualTo(initialSize);
    }

    @DisplayName("예약 가능한 시간 목록들을 반환한다.")
    @Test
    void given_when_findAvailableBookList_thenReturnBookResponse() {
        //when
        final List<BookResponse> bookResponses = service.findAvailableBookList(LocalDate.parse("2099-05-08"), 1L)
                .getData();
        //then
        assertThat(bookResponses.size()).isEqualTo(4);
    }
}
