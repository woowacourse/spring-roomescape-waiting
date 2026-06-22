package roomescape.reservationtime.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.controller.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql("/clear.sql")
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 시간을 생성한다")
    void createReservationTime() {
        // when
        ReservationTimeResponse response = reservationTimeService.create(
                new ReservationTimeCreateRequest(LocalTime.of(10, 0))
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservationTimeRepository.findById(response.id()))
                .get()
                .extracting(ReservationTime::getStartAt)
                .isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNonExistingReservationTime() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("해당 시간에 예약 슬롯이 있으면 예약 시간 삭제시 예외가 발생한다")
    void throwExceptionWhenDeletingReservationTimeInUse() {
        // given
        final ReservationTime time = saveReservationTime("10:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~", 10000);
        saveReservation("브라운", "customer@example.com", LocalDate.now().plusDays(1), time, theme);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(time.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }

    private ReservationTime saveReservationTime(final String startAt) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.parse(startAt)));
    }

    private Theme saveTheme(
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        return themeRepository.save(Theme.create(name, description, thumbnailUrl, price));
    }

    private void saveReservation(
            final String customerName,
            final String customerEmail,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        final ReservationSlot slot = reservationSlotRepository.findOrCreate(date, time, theme);
        reservationRepository.save(Reservation.of(
                null,
                customerName,
                customerEmail,
                slot
        ));
    }
}
