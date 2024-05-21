package roomescape.time.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.ReservationTimeCreateRequest;
import roomescape.time.dto.ReservationTimeResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(value = {ReservationTimeService.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("예약 시간 서비스")
class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;

    @Autowired
    public ReservationTimeServiceTest(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @DisplayName("에약 시간 서비스는 시간을 생성한다.")
    @Test
    void createTime() {
        // given
        LocalTime startAt = LocalTime.of(3, 30);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt);

        // when
        ReservationTimeResponse reservationTime = reservationTimeService.createTime(request);

        // then
        assertThat(reservationTime.startAt())
                .isEqualTo(startAt);
    }

    @DisplayName("예약 시간 서비스는 중복된 예약 시간 요청이 들어오면 예외가 발생한다.")
    @Test
    void validateIsDuplicated() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 예약 시간입니다.");
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간을 반환한다.")
    @Test
    void readReservationTime() {
        // given
        Long id = 1L;

        // when
        ReservationTimeResponse reservationTime = reservationTimeService.readReservationTime(id);

        // then
        assertThat(reservationTime.startAt())
                .isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("예약 시간 서비스는 시간들을 반환한다.")
    @Test
    void readReservationTimes() {
        // when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.readReservationTimes();

        // then
        assertThat(reservationTimes.size()).isEqualTo(3);
    }

    @DisplayName("예약 시간 서비스는 지정된 날짜와 테마별 예약 가능 여부를 포함하여 시간들을 반환한다.")
    @Test
    void readReservationTimesByDateAndThemeId() {
        // given
        LocalDate date = LocalDate.of(2099, 12, 31);
        Long themeId = 1L;

        // when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.readReservationTimes(date, themeId);

        // then
        assertThat(reservationTimes).hasSize(3);
        assertThat(reservationTimes).contains(
                ReservationTimeResponse.of(new ReservationTime(1L, LocalTime.of(10, 0)), true)
        );
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간을 삭제한다.")
    @Test
    void deleteTime() {
        // given
        Long id = 3L;

        // when & then
        assertThatCode(() -> reservationTimeService.deleteTime(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간 서비스는 id에 맞는 시간에 예약이 존재하면 예외가 발생한다.")
    @Test
    void deleteTimeWithExistsReservation() {
        // given
        Long id = 1L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(id))
                .isInstanceOf(BadRequestException.class);
    }
}
