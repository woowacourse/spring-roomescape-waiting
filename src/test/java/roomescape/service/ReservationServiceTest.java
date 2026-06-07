package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.RoomEscapeFixture;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.UnprocessableException;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final String URL = "https://zeze.com/thumb.jpg";
    private static final String NAME = "제제";
    private static final LocalDateTime TODAY = LocalDateTime.of(2026, 5, 10, 10, 0, 0);

    private static final Slot DUMMY_SLOT = Slot.load(
            1L,
            LocalDate.of(2099, 1, 1),
            ReservationTime.of(1L, LocalTime.of(10, 0)),
            Theme.load(1L, "any", "any", URL)
    );
    private static final Reservation DUMMY = Reservation.load(1L, NAME, "APPROVED", DUMMY_SLOT);

    private static final long NOT_EXISTS_ID = Long.MAX_VALUE;
    private static final long EXISTS_ID = 1L;

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약_취소_성공() {
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(slotRepository.findById(1L)).willReturn(Optional.of(DUMMY_SLOT));
        reservationService.cancel(1L, NAME, LocalDateTime.MIN);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void 존재하지_않는_예약_취소시_예외_발생() {
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> reservationService.cancel(999L, NAME, LocalDateTime.MIN))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외() {
        given(reservationTimeRepository.findById(999L)).willReturn(Optional.empty());
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-05-03"), 999L, 1L);
        Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MAX))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_예약_시_예외가_발생해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MAX));
    }

    @Test
    void 같은_날짜이며_시간이_1초_전이면_예약에_성공해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        ReservationCreateRequest request = new ReservationCreateRequest(NAME, LocalDate.of(2026, 4, 5), 1L, 1L);
        Slot mockSlot = Slot.load(1L, LocalDate.of(2026, 4, 5), reservationTime, theme);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(mockSlot));
        given(reservationRepository.existsBySlotIdAndName(1L, NAME)).willReturn(false);
        given(reservationRepository.existsApprovedBySlotId(1L)).willReturn(false);
        given(reservationRepository.save(any())).willReturn(DUMMY);
        Assertions.assertThatNoException()
                .isThrownBy(() -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 10, 59, 59)));
    }

    @Test
    void 같은_날짜이며_시간이_1초_지났다면_예약에_실패해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        Assertions.assertThatThrownBy(
                () -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 11, 0, 1)));
    }

    @Test
    void 미래로_예약하면_성공해야_한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2026-04-05"), 1L, 1L);
        Slot mockSlot = Slot.load(1L, LocalDate.parse("2026-04-05"), reservationTime, theme);
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(mockSlot));
        given(reservationRepository.existsBySlotIdAndName(1L, "zeze")).willReturn(false);
        given(reservationRepository.existsApprovedBySlotId(1L)).willReturn(false);
        given(reservationRepository.save(any())).willReturn(DUMMY);
        Assertions.assertThatNoException().isThrownBy(
                () -> reservationService.reserve(request, LocalDateTime.of(2026, 4, 5, 10, 59, 59)));
    }

    @Test
    void 예약_생성시_이미_예약된_예약이면_예외가_발생한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        Slot mockSlot = Slot.load(1L, LocalDate.parse("2099-04-05"), reservationTime, theme);
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2099-04-05"), 1L, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(mockSlot));
        given(reservationRepository.existsBySlotIdAndName(1L, "zeze")).willReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.MIN))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 이미_slot이_생성되어_있으면_원래_slot을_사용한다() {
        ReservationTime reservationTime = ReservationTime.of(LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "테마1", "설명", URL);
        Slot existingSlot = Slot.load(1L, LocalDate.parse("2099-04-05"), reservationTime, theme);
        ReservationCreateRequest request = new ReservationCreateRequest("zeze", LocalDate.parse("2099-04-05"), 1L, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(existingSlot));
        given(reservationRepository.existsBySlotIdAndName(1L, "zeze")).willReturn(false);
        given(reservationRepository.existsApprovedBySlotId(1L)).willReturn(false);
        given(reservationRepository.save(any())).willReturn(DUMMY);

        reservationService.reserve(request, LocalDateTime.MIN);

        verify(slotRepository, never()).save(any());
    }

    @Test
    void 예약_수정시_ID가_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L, 1L);
        given(reservationRepository.findById(999L)).willReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> reservationService.update(request, 999L, LocalDateTime.MIN))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다. 입력을 확인해 주세요.");
    }

    @Test
    void 예약_수정시_과거_날짜의_예약이면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2000-04-06"), 1L, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(ReservationTime.of(1L, LocalTime.parse("11:00"))));
        given(themeRepository.findById(1L)).willReturn(Optional.of(Theme.load(1L, "any", "any", URL)));
        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MAX))
                .isInstanceOf(UnprocessableException.class)
                .hasMessage("과거 예약에 대한 조작은 불가능합니다. 오늘 이후 날짜와 시간으로 다시 시도해 주세요");
    }

    @Test
    void 예약_수정시_시간을_찾을_수_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L, 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다. 입력을 확인해 주세요.");
    }

    @Test
    void 예약_수정시_사용_불가능한_날짜가_들어오면_예외가_발생한다() {
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "any", "any", URL);
        Slot newSlot = Slot.load(2L, LocalDate.parse("2099-04-06"), reservationTime, theme);
        ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.parse("2099-04-06"), 1L, 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(DUMMY));
        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(reservationTime));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(newSlot));
        given(reservationRepository.existsBySlotIdAndName(2L, "zeze")).willReturn(true);

        Assertions.assertThatThrownBy(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약된 시간입니다. 다른 시간을 선택해 주세요.");
    }

    @Test
    void 예약_수정시_같은_슬롯이면_자기_자신이므로_중복이_아니어야_한다() {
        // given
        LocalDate date = LocalDate.of(2099, 1, 1);
        LocalTime startAt = LocalTime.of(10, 0);
        long timeId = 1L;
        long themeId = 1L;
        String name = "zeze";

        ReservationTime time = ReservationTime.of(timeId, startAt);
        Theme theme = Theme.load(themeId, "any", "any", URL);
        Slot slot = Slot.load(1L, date, time, theme);
        Reservation existing = Reservation.load(1L, name, "APPROVED", slot);

        // 같은 슬롯, 같은 이름으로 수정 요청 (예: 프론트에서 저장 버튼을 다시 누른 경우)
        ReservationUpdateRequest request = new ReservationUpdateRequest(name, date, timeId, themeId);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(existing));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(time));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(slotRepository.findByDateAndTimeAndTheme(any(), any(), any())).willReturn(Optional.of(slot));

        // 자기 자신은 제외되므로 조건 불충족 (같은 slotId)
        given(reservationRepository.existsBySlotIdAndName(1L, name)).willReturn(true);
        given(reservationRepository.existsApprovedBySlotIdExcluding(1L, 1L)).willReturn(false);
        given(reservationRepository.update(eq(1L), any())).willReturn(existing);

        // when & then — 자기 자신이므로 성공해야 한다
        Assertions.assertThatCode(() -> reservationService.update(request, 1L, LocalDateTime.MIN))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_삭제_시_ID가_존재하지_않으면_예외가_발생한다() {
        given(reservationRepository.findById(NOT_EXISTS_ID)).willThrow(NotFoundException.class);
        Assertions.assertThatThrownBy(() -> reservationRepository.findById(NOT_EXISTS_ID))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_이름이_다르면_예외가_발생한다() {
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(reservation));
        given(slotRepository.findById(1L)).willReturn(Optional.of(DUMMY_SLOT));
        Assertions.assertThatThrownBy(() -> reservationService.cancel(EXISTS_ID, "diff", TODAY))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_문제가_없으면_삭제되어야_한다() {
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(reservation));
        given(slotRepository.findById(1L)).willReturn(Optional.of(DUMMY_SLOT));
        assertThatCode(() -> reservationService.cancel(EXISTS_ID, reservation.getName().getValue(),
                TODAY)).doesNotThrowAnyException();
    }

    @Test
    void 단건_조회시_존재하는_ID면_결과를_반환한다() {
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));

        Reservation result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.getId()).isEqualTo(EXISTS_ID);
    }

    @Test
    void 단건_조회시_존재하지_않는_ID면_예외가_발생한다() {
        given(reservationRepository.findById(NOT_EXISTS_ID)).willReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> reservationService.find(NOT_EXISTS_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다. 입력을 확인해 주세요.");
    }

    @Test
    void 이름_없이_목록_조회시_전체_예약을_반환한다() {
        given(reservationRepository.findAll()).willReturn(new Reservations(List.of(DUMMY)));

        Reservations results = reservationService.findList(null);

        Assertions.assertThat(results.getValues()).hasSize(1);
        Assertions.assertThat(results.getValues().get(0).getId()).isEqualTo(EXISTS_ID);
    }

    @Test
    void 이름으로_목록_조회시_해당_이름의_예약만_반환한다() {
        given(reservationRepository.findByName(NAME)).willReturn(new Reservations(List.of(DUMMY)));

        Reservations results = reservationService.findList(NAME);

        Assertions.assertThat(results.getValues()).hasSize(1);
        Assertions.assertThat(results.getValues().get(0).getName().getValue()).isEqualTo(NAME);
    }

    @Test
    void 첫번째_예약은_승인_상태이다() {
        given(reservationRepository.findById(EXISTS_ID)).willReturn(Optional.of(DUMMY));

        Reservation result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void 두번째_이후_예약은_대기_상태이고_대기번호_1번이다() {
        Slot waitingSlot = Slot.load(
                1L,
                LocalDate.of(2099, 1, 1),
                ReservationTime.of(1L, LocalTime.of(10, 0)),
                Theme.load(1L, "any", "any", URL)
        );
        Reservation approved = Reservation.load(1L, NAME, "APPROVED", waitingSlot);
        Reservation waiting = Reservation.load(2L, "대기자", "WAITING", waitingSlot);
        given(reservationRepository.findById(2L)).willReturn(Optional.of(waiting));
        given(reservationRepository.findBySlotId(1L)).willReturn(new Reservations(List.of(approved, waiting)));

        Reservation result = reservationService.find(2L);

        Assertions.assertThat(result.getStatus()).isEqualTo(Status.WAITING);
        Assertions.assertThat(result.getRank().getValue()).isEqualTo(1);
    }
}
