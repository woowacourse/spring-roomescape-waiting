package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.RankedReservation;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final String NAME = "제제";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 10, 10, 0, 0);
    private static final Slot SLOT = RoomEscapeFixture.slot().build();
    private static final Reservation DUMMY = RoomEscapeFixture.reservation().name(NAME).createdAt(NOW).build();
    private static final long NOT_EXISTS_ID = Long.MAX_VALUE;
    private static final long EXISTS_ID = 1L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SlotService slotService;

    @InjectMocks
    private ReservationService reservationService;

    @Nested
    @DisplayName("reserve")
    class Reserve {

        @Test
        void 슬롯_조회_실패시_예외가_발생한다() {
            given(slotService.findOrCreate(any(), anyLong(), anyLong()))
                    .willThrow(new RoomEscapeException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

            ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequest();

            Assertions.assertThatThrownBy(() -> reservationService.reserve(request, NOW))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        void 슬롯에_예약이_없으면_APPROVED로_예약된다() {
            given(slotService.findOrCreate(any(), anyLong(), anyLong())).willReturn(SLOT);
            given(reservationRepository.findAllBySlot(any()))
                    .willReturn(List.of())
                    .willReturn(List.of(DUMMY));
            given(reservationRepository.save(any())).willReturn(DUMMY);

            ReservationCreateRequest request = new ReservationCreateRequest(NAME,
                    LocalDate.of(2099, 11, 11), 1L, 1L);

            RankedReservation result = reservationService.reserve(request, NOW);

            Assertions.assertThat(result.getReservation().getStatus()).isEqualTo(Status.APPROVED);
        }

        @Test
        void APPROVED가_이미_있으면_WAITING으로_예약된다() {
            Reservation existing = RoomEscapeFixture.reservation().name("기존").createdAt(NOW).build();
            Reservation waitingSaved = RoomEscapeFixture.reservation().id(2L).name("달수")
                    .status(Status.WAITING).createdAt(NOW).build();

            given(slotService.findOrCreate(any(), anyLong(), anyLong())).willReturn(SLOT);
            given(reservationRepository.findAllBySlot(any()))
                    .willReturn(List.of(existing))
                    .willReturn(List.of(existing, waitingSaved));
            given(reservationRepository.save(any())).willReturn(waitingSaved);

            ReservationCreateRequest request = new ReservationCreateRequest("달수",
                    LocalDate.of(2099, 11, 11), 1L, 1L);

            RankedReservation result = reservationService.reserve(request, NOW);

            Assertions.assertThat(result.getReservation().getStatus()).isEqualTo(Status.WAITING);
        }
    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void 존재하는_ID면_결과를_반환한다() {
            given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));
            given(reservationRepository.findAllBySlot(any())).willReturn(List.of(DUMMY));

            RankedReservation result = reservationService.find(EXISTS_ID);

            Assertions.assertThat(result.getReservation().getId()).isEqualTo(EXISTS_ID);
            Assertions.assertThat(result.getRank().getValue()).isZero();
        }

        @Test
        void 존재하지_않는_ID면_예외가_발생한다() {
            given(reservationRepository.findById(NOT_EXISTS_ID)).willReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> reservationService.find(NOT_EXISTS_ID))
                    .isInstanceOf(RoomEscapeException.class)
                    .hasMessage(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("findList")
    class FindList {

        @Test
        void 이름_없이_목록_조회시_전체_예약을_반환한다() {
            given(reservationRepository.findAll()).willReturn(List.of(DUMMY));

            List<RankedReservation> results = reservationService.findList(null);

            Assertions.assertThat(results).hasSize(1);
        }

        @Test
        void 이름으로_목록_조회시_해당_이름의_예약만_반환한다() {
            given(reservationRepository.findAll()).willReturn(List.of(DUMMY));

            List<RankedReservation> results = reservationService.findList(NAME);

            Assertions.assertThat(results).hasSize(1);
            Assertions.assertThat(results.getFirst().getReservation().getName().getValue()).isEqualTo(NAME);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void ID가_없으면_예외가_발생한다() {
            given(reservationRepository.findById(NOT_EXISTS_ID)).willReturn(Optional.empty());

            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.of(2099, 4, 6), 1L, 1L);

            Assertions.assertThatThrownBy(() -> reservationService.update(request, NOT_EXISTS_ID, NOW))
                    .isInstanceOf(RoomEscapeException.class)
                    .hasMessage(ErrorCode.RESERVATION_NOT_FOUND.getMessage());
        }

        @Test
        void 과거_날짜의_예약이면_예외가_발생한다() {
            given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));

            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.of(2000, 4, 6), 1L, 1L);

            Assertions.assertThatThrownBy(() -> reservationService.update(request, EXISTS_ID, LocalDateTime.MAX))
                    .isInstanceOf(RoomEscapeException.class)
                    .hasMessage(ErrorCode.PAST_RESERVATION_NOT_ALLOWED.getMessage());
        }

        @Test
        void WAITING_예약을_수정하면_승격이_발생하지_않는다() {
            Reservation waiting = RoomEscapeFixture.reservation().id(2L).name("대기자")
                    .status(Status.WAITING).createdAt(NOW).build();
            Reservation updated = RoomEscapeFixture.reservation().id(2L).name("대기자")
                    .status(Status.WAITING).createdAt(NOW).build();

            given(reservationRepository.findById(2L)).willReturn(Optional.of(waiting));
            given(slotService.findOrCreate(any(), anyLong(), anyLong())).willReturn(SLOT);
            given(reservationRepository.findAllBySlot(any()))
                    .willReturn(List.of())
                    .willReturn(List.of(updated));
            given(reservationRepository.update(anyLong(), any())).willReturn(updated);

            ReservationUpdateRequest request = new ReservationUpdateRequest("대기자",
                    LocalDate.of(2099, 11, 11), 1L, 1L);

            reservationService.update(request, 2L, LocalDateTime.MIN);

            verify(reservationRepository, times(0)).updateStatus(anyLong(), any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        void 정상_취소시_삭제된다() {
            given(reservationRepository.findById(EXISTS_ID))
                    .willReturn(Optional.of(RoomEscapeFixture.reservation().build()));
            given(reservationRepository.findFirstWaitingBySlot(any())).willReturn(Optional.empty());

            assertThatCode(() -> reservationService.cancel(EXISTS_ID, NOW)).doesNotThrowAnyException();
        }

        @Test
        void APPROVED_예약_취소_시_첫번째_WAITING_예약이_승격된다() {
            Reservation waiting = RoomEscapeFixture.reservation().id(2L).name("대기자")
                    .status(Status.WAITING).createdAt(NOW).build();
            given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));
            given(reservationRepository.findFirstWaitingBySlot(any())).willReturn(Optional.of(waiting));

            reservationService.cancel(EXISTS_ID, LocalDateTime.MIN);

            verify(reservationRepository).updateStatus(2L, Status.APPROVED);
        }

        @Test
        void WAITING_예약_취소_시_승격이_발생하지_않는다() {
            Reservation waiting = RoomEscapeFixture.reservation().id(2L).name("대기자")
                    .status(Status.WAITING).createdAt(NOW).build();
            given(reservationRepository.findById(2L)).willReturn(Optional.of(waiting));

            reservationService.cancel(2L, LocalDateTime.MIN);

            verify(reservationRepository).deleteById(2L);
            org.mockito.Mockito.verifyNoMoreInteractions(reservationRepository);
        }

        @Test
        void 존재하지_않는_예약_취소시_예외_발생() {
            given(reservationRepository.findById(NOT_EXISTS_ID)).willReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> reservationService.cancel(NOT_EXISTS_ID, NOW))
                    .isInstanceOf(RoomEscapeException.class);
        }
    }
}
