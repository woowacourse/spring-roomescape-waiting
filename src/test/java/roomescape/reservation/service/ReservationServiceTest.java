package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.controller.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.response.ReservationOptionResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.service.support.FakeReservationRepository;
import roomescape.reservationtime.service.support.FakeReservationTimeRepository;
import roomescape.reservationslot.service.support.FakeReservationSlotRepository;
import roomescape.theme.service.support.FakeThemeRepository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.service.support.FakeWaitingRepository;

import java.sql.Date;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private FakeThemeRepository themeRepository;
    private FakeReservationSlotRepository reservationSlotRepository;
    private FakeWaitingRepository waitingRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = new FakeWaitingRepository();
        reservationSlotRepository = new FakeReservationSlotRepository(reservationRepository, waitingRepository);
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                reservationSlotRepository,
                waitingRepository
        );
    }

    @Test
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        final LocalDate tomorrow = LocalDate.now().plusDays(1);
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when
        ReservationResponse response = reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", tomorrow, 1L, 1L)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약자 이름으로 현재 시간 이후의 예약 및 대기 목록을 조회한다")
    void findReservationsAndWaitingsAfterNowByCustomerName() {
        // given
        final LocalDateTime oneHourBefore = NOW.minusHours(1);
        final LocalDateTime oneHourAfter = NOW.plusHours(1);

        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                oneHourBefore.toLocalDate(),
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationRepository.add(Reservation.of(
                2L,
                "브라운",
                "customer@example.com",
                oneHourAfter.toLocalDate(),
                ReservationTime.of(2L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                "customer@example.com",
                Date.valueOf(oneHourBefore.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                "customer@example.com",
                Date.valueOf(oneHourAfter.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        ReservationsAndWaitingsResponse responses = reservationService.getReservationsByCustomer("브라운", "customer@example.com");

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
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", yesterday, 1L, 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationWithNonExistingReservationTime() {
        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", LocalDate.now().plusDays(1), 1L, 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationWithNonExistingTheme() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", LocalDate.now().plusDays(1), 1L, 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationAtAlreadyReservedTime() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));
        reservationRepository.failToSaveByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", LocalDate.now().plusDays(1), 1L, 1L)
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    @DisplayName("예약 옵션이 변경된 상태로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationAfterOptionChanged() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));
        reservationRepository.failToSaveByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", "customer@example.com", LocalDate.now().plusDays(1), 1L, 1L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    @DisplayName("예약 일정을 수정한다")
    void updateReservationSchedule() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate changedFutureDate = LocalDate.now().plusDays(2);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                futureDate,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));

        // when
        ReservationResponse response = reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(changedFutureDate, 2L)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(changedFutureDate);
        assertThat(response.time().id()).isEqualTo(2L);
        assertThat(response.theme().id()).isEqualTo(1L);
        assertThat(reservationRepository.findById(1L).get().getTime().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingNonExistingReservation() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(LocalDate.now().plusDays(1), 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingWithNonExistingReservationTime() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                futureDate,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(futureDate, 999L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("현재 이전 시간으로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleBeforeNow() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate yesterday = LocalDate.now().minusDays(1);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                futureDate,
                ReservationTime.of(2L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(yesterday, 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약 일정을 수정할 수 없다")
    void customerCannotUpdateReservationScheduleOnReservationDateBeforeStartTime() {
        // given
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                today,
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(tomorrow, 2L)
        ))
                .isInstanceOf(ReservationModificationException.class);
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약 일정을 수정할 수 있다")
    void adminCanUpdateReservationScheduleOnReservationDate() {
        // given
        final LocalDate today = LocalDate.now();
        final LocalDate tomorrow = today.plusDays(1);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                today,
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when
        ReservationResponse response = reservationService.updateByAdmin(
                1L,
                new ReservationUpdateRequest(tomorrow, 2L)
        );

        // then
        assertThat(response.date()).isEqualTo(tomorrow);
        assertThat(response.time().id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미 예약된 시간으로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleToAlreadyReservedTime() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate changedFutureDate = LocalDate.now().plusDays(2);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                futureDate,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(changedFutureDate, 2L)
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    @DisplayName("예약 옵션이 변경된 상태로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleAfterOptionChanged() {
        // given
        final LocalDate futureDate = LocalDate.now().plusDays(1);
        final LocalDate changedFutureDate = LocalDate.now().plusDays(2);
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                futureDate,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                "브라운",
                "customer@example.com",
                new ReservationUpdateRequest(changedFutureDate, 2L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    @DisplayName("예약 가능 날짜와 테마를 조회한다")
    void findReservableDatesAndThemes() {
        // given
        final LocalDate today = LocalDate.now();
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

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
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        // given
        final LocalDate today = LocalDate.now();
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                today,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(1L, "브라운", "customer@example.com"))
                .isInstanceOf(ReservationCancellationException.class);
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약을 취소할 수 있다")
    void adminCanCancelReservationOnReservationDate() {
        // given
        final LocalDate today = LocalDate.now();
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                today,
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        reservationService.cancelByAdmin(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
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
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);

        waitingRepository.add(Waiting.of(1L, "코로구", "customer@example.com", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", "customer@example.com", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));
        waitingRepository.add(Waiting.of(3L, "브라운", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomer("브라운", "customer@example.com");

        // then
        assertThat(response.waitings()).hasSize(1);
        assertThat(response.waitings().getFirst().rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("대기 순위는 createdAt이 같으면 id 순서로 계산된다")
    void calculateWaitingRankByIdWhenCreatedAtIsSame() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);

        waitingRepository.add(Waiting.of(1L, "코로구", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(3L, "영이", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomer("영이", "customer@example.com");

        // then
        assertThat(response.waitings()).hasSize(1);
        assertThat(response.waitings().getFirst().rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("예약 취소 시 해당 슬롯의 가장 빠른 대기가 예약으로 전환된다")
    void promoteEarliestWaitingInSlotOnReservationCancel() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);

        reservationRepository.add(Reservation.of(1L, "브라운", "customer@example.com", futureDate, time, theme));

        waitingRepository.add(Waiting.of(1L, "코로구", "customer@example.com", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", "customer@example.com", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));

        // when
        reservationService.cancelByCustomer(1L, "브라운", "customer@example.com");

        // then
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("코로구");
    }

    @Test
    @DisplayName("예약 취소 시 createdAt이 같으면 id가 작은 대기가 예약으로 전환된다")
    void promoteWaitingWithSmallerIdWhenCreatedAtIsSameOnReservationCancel() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);

        reservationRepository.add(Reservation.of(1L, "브라운", "customer@example.com", futureDate, time, theme));

        waitingRepository.add(Waiting.of(1L, "코로구", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", "customer@example.com", Date.valueOf(futureDate), NOW, time, theme));

        // when
        reservationService.cancelByCustomer(1L, "브라운", "customer@example.com");

        // then
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("코로구");
    }

    @Test
    @DisplayName("예약 취소 시 대기가 없으면 예약만 삭제된다")
    void deleteOnlyReservationWhenNoWaitingOnReservationCancel() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.now().plusDays(1);

        reservationRepository.add(Reservation.of(1L, "브라운", "customer@example.com", futureDate, time, theme));

        // when
        reservationService.cancelByCustomer(1L, "브라운", "customer@example.com");

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

}
