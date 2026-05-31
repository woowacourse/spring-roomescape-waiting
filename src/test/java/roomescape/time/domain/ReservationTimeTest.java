package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static roomescape.time.exception.ReservationTimeErrorInformation.ID_IS_NULL;
import static roomescape.time.exception.ReservationTimeErrorInformation.START_AT_IS_NULL;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.time.exception.ReservationTimeException;

class ReservationTimeTest {

    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        reservationTime = ReservationTime.load(1L, LocalTime.of(10, 0), false);
    }

    @Test
    @DisplayName("예약 시간 id를 가져온다.")
    void getId() {
        //given
        Long expected = 1L;

        //when
        Long actual = reservationTime.getId();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약 시작 시간을 가져온다.")
    void getStartAt() {
        //given
        LocalTime expected = LocalTime.of(10, 0);

        //when
        LocalTime actual = reservationTime.getStartAt();

        //then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약 시작 시간이 유효하지 않은 경우 예외가 발생한다.")
    void validate_startAt() {
        assertThatThrownBy(() -> ReservationTime.load(1L, null, false))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(START_AT_IS_NULL.getMessage());
    }

    @Test
    @DisplayName("예약시간을 로드할 때, ID가 없으면 예외가 발생한다.")
    void load_id() {
        // given
        Long nullId = null;

        // when & then
        assertThatThrownBy(() -> ReservationTime.load(nullId, LocalTime.of(10, 0), false))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ID_IS_NULL.getMessage());
    }

}
