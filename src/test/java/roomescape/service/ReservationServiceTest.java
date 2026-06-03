package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final String URL = "https://zeze.com/thumb.jpg";
    private static final String NAME = "제제";
    private static final LocalDateTime TODAY = LocalDateTime.of(2026, 5, 10, 10, 0, 0);
    private static final Slot DUMMY_SLOT = RoomEscapeFixture.slot().build();
    private static final Reservation DUMMY = RoomEscapeFixture.reservation().name(NAME).createdAt(TODAY).build();
    private static final long NOT_EXISTS_ID = Long.MAX_VALUE;
    private static final long EXISTS_ID = 1L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Nested
    @DisplayName("reserve")
    class Reserve {

        @Test
        void 존재하지_않는_시간으로_예약시_예외가_발생한다() {
            given(reservationTimeRepository.findById(999L)).willReturn(Optional.empty());

            ReservationCreateRequest request = RoomEscapeFixture.reservationCreateRequest();

            Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MAX))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        void APPROVED가_이미_있으면_WAITING으로_예약된다() {
            ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
            Theme theme = Theme.load(1L, new ThemeName("테마1"), "설명", new ThumbnailUrl(URL));
            Slot waitingSlot = RoomEscapeFixture.slot().id(2L)
                    .date(new ReservationDate(LocalDate.parse("2026-04-05"))).time(reservationTime).theme(theme)
                    .build();
            Reservation waitingSaved = RoomEscapeFixture.reservation().id(2L).name("zeze").slot(waitingSlot)
                    .status(Status.WAITING).createdAt(TODAY).build();

            ReservationCreateRequest request = new ReservationCreateRequest("zeze",
                    LocalDate.parse("2026-04-05"), 1L, 1L);
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsApprovedByTimeAndThemeAndDate(anyLong(), anyLong(), any()))
                    .willReturn(true);
            given(slotRepository.findByDateAndTimeAndTheme(any(), anyLong(), anyLong()))
                    .willReturn(Optional.of(waitingSlot));
            given(reservationRepository.save(any())).willReturn(waitingSaved);
            given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any()))
                    .willReturn(List.of(DUMMY, waitingSaved));

            RankedReservation result = reservationService.reserve(request,
                    LocalDateTime.of(2026, 4, 5, 10, 59, 59));

            Assertions.assertThat(result.getReservation().getStatus()).isEqualTo(Status.WAITING);
        }
    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void 존재하는_ID면_결과를_반환한다() {
            given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));
            given(reservationRepository.findByTimeAndThemeAndDate(any(), any(), any())).willReturn(List.of(DUMMY));

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
            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.parse("2099-04-06"), 1L, 1L);
            given(reservationRepository.findById(999L)).willReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> reservationService.update(request, 999L, LocalDateTime.MIN))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        void 과거_날짜의_예약이면_예외가_발생한다() {
            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.parse("2000-04-06"), 1L, 1L);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));

            Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MAX))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        void 시간을_찾을_수_없으면_예외가_발생한다() {
            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.parse("2099-04-06"), 1L, 1L);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                    .isInstanceOf(RoomEscapeException.class)
                    .hasMessage(ErrorCode.RESERVATION_TIME_NOT_FOUND.getMessage());
        }

        @Test
        void 이름_중복이면_예외가_발생한다() {
            ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("11:00"));
            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze",
                    LocalDate.parse("2099-04-06"), 1L, 1L);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByTimeAndThemeAndDateAndName(request.getTimeId(),
                    request.getThemeId(), request.getDate(), request.getName())).willReturn(true);

            Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                    .isInstanceOf(RoomEscapeException.class)
                    .hasMessage(ErrorCode.DUPLICATE_RESERVATION.getMessage());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        void 정상_취소시_삭제된다() {
            given(reservationRepository.findById(EXISTS_ID))
                    .willReturn(Optional.of(RoomEscapeFixture.reservation().build()));
            given(reservationRepository.findFirstWaitingByTimeAndThemeAndDate(anyLong(), anyLong(), any()))
                    .willReturn(Optional.empty());

            assertThatCode(() -> reservationService.cancel(EXISTS_ID, TODAY)).doesNotThrowAnyException();
            verify(reservationRepository).deleteById(EXISTS_ID);
        }

        @Test
        void APPROVED_예약_취소_시_첫번째_WAITING_예약이_승격된다() {
            Reservation waiting = RoomEscapeFixture.reservation().id(2L).name("대기자").slot(DUMMY_SLOT)
                    .status(Status.WAITING).createdAt(TODAY).build();
            given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
            given(reservationRepository.findFirstWaitingByTimeAndThemeAndDate(anyLong(), anyLong(), any()))
                    .willReturn(Optional.of(waiting));

            reservationService.cancel(1L, LocalDateTime.MIN);

            verify(reservationRepository).updateStatus(2L, Status.APPROVED);
        }

        @Test
        void WAITING_예약_취소_시_승격이_발생하지_않는다() {
            Reservation waiting = RoomEscapeFixture.reservation().id(2L).name("대기자").slot(DUMMY_SLOT)
                    .status(Status.WAITING).createdAt(TODAY).build();
            given(reservationRepository.findById(2L)).willReturn(Optional.of(waiting));

            reservationService.cancel(2L, LocalDateTime.MIN);

            verify(reservationRepository).deleteById(2L);
            org.mockito.Mockito.verifyNoMoreInteractions(reservationRepository);
        }

        @Test
        void 존재하지_않는_예약_취소시_예외_발생() {
            given(reservationRepository.findById(999L)).willReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> reservationService.cancel(999L, LocalDateTime.MIN))
                    .isInstanceOf(RoomEscapeException.class);
        }
    }
}
