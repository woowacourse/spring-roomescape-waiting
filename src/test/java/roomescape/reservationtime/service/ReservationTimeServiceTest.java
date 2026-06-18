package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.config.TestTimeConfig;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Slot;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.SlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

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
    @Autowired
    private SlotRepository slotRepository;

    @Test
    void 중복된_예약시간을_추가하면_예외가_발생한다() {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(
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
        Slot slot = slotRepository.findOrCreate(LocalDate.parse("2026-08-05"), time, theme);
        reservationRepository.save(
                Reservation.create(slot, "브라운", ReservationStatus.CONFIRMED, LocalDateTime.now())
        );

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(time.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.RESERVATION_EXIST_ON_TIME);
    }

    @Test
    void 예약이_모두_취소되어_슬롯만_남은_시간도_삭제할_수_있다() {
        // given : 예약 생성 후 취소하면 슬롯 행만 남는다
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.parse("10:00"))
        );
        Theme theme = themeRepository.save(
                Theme.create("귀신찾기", "귀신을 찾는다", "https://image.png")
        );
        Slot slot = slotRepository.findOrCreate(LocalDate.parse("2026-08-05"), time, theme);
        Reservation saved = reservationRepository.save(
                Reservation.create(slot, "브라운", ReservationStatus.CONFIRMED, LocalDateTime.now())
        );
        reservationRepository.delete(saved.getId());

        // when
        reservationTimeService.deleteReservationTime(time.getId());

        // then
        assertThat(reservationTimeRepository.findById(time.getId())).isEmpty();
    }
}
