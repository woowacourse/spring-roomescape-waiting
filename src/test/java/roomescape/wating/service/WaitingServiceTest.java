package roomescape.wating.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.wating.controller.dto.request.WaitingCreateRequest;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql("/clear.sql")
class WaitingServiceTest {

    @Autowired
    WaitingService waitingService;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationSlotRepository reservationSlotRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WaitingRepository waitingRepository;

    @Test
    @DisplayName("예약이 있는 슬롯에 대기를 등록한다")
    void createWaitingInReservedSlot() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        saveReservation("브라운", "brown@example.com", tomorrow, time, theme);

        // when
        long waitingId = waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", tomorrow, time.getId(), theme.getId())
        );

        // then
        assertThat(waitingId).isEqualTo(1L);
        assertThat(waitingRepository.findById(waitingId))
                .get()
                .extracting(waiting -> waiting.getCustomerName().name())
                .isEqualTo("재키");
    }

    @Test
    @DisplayName("예약이 없는 슬롯에 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenCreatingWaitingWithoutReservation() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        reservationSlotRepository.findOrCreate(tomorrow, time, theme);

        // when & then
        assertThatThrownBy(() -> waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", tomorrow, time.getId(), theme.getId())
        ))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessage("예약이 존재하지 않는 슬롯에는 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenCreatingDuplicatedWaitingInSameSlot() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        saveReservation("브라운", "brown@example.com", tomorrow, time, theme);
        waitingService.create(new WaitingCreateRequest("재키", "jaekkii@example.com", tomorrow, time.getId(), theme.getId()));

        // when & then
        assertThatThrownBy(() -> waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", tomorrow, time.getId(), theme.getId())
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 시간에 이미 대기가 존재합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 시간으로 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenCreatingWaitingWithNonExistingTime() {
        // given
        final Theme theme = saveTheme("링", "공포 테마", "http:~");

        // when & then
        assertThatThrownBy(() -> waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", LocalDate.now().plusDays(1), 999L, theme.getId())
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenCreatingWaitingWithNonExistingTheme() {
        // given
        final ReservationTime time = saveReservationTime("11:00:00");

        // when & then
        assertThatThrownBy(() -> waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", LocalDate.now().plusDays(1), time.getId(), 999L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("과거 시간의 예약에 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenCreatingWaitingInPastReservationDateTime() {
        // given
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        saveReservation("브라운", "brown@example.com", yesterday, time, theme);

        // when & then
        assertThatThrownBy(() -> waitingService.create(
                new WaitingCreateRequest("재키", "jaekkii@example.com", yesterday, time.getId(), theme.getId())
        ))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessage("과거 시간의 예약에 대기를 등록할 수 없습니다.");
    }

    @Test
    @DisplayName("대기 아이디와 본인 정보로 대기를 삭제한다")
    void deleteWaitingByIdAndCustomer() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        final Reservation reservation = saveReservation("브라운", "brown@example.com", tomorrow, time, theme);
        final long waitingId = saveWaiting("재키", "jaekkii@example.com", reservation.getSlot());

        // when
        waitingService.deleteByIdAndCustomer(waitingId, "재키", "jaekkii@example.com");

        // then
        assertThat(waitingRepository.findById(waitingId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 대기를 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNonExistingWaiting() {
        // when & then
        assertThatThrownBy(() -> waitingService.deleteByIdAndCustomer(1L, "재키", "jaekkii@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("본인이 소유하지 않은 대기를 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingNotOwnedWaiting() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        final Reservation reservation = saveReservation("브라운", "brown@example.com", tomorrow, time, theme);
        final long waitingId = saveWaiting("재키", "jaekkii@example.com", reservation.getSlot());

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByIdAndCustomer(waitingId, "브라운", "brown@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 대기입니다.");
    }

    @Test
    @DisplayName("과거 시간 예약의 대기를 삭제하면 예외가 발생한다")
    void throwExceptionWhenDeletingPastWaiting() {
        // given
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final ReservationTime time = saveReservationTime("11:00:00");
        final Theme theme = saveTheme("링", "공포 테마", "http:~");
        final Reservation reservation = saveReservation("브라운", "brown@example.com", yesterday, time, theme);
        final long waitingId = saveWaiting("재키", "jaekkii@example.com", reservation.getSlot());

        // when & then
        assertThatThrownBy(() -> waitingService.deleteByIdAndCustomer(waitingId, "재키", "jaekkii@example.com"))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessage("과거 시간 예약의 대기를 삭제할 수 없습니다.");
    }

    private ReservationTime saveReservationTime(final String startAt) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.parse(startAt)));
    }

    private Theme saveTheme(
            final String name,
            final String description,
            final String thumbnailUrl
    ) {
        return themeRepository.save(Theme.create(name, description, thumbnailUrl));
    }

    private Reservation saveReservation(
            final String customerName,
            final String customerEmail,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        final ReservationSlot slot = reservationSlotRepository.findOrCreate(date, time, theme);
        return reservationRepository.save(Reservation.of(
                null,
                customerName,
                customerEmail,
                slot
        ));
    }

    private long saveWaiting(
            final String customerName,
            final String customerEmail,
            final ReservationSlot slot
    ) {
        return waitingRepository.save(Waiting.of(
                null,
                customerName,
                customerEmail,
                slot,
                LocalDateTime.now()
        ));
    }
}
