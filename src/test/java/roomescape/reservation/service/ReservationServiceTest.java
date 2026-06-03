package roomescape.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationCancellationException;
import roomescape.reservation.domain.exception.ReservationModificationException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.request.ReservationUpdateRequest;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
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

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDateTime NOW = LocalDateTime.now(FIXED_CLOCK);

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
                waitingRepository,
                FIXED_CLOCK
        );
    }

    @Test
    @DisplayName("예약을 생성한다")
    void createReservation() {
        // given
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(11, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when
        ReservationResponse response = reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
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
                oneHourBefore.toLocalDate(),
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationRepository.add(Reservation.of(
                2L,
                "브라운",
                oneHourAfter.toLocalDate(),
                ReservationTime.of(2L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                Date.valueOf(oneHourBefore.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourBefore.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        waitingRepository.add(Waiting.of(
                1L,
                "브라운",
                Date.valueOf(oneHourAfter.toLocalDate()),
                NOW,
                ReservationTime.of(1L, oneHourAfter.toLocalTime()),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        ReservationsAndWaitingsResponse responses = reservationService.getReservationsByCustomerName("브라운");

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
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약하면 예외가 발생한다")
    void throwExceptionWhenCreatingReservationWithNonExistingReservationTime() {
        // when & then
        assertThatThrownBy(() -> reservationService.create(
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 8, 5), 1L, 1L)
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
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 8, 5), 1L, 1L)
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
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
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
                new ReservationCreateRequest("브라운", LocalDate.of(2026, 5, 8), 1L, 1L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    @DisplayName("예약 일정을 수정한다")
    void updateReservationSchedule() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));

        // when
        ReservationResponse response = reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        );

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("브라운");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 8, 6));
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
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 5), 1L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingWithNonExistingReservationTime() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 5), 999L)
        ))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("현재 이전 시간으로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleBeforeNow() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(2L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 8), 1L)
        ))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약 일정을 수정할 수 없다")
    void customerCannotUpdateReservationScheduleOnReservationDateBeforeStartTime() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 9), 2L)
        ))
                .isInstanceOf(ReservationModificationException.class);
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약 일정을 수정할 수 있다")
    void adminCanUpdateReservationScheduleOnReservationDate() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(12, 0)));

        // when
        ReservationResponse response = reservationService.updateByAdmin(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 5, 9), 2L)
        );

        // then
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 9));
        assertThat(response.time().id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미 예약된 시간으로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleToAlreadyReservedTime() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByDuplicatedReservation();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        ))
                .isInstanceOf(ReservationAlreadyExistsException.class);
    }

    @Test
    @DisplayName("예약 옵션이 변경된 상태로 예약 일정을 수정하면 예외가 발생한다")
    void throwExceptionWhenUpdatingReservationScheduleAfterOptionChanged() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));
        reservationTimeRepository.add(ReservationTime.of(2L, LocalTime.of(11, 0)));
        reservationRepository.failToUpdateByChangedOption();

        // when & then
        assertThatThrownBy(() -> reservationService.updateByCustomer(
                1L,
                new ReservationUpdateRequest(LocalDate.of(2026, 8, 6), 2L)
        ))
                .isInstanceOf(ReservationOptionChangedException.class);
    }

    @Test
    @DisplayName("예약 가능 날짜와 테마를 조회한다")
    void findReservableDatesAndThemes() {
        // given
        themeRepository.add(Theme.of(1L, "링", "공포 테마", "http:~"));

        // when
        ReservationOptionResponse response = reservationService.getReservationOptions();

        // then
        assertThat(response.dates()).hasSize(14);
        assertThat(response.dates().getFirst()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(response.dates().getLast()).isEqualTo(LocalDate.of(2026, 5, 21));
        assertThat(response.themes())
                .extracting(theme -> theme.name())
                .containsExactly("링");
    }

    @Test
    @DisplayName("존재하지 않는 예약을 취소하면 예외가 발생한다")
    void throwExceptionWhenCancelingNonExistingReservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelByCustomer(1L))
                .isInstanceOf(ReservationCancellationException.class);
    }

    @Test
    @DisplayName("관리자는 예약일 당일에도 예약을 삭제할 수 있다")
    void adminCanDeleteReservationOnReservationDate() {
        // given
        reservationRepository.add(Reservation.of(
                1L,
                "브라운",
                LocalDate.of(2026, 5, 8),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.of(1L, "링", "공포 테마", "http:~")
        ));

        // when
        reservationService.deleteByAdmin(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 관리자가 삭제하면 예외가 발생한다")
    void throwExceptionWhenAdminDeletesNonExistingReservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.deleteByAdmin(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("대기 순위는 같은 슬롯 내 createdAt 순서로 계산된다")
    void calculateWaitingRankByCreatedAtInSameSlot() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));
        waitingRepository.add(Waiting.of(3L, "브라운", Date.valueOf(futureDate), NOW, time, theme));

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomerName("브라운");

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
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(3L, "영이", Date.valueOf(futureDate), NOW, time, theme));

        // when
        ReservationsAndWaitingsResponse response = reservationService.getReservationsByCustomerName("영이");

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
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        reservationRepository.add(Reservation.of(1L, "브라운", futureDate, time, theme));

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW.minusMinutes(2), time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW.minusMinutes(1), time, theme));

        // when
        reservationService.cancelByCustomer(1L);

        // then
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("코로구");
    }

    @Test
    @DisplayName("예약 취소 시 createdAt이 같으면 id가 작은 대기가 예약으로 전환된다")
    void promoteWaitingWithSmallerIdWhenCreatedAtIsSameOnReservationCancel() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        reservationRepository.add(Reservation.of(1L, "브라운", futureDate, time, theme));

        waitingRepository.add(Waiting.of(1L, "코로구", Date.valueOf(futureDate), NOW, time, theme));
        waitingRepository.add(Waiting.of(2L, "재키", Date.valueOf(futureDate), NOW, time, theme));

        // when
        reservationService.cancelByCustomer(1L);

        // then
        assertThat(reservationRepository.savedReservation().getCustomerName()).isEqualTo("코로구");
    }

    @Test
    @DisplayName("예약 취소 시 대기가 없으면 예약만 삭제된다")
    void deleteOnlyReservationWhenNoWaitingOnReservationCancel() {
        // given
        final ReservationTime time = ReservationTime.of(1L, LocalTime.of(11, 0));
        final Theme theme = Theme.of(1L, "링", "공포 테마", "http:~");
        final LocalDate futureDate = LocalDate.of(2026, 5, 9);

        reservationRepository.add(Reservation.of(1L, "브라운", futureDate, time, theme));

        // when
        reservationService.cancelByCustomer(1L);

        // then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

}
