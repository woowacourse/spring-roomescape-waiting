package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationTimeRequestDTO;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Import(TestTimeConfig.class)
@Sql(scripts = "/empty.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 중복된_예약시간을_추가하면_예외가_발생한다() {
        // given
        ReservationTimeRequestDTO request = new ReservationTimeRequestDTO(
                LocalTime.parse("10:00")
        );

        reservationTimeService.addReservationTime(request);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.addReservationTime(request))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.RESERVATION_TIME_DUPLICATE);
    }

    @Test
    void 예약이_존재하는_시간을_삭제하면_예외가_발생한다() {
        // given
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );
        reservationRepository.save(
                Reservation.create("브라운", LocalDate.parse("2026-08-05"), time, theme)
        );

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME);
    }
}
