package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.response.ReservationOptionResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql("/clear.sql")
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

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
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");

        // when
        ReservationResponse response = reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", tomorrow, timeId, themeId)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(tomorrow);
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("예약자 이름으로 현재 시간 이후의 예약 및 대기 목록을 조회한다")
    void findReservationsAndWaitingsAfterNowByCustomerName() {
        // given
        final long pastTimeId = insertReservationTime("09:00:00");
        final long futureTimeId = insertReservationTime("23:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        insertReservation("브라운", "customer@example.com", yesterday, pastTimeId, themeId);
        insertReservation("브라운", "customer@example.com", tomorrow, futureTimeId, themeId);

        final long waitingSlotId = insertReservationSlot(tomorrow, pastTimeId, themeId);
        insertReservation("예약자", "owner@example.com", waitingSlotId);
        insertWaiting("브라운", "customer@example.com", waitingSlotId, LocalDateTime.now().minusMinutes(1));

        // when
        ReservationsAndWaitingsResponse responses = reservationService.getReservationsByCustomer(
                "브라운",
                "customer@example.com"
        );

        // then
        assertThat(responses.reservations()).hasSize(1);
        assertThat(responses.reservations().getFirst().name()).isEqualTo("브라운");

        assertThat(responses.waitings()).hasSize(1);
        assertThat(responses.waitings().getFirst().customerName()).isEqualTo("브라운");
        assertThat(responses.waitings().getFirst().rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("현재 이전 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationBeforeNow() {
        // given
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final long timeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", yesterday, timeId, themeId)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationWithNonExistingReservationTime() {
        // given
        final long unsavedTimeId = 999L;
        final long themeId = insertTheme("링", "공포 테마", "http:~");

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest(
                        "브라운",
                        "customer@example.com",
                        LocalDate.now().plusDays(1),
                        unsavedTimeId,
                        themeId
                )
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationWithNonExistingTheme() {
        // given
        final long timeId = insertReservationTime("10:00:00");
        final long unsavedThemeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest(
                        "브라운",
                        "customer@example.com",
                        LocalDate.now().plusDays(1),
                        timeId,
                        unsavedThemeId
                )
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationAtAlreadyReservedTime() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        reservationService.create(new ReservationCreateRequest(
                "브라운",
                "brown@example.com",
                tomorrow,
                timeId,
                themeId
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("재키", "jaekkii@example.com", tomorrow, timeId, themeId)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("관리자가 예약 일정을 수정한다")
    void updateReservationSchedule() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate changedFutureDate = LocalDate.now().plusDays(2);
        final long originTimeId = insertReservationTime("10:00:00");
        final long changedTimeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", futureDate, originTimeId, themeId);

        // when
        ReservationResponse response = reservationService.updateByAdmin(
                reservationId,
                new ReservationUpdateRequest(changedFutureDate, changedTimeId)
        );

        // then
        assertThat(response.id()).isEqualTo(reservationId);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(changedFutureDate);
        assertThat(response.time().id()).isEqualTo(changedTimeId);
        assertThat(response.theme().id()).isEqualTo(themeId);
        assertThat(reservationRepository.findById(reservationId))
                .get()
                .satisfies(reservation -> {
                    assertThat(reservation.getDate()).isEqualTo(changedFutureDate);
                    assertThat(reservation.getTime().getId()).isEqualTo(changedTimeId);
                });
    }

    @Test
    @DisplayName("존재하지 않는 예약을 관리자가 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingNonExistingReservation() {
        // given
        final long timeId = insertReservationTime("11:00:00");

        // when & then
        assertThatThrownBy(() -> reservationService.updateByAdmin(
                1L,
                new ReservationUpdateRequest(LocalDate.now().plusDays(1), timeId)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 관리자가 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingWithNonExistingReservationTime() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long timeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", futureDate, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.updateByAdmin(
                reservationId,
                new ReservationUpdateRequest(futureDate, 999L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("현재 이전 시간으로 관리자가 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleBeforeNow() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        final long originTimeId = insertReservationTime("11:00:00");
        final long changedTimeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", futureDate, originTimeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.updateByAdmin(
                reservationId,
                new ReservationUpdateRequest(yesterday, changedTimeId)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약 일정을 수정할 수 있다")
    void adminCanUpdateReservationScheduleOnReservationDate() {
        // given
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);
        final long originTimeId = insertReservationTime("11:00:00");
        final long changedTimeId = insertReservationTime("12:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", today, originTimeId, themeId);

        // when
        ReservationResponse response = reservationService.updateByAdmin(
                reservationId,
                new ReservationUpdateRequest(tomorrow, changedTimeId)
        );

        // then
        assertThat(response.date()).isEqualTo(tomorrow);
        assertThat(response.time().id()).isEqualTo(changedTimeId);
    }

    @Test
    @DisplayName("이미 예약된 시간으로 관리자가 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleToAlreadyReservedTime() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate changedFutureDate = LocalDate.now().plusDays(2);
        final long originTimeId = insertReservationTime("10:00:00");
        final long changedTimeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "brown@example.com", futureDate, originTimeId, themeId);
        insertReservation("재키", "jaekkii@example.com", changedFutureDate, changedTimeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.updateByAdmin(
                reservationId,
                new ReservationUpdateRequest(changedFutureDate, changedTimeId)
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("예약 가능 날짜와 테마를 조회한다")
    void findReservableDatesAndThemes() {
        // given
        final LocalDate today = LocalDate.now();
        insertTheme("링", "공포 테마", "http:~");

        // when
        ReservationOptionResponse response = reservationService.getReservationOptions();

        // then
        assertThat(response.dates()).hasSize(14);
        assertThat(response.dates().getFirst()).isEqualTo(today);
        assertThat(response.dates().getLast()).isEqualTo(today.plusDays(13));
        assertThat(response.themes())
                .extracting(theme -> theme.name())
                .containsExactly("링");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 고객이 취소하면 예외가 발생한다")
    void throwExceptionWhenCustomerCancelsNonExistingReservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(1L, "브라운", "customer@example.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("본인이 소유하지 않은 예약을 고객이 취소하면 예외가 발생한다")
    void throwExceptionWhenCustomerCancelsNotOwnedReservation() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        final long timeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", tomorrow, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(reservationId, "재키", "other@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        // given
        final LocalDate today = LocalDate.now();
        final long timeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", today, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(reservationId, "브라운", "customer@example.com"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("당일 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약을 취소할 수 있다")
    void adminCanCancelReservationOnReservationDate() {
        // given
        final LocalDate today = LocalDate.now();
        final long timeId = insertReservationTime("10:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final long reservationId = insertReservation("브라운", "customer@example.com", today, timeId, themeId);

        // when
        reservationService.cancelByAdmin(reservationId);

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 관리자가 취소하면 예외가 발생한다")
    void throwExceptionWhenAdminCancelsNonExistingReservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancelByAdmin(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("대기 순위는 같은 슬롯 내 createdAt 순서로 계산된다")
    void calculateWaitingRankByCreatedAtInSameSlot() {
        // given
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long slotId = insertReservationSlot(futureDate, timeId, themeId);
        insertReservation("예약자", "owner@example.com", slotId);

        insertWaiting("코로구", "korogoo@example.com", slotId, LocalDateTime.now().minusMinutes(2));
        insertWaiting("재키", "jaekkii@example.com", slotId, LocalDateTime.now().minusMinutes(1));
        insertWaiting("브라운", "customer@example.com", slotId, LocalDateTime.now());

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomer(
                "브라운",
                "customer@example.com"
        );

        // then
        assertThat(response.waitings()).hasSize(1);
        assertThat(response.waitings().getFirst().rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("대기 순위는 createdAt이 같으면 id 순서로 계산된다")
    void calculateWaitingRankByIdWhenCreatedAtIsSame() {
        // given
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long slotId = insertReservationSlot(futureDate, timeId, themeId);
        insertReservation("예약자", "owner@example.com", slotId);
        final LocalDateTime sameCreatedAt = LocalDateTime.now();

        insertWaiting("코로구", "korogoo@example.com", slotId, sameCreatedAt);
        insertWaiting("재키", "jaekkii@example.com", slotId, sameCreatedAt);
        insertWaiting("영이", "customer@example.com", slotId, sameCreatedAt);

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomer(
                "영이",
                "customer@example.com"
        );

        // then
        assertThat(response.waitings()).hasSize(1);
        assertThat(response.waitings().getFirst().rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("예약 취소 시 해당 슬롯의 가장 빠른 대기가 예약으로 전환된다")
    void promoteEarliestWaitingInSlotOnReservationCancel() {
        // given
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long slotId = insertReservationSlot(futureDate, timeId, themeId);
        final long reservationId = insertReservation("브라운", "brown@example.com", slotId);

        final long firstWaitingId = insertWaiting(
                "코로구",
                "korogoo@example.com",
                slotId,
                LocalDateTime.now().minusMinutes(2)
        );
        final long secondWaitingId = insertWaiting(
                "재키",
                "jaekkii@example.com",
                slotId,
                LocalDateTime.now().minusMinutes(1)
        );

        // when
        reservationService.cancelByCustomer(reservationId, "브라운", "brown@example.com");

        // then
        assertThat(findReservationBySlotId(slotId).getCustomerName()).isEqualTo("코로구");
        assertThat(waitingRepository.findById(firstWaitingId)).isEmpty();
        assertThat(waitingRepository.findById(secondWaitingId)).isPresent();
    }

    @Test
    @DisplayName("예약 취소 시 createdAt이 같으면 id가 작은 대기가 예약으로 전환된다")
    void promoteWaitingWithSmallerIdWhenCreatedAtIsSameOnReservationCancel() {
        // given
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long slotId = insertReservationSlot(futureDate, timeId, themeId);
        final long reservationId = insertReservation("브라운", "brown@example.com", slotId);
        final LocalDateTime sameCreatedAt = LocalDateTime.now();

        final long firstWaitingId = insertWaiting("코로구", "korogoo@example.com", slotId, sameCreatedAt);
        final long secondWaitingId = insertWaiting("재키", "jaekkii@example.com", slotId, sameCreatedAt);

        // when
        reservationService.cancelByCustomer(reservationId, "브라운", "brown@example.com");

        // then
        assertThat(findReservationBySlotId(slotId).getCustomerName()).isEqualTo("코로구");
        assertThat(waitingRepository.findById(firstWaitingId)).isEmpty();
        assertThat(waitingRepository.findById(secondWaitingId)).isPresent();
    }

    @Test
    @DisplayName("예약 취소 시 대기가 없으면 예약만 삭제된다")
    void deleteOnlyReservationWhenNoWaitingOnReservationCancel() {
        // given
        final long timeId = insertReservationTime("11:00:00");
        final long themeId = insertTheme("링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final long reservationId = insertReservation("브라운", "customer@example.com", futureDate, timeId, themeId);

        // when
        reservationService.cancelByCustomer(reservationId, "브라운", "customer@example.com");

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    private long insertReservationTime(final String startAt) {
        return reservationTimeRepository.save(ReservationTime.create(LocalTime.parse(startAt)))
                .getId();
    }

    private long insertTheme(
            final String name,
            final String description,
            final String thumbnailUrl
    ) {
        return themeRepository.save(Theme.create(name, description, thumbnailUrl))
                .getId();
    }

    private long insertReservation(
            final String customerName,
            final String customerEmail,
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        final long slotId = insertReservationSlot(date, timeId, themeId);
        return insertReservation(customerName, customerEmail, slotId);
    }

    private long insertReservation(
            final String customerName,
            final String customerEmail,
            final long slotId
    ) {
        final ReservationSlot slot = reservationSlotRepository.findByIdForUpdate(slotId)
                .orElseThrow();

        return reservationRepository.save(Reservation.of(
                null,
                customerName,
                customerEmail,
                slot
        )).getId();
    }

    private long insertReservationSlot(
            final LocalDate reservationDate,
            final long timeId,
            final long themeId
    ) {
        final ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow();
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow();

        return reservationSlotRepository.findOrCreate(reservationDate, time, theme)
                .getId();
    }

    private long insertWaiting(
            final String customerName,
            final String customerEmail,
            final long slotId,
            final LocalDateTime createdAt
    ) {
        final ReservationSlot slot = reservationSlotRepository.findByIdForUpdate(slotId)
                .orElseThrow();

        return waitingRepository.save(Waiting.of(
                null,
                customerName,
                customerEmail,
                slot,
                createdAt
        ));
    }

    private Reservation findReservationBySlotId(final long slotId) {
        return reservationRepository.findAll()
                .stream()
                .filter(reservation -> reservation.getSlotId().equals(slotId))
                .findFirst()
                .orElseThrow();
    }

}
