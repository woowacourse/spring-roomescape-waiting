package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.RoomEscapeFixture;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final String URL = "https://zeze.com/thumb.jpg";
    private static final String NAME = "제제";
    private static final long MEMBER_ID = 1L;

    private static final Member DUMMY_MEMBER = new Member(MEMBER_ID, NAME);
    private static final Slot DUMMY_SLOT = Slot.load(
            1L,
            LocalDate.of(2099, 1, 1),
            ReservationTime.load(1L, LocalTime.of(10, 0)),
            Theme.load(1L, "any", "any", URL)
    );
    private static final Reservation DUMMY = new Reservation(1L, DUMMY_MEMBER, Status.APPROVED, DUMMY_SLOT);

    private static final long NOT_EXISTS_ID = Long.MAX_VALUE;
    private static final long EXISTS_ID = 1L;

    @Mock
    private Clock clock;
    @Mock
    private ReservationAssembler assembler;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private ReservationService reservationService;

    private void givenNow(LocalDateTime dateTime) {
        given(clock.instant()).willReturn(dateTime.toInstant(ZoneOffset.UTC));
        given(clock.getZone()).willReturn(ZoneOffset.UTC);
    }

    @Test
    void 예약_취소_성공() {
        givenNow(LocalDateTime.of(2026, 1, 1, 0, 0));
        given(reservationRepository.getById(1L)).willReturn(DUMMY);
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of());
        reservationService.cancel(1L, MEMBER_ID);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    void 존재하지_않는_예약_취소시_예외_발생() {
        given(reservationRepository.getById(999L)).willThrow(RoomEscapeException.class);
        Assertions.assertThatThrownBy(() -> reservationService.cancel(999L, MEMBER_ID))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 존재하지_않는_시간으로_예약시_예외() {
        given(assembler.from(any(ReservationCreateCommand.class))).willThrow(RoomEscapeException.class);
        ReservationCreateRequest request = new ReservationCreateRequest(MEMBER_ID, LocalDate.parse("2026-05-03"), 999L, 1L);
        Assertions.assertThatThrownBy(() -> reservationService.reserve(ReservationCreateCommand.from(request)))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 지나간_날짜로_예약_시_예외가_발생해야_한다() {
        given(assembler.from(any(ReservationCreateCommand.class))).willThrow(new RoomEscapeException(DomainErrorCode.PAST_DATE, "test"));
        ReservationCreateRequest request = new ReservationCreateRequest(MEMBER_ID, LocalDate.parse("2026-04-05"), 1L, 1L);
        Assertions.assertThatThrownBy(() -> reservationService.reserve(ReservationCreateCommand.from(request)))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 미래로_예약하면_성공해야_한다() {
        Reservation assembled = Reservation.create(DUMMY_MEMBER, DUMMY_SLOT);
        given(assembler.from(any(ReservationCreateCommand.class))).willReturn(assembled);
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of());
        given(reservationRepository.save(any())).willReturn(DUMMY);
        Assertions.assertThatNoException().isThrownBy(() -> reservationService.reserve(
                ReservationCreateCommand.from(new ReservationCreateRequest(MEMBER_ID, LocalDate.parse("2026-04-05"), 1L, 1L))));
    }

    @Test
    void 예약_생성시_이미_예약된_예약이면_예외가_발생한다() {
        Member zezeForConflict = new Member(1L, "zeze");
        Reservation assembled = Reservation.create(zezeForConflict, DUMMY_SLOT);
        given(assembler.from(any(ReservationCreateCommand.class))).willReturn(assembled);
        Reservation existingReservation = new Reservation(1L, zezeForConflict, Status.APPROVED, DUMMY_SLOT);
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of(existingReservation));

        Assertions.assertThatThrownBy(() -> reservationService.reserve(
                        ReservationCreateCommand.from(new ReservationCreateRequest(1L, LocalDate.parse("2099-04-05"), 1L, 1L))))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_수정시_ID가_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(MEMBER_ID, LocalDate.parse("2099-04-06"), 1L, 1L);
        given(reservationRepository.getById(999L)).willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));
        Assertions.assertThatThrownBy(() -> reservationService.update(ReservationUpdateCommand.from(request), 999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_수정시_과거_날짜의_예약이면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(MEMBER_ID, LocalDate.parse("2000-04-06"), 1L, 1L);
        given(reservationRepository.getById(1L)).willReturn(DUMMY);
        given(assembler.from(any(ReservationUpdateCommand.class))).willThrow(new RoomEscapeException(DomainErrorCode.PAST_DATE, "test"));

        Assertions.assertThatThrownBy(() -> reservationService.update(ReservationUpdateCommand.from(request), 1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_수정시_시간을_찾을_수_없으면_예외가_발생한다() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(MEMBER_ID, LocalDate.parse("2099-04-06"), 1L, 1L);
        given(reservationRepository.getById(1L)).willReturn(DUMMY);
        given(assembler.from(any(ReservationUpdateCommand.class))).willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));
        Assertions.assertThatThrownBy(() -> reservationService.update(ReservationUpdateCommand.from(request), 1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_수정시_사용_불가능한_날짜가_들어오면_예외가_발생한다() {
        ReservationTime reservationTime = ReservationTime.load(1L, LocalTime.parse("11:00"));
        Theme theme = Theme.load(1L, "any", "any", URL);
        Slot newSlot = Slot.load(2L, LocalDate.parse("2099-04-06"), reservationTime, theme);
        ReservationUpdateRequest request = new ReservationUpdateRequest(MEMBER_ID, LocalDate.parse("2099-04-06"), 1L, 1L);

        given(reservationRepository.getById(1L)).willReturn(DUMMY);
        given(assembler.from(any(ReservationUpdateCommand.class))).willReturn(Reservation.create(DUMMY_MEMBER, newSlot));
        Reservation conflicting = new Reservation(2L, DUMMY_MEMBER, Status.APPROVED, newSlot);
        given(reservationRepository.findBySlot_Id(2L)).willReturn(List.of(conflicting));

        Assertions.assertThatThrownBy(() -> reservationService.update(ReservationUpdateCommand.from(request), 1L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_수정시_같은_슬롯이면_자기_자신이므로_중복이_아니어야_한다() {
        long timeId = 1L;
        long themeId = 1L;

        ReservationTime time = ReservationTime.load(timeId, LocalTime.of(10, 0));
        Theme theme = Theme.load(themeId, "any", "any", URL);
        Slot slot = Slot.load(1L, LocalDate.of(2099, 1, 1), time, theme);
        Reservation existing = new Reservation(1L, DUMMY_MEMBER, Status.APPROVED, slot);

        ReservationUpdateRequest request = new ReservationUpdateRequest(MEMBER_ID, LocalDate.of(2099, 1, 1), timeId, themeId);

        given(reservationRepository.getById(1L)).willReturn(existing);
        given(assembler.from(any(ReservationUpdateCommand.class))).willReturn(Reservation.create(DUMMY_MEMBER, slot));
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of(existing));

        assertThatCode(() -> reservationService.update(ReservationUpdateCommand.from(request), 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약_삭제_시_ID가_존재하지_않으면_예외가_발생한다() {
        given(reservationRepository.getById(NOT_EXISTS_ID)).willThrow(RoomEscapeException.class);
        Assertions.assertThatThrownBy(() -> reservationService.cancel(NOT_EXISTS_ID, MEMBER_ID))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_회원ID가_다르면_예외가_발생한다() {
        givenNow(LocalDateTime.of(2026, 1, 1, 0, 0));
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.getById(EXISTS_ID)).willReturn(reservation);
        Assertions.assertThatThrownBy(() -> reservationService.cancel(EXISTS_ID, 999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약_삭제_시_문제가_없으면_삭제되어야_한다() {
        givenNow(LocalDateTime.of(2026, 1, 1, 0, 0));
        Reservation reservation = RoomEscapeFixture.reservation();
        given(reservationRepository.getById(EXISTS_ID)).willReturn(reservation);
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of());
        assertThatCode(() -> reservationService.cancel(EXISTS_ID, reservation.getMember().getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void 승인된_예약_취소_시_첫_번째_대기자가_승급된다() {
        givenNow(LocalDateTime.of(2026, 1, 1, 0, 0));
        Reservation approved = new Reservation(1L, DUMMY_MEMBER, Status.APPROVED, DUMMY_SLOT);
        Reservation waiting = new Reservation(2L, new Member(2L, "대기자"), Status.WAITING, DUMMY_SLOT);

        given(reservationRepository.getById(1L)).willReturn(approved);
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of(waiting));

        reservationService.cancel(1L, MEMBER_ID);

        assertThat(waiting.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void 대기_예약_취소_시_승급이_일어나지_않는다() {
        givenNow(LocalDateTime.of(2026, 1, 1, 0, 0));
        Reservation waiting = new Reservation(1L, DUMMY_MEMBER, Status.WAITING, DUMMY_SLOT);
        given(reservationRepository.getById(1L)).willReturn(waiting);

        reservationService.cancel(1L, MEMBER_ID);

        verify(reservationRepository, never()).findBySlot_Id(any());
    }

    @Test
    void 승인된_예약의_슬롯_변경_시_기존_슬롯의_첫_번째_대기자가_승급된다() {
        Slot newSlot = Slot.load(2L, LocalDate.of(2099, 6, 1), ReservationTime.load(1L, LocalTime.of(11, 0)), Theme.load(1L, "any", "any", URL));
        Reservation existing = new Reservation(1L, DUMMY_MEMBER, Status.APPROVED, DUMMY_SLOT);
        Reservation waitingInOldSlot = new Reservation(3L, new Member(3L, "대기자"), Status.WAITING, DUMMY_SLOT);

        given(reservationRepository.getById(1L)).willReturn(existing);
        given(assembler.from(any(ReservationUpdateCommand.class))).willReturn(Reservation.create(DUMMY_MEMBER, newSlot));
        given(reservationRepository.findBySlot_Id(2L)).willReturn(List.of());
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of(waitingInOldSlot));

        reservationService.update(ReservationUpdateCommand.from(new ReservationUpdateRequest(MEMBER_ID, LocalDate.of(2099, 6, 1), 1L, 1L)), 1L);

        assertThat(waitingInOldSlot.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void 승인된_예약의_슬롯_미변경_시_승급이_일어나지_않는다() {
        Reservation existing = new Reservation(1L, DUMMY_MEMBER, Status.APPROVED, DUMMY_SLOT);

        given(reservationRepository.getById(1L)).willReturn(existing);
        given(assembler.from(any(ReservationUpdateCommand.class))).willReturn(Reservation.create(DUMMY_MEMBER, DUMMY_SLOT));
        given(reservationRepository.findBySlot_Id(1L)).willReturn(List.of(existing));

        assertThatCode(() -> reservationService.update(
                ReservationUpdateCommand.from(new ReservationUpdateRequest(MEMBER_ID, LocalDate.of(2099, 1, 1), 1L, 1L)), 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void 단건_조회시_존재하는_ID면_결과를_반환한다() {
        given(reservationRepository.getByIdWithRank(EXISTS_ID)).willReturn(new ReservationWithRank(DUMMY, 0L));
        ReservationWithRank result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.reservation().getId()).isEqualTo(EXISTS_ID);
    }

    @Test
    void 단건_조회시_존재하지_않는_ID면_예외가_발생한다() {
        given(reservationRepository.getByIdWithRank(NOT_EXISTS_ID)).willThrow(new RoomEscapeException(DomainErrorCode.RESOURCE_NOT_FOUND, "test"));
        Assertions.assertThatThrownBy(() -> reservationService.find(NOT_EXISTS_ID))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void memberId_없이_목록_조회시_전체_예약을_반환한다() {
        given(reservationRepository.findAll()).willReturn(List.of(DUMMY));

        Reservations results = reservationService.findAll(null);

        Assertions.assertThat(results.getValues()).hasSize(1);
        Assertions.assertThat(results.getValues().get(0).getId()).isEqualTo(EXISTS_ID);
    }

    @Test
    void memberId로_목록_조회시_해당_회원의_예약만_반환한다() {
        given(memberRepository.getById(MEMBER_ID)).willReturn(DUMMY_MEMBER);
        given(reservationRepository.findAllByMember(DUMMY_MEMBER)).willReturn(List.of(DUMMY));

        Reservations results = reservationService.findAll(MEMBER_ID);

        Assertions.assertThat(results.getValues()).hasSize(1);
        Assertions.assertThat(results.getValues().get(0).getMember().getName()).isEqualTo(NAME);
    }

    @Test
    void 첫번째_예약은_승인_상태이다() {
        given(reservationRepository.getByIdWithRank(EXISTS_ID)).willReturn(new ReservationWithRank(DUMMY, 0L));
        ReservationWithRank result = reservationService.find(EXISTS_ID);

        Assertions.assertThat(result.reservation().getStatus()).isEqualTo(Status.APPROVED);
    }
}
