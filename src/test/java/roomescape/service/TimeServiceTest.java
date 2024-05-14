package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.TimeSlotRequest;
import roomescape.domain.dto.TimeSlotResponse;
import roomescape.exception.DeleteNotAllowException;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TimeServiceTest {
    private final TimeService service;

    @Autowired
    public TimeServiceTest(final TimeService service) {
        this.service = service;
    }

    private long getTimeSlotSize() {
        return service.findAll().getData().size();
    }

    @Test
    @DisplayName("시간 목록을 반환한다.")
    void given_when_findAll_then_returnTimeSlotResponses() {
        //when, then
        assertThat(service.findAll().getData().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("시간 등록이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_timeSlotRequestWithInitialSize_when_create_then_returnTimeSlotResponseAndSaveDb() {
        //given
        long initialSize = getTimeSlotSize();
        TimeSlotRequest timeSlotRequest = new TimeSlotRequest(LocalTime.parse("11:22"));
        //when
        final TimeSlotResponse timeSlotResponse = service.create(timeSlotRequest);
        long afterCreateSize = getTimeSlotSize();
        //then
        assertThat(timeSlotResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("존재하는 시간을 삭제하면 Db에도 삭제된다.")
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getTimeSlotSize();
        //when
        service.delete(initialSize);
        long afterCreateSize = getTimeSlotSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    @Test
    @DisplayName("예약이 되어있는 시간을 지울 경우 예외를 발생시키고 Db에 반영하지 않는다.")
    void given_initialSize_when_createWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getTimeSlotSize();
        //when, then
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(DeleteNotAllowException.class);
        assertThat(getTimeSlotSize()).isEqualTo(initialSize);

    }
}