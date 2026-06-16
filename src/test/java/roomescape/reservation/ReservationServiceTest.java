package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.member.application.port.out.MemberRepository;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.application.SlotAssembler;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.port.out.WaitingRepository;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingPromotionPolicy;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-01T00:00:00Z"),
            ZoneId.systemDefault()
    );
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private SlotAssembler slotAssembler;
    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private WaitingPromotionPolicy waitingPromotionPolicy;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                memberRepository,
                waitingRepository,
                slotAssembler,
                waitingPromotionPolicy,
                clock
        );
    }

    @Test
    @DisplayName("나의 예약 목록 조회 시 여러 슬롯의 대기열을 한 번에 조회해 대기 순번을 계산한다.")
    void calculates_waiting_order_with_bulk_waiting_line_lookup_for_my_reservations() {
        WaitingDetailProjection firstWaitingDetail = waitingDetail(11L, 10L);
        WaitingDetailProjection secondWaitingDetail = waitingDetail(22L, 20L);

        when(reservationRepository.findAllReservationDetailsByMemberId(MEMBER_ID)).thenReturn(List.of());
        when(waitingRepository.findAllWaitingDetailsByMemberId(MEMBER_ID))
                .thenReturn(List.of(firstWaitingDetail, secondWaitingDetail));
        when(waitingRepository.findAllBySlotIds(List.of(10L, 20L)))
                .thenReturn(List.of(
                        roomescape.TestFixtures.waiting(11L, MEMBER_ID, 10L),
                        roomescape.TestFixtures.waiting(9L, OTHER_MEMBER_ID, 10L),
                        roomescape.TestFixtures.waiting(22L, MEMBER_ID, 20L),
                        roomescape.TestFixtures.waiting(21L, OTHER_MEMBER_ID, 20L)
                ));

        List<ReservationDetailFindResponse> responses = reservationService.findMyReservations(MEMBER_ID);

        assertThat(responses).extracting(ReservationDetailFindResponse::waitingOrder)
                .containsExactly(2L, 2L);
        verify(waitingRepository).findAllBySlotIds(List.of(10L, 20L));
        verify(waitingRepository, never()).findAllBySlotIdOrderByIdForUpdate(anyLong());
    }

    private WaitingDetailProjection waitingDetail(Long waitingId, Long slotId) {
        return new WaitingDetailProjection(
                waitingId,
                slotId,
                "member",
                LocalDate.of(2026, 6, 1),
                1L,
                "theme",
                "description",
                "thumbnail",
                1L,
                LocalTime.of(10, 0)
        );
    }

    @Test
    @DisplayName("유저는 본인 예약 삭제에 성공한다.")
    void user_deletes_own_reservation_successfully() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(waitingRepository.findAllBySlotIdOrderByIdForUpdate(oldReservation.getSlotId())).thenReturn(List.of());

        assertThatCode(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    private Reservation reservation(
            Long reservationId,
            Long memberId,
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalTime startAt,
            Long slotId
    ) {
        return roomescape.TestFixtures.reservation(reservationId, memberId, slot(slotId, date, themeId, timeId, startAt));
    }

    private Slot slot(Long slotId, LocalDate date, Long themeId, Long timeId, LocalTime startAt) {
        return Slot.of(
                slotId,
                date,
                new ReservationTime(timeId, startAt),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("매니저는 예약 삭제에 성공한다.")
    void manager_deletes_reservation_successfully() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(waitingRepository.findAllBySlotIdOrderByIdForUpdate(oldReservation.getSlotId())).thenReturn(List.of());

        assertThatCode(() -> reservationService.deleteById(reservationId))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("유저는 타인 예약 삭제를 할 수 없다.")
    void user_cannot_delete_other_members_reservation() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, OTHER_MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("예약을 삭제하면 같은 슬롯의 첫 번째 대기가 예약으로 승격된다.")
    void deleting_reservation_promotes_first_waiting_in_same_slot() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        Waiting firstWaiting = roomescape.TestFixtures.waiting(1L, 2L, oldReservation.getSlotId());
        Waiting secondWaiting = roomescape.TestFixtures.waiting(2L, 3L, oldReservation.getSlotId());
        Reservation promotedReservation = Reservation.create(firstWaiting.getMember(), oldReservation.getSlot());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(waitingRepository.findAllBySlotIdOrderByIdForUpdate(oldReservation.getSlotId()))
                .thenReturn(List.of(firstWaiting, secondWaiting));
        when(waitingPromotionPolicy.promote(firstWaiting, oldReservation.getSlot()))
                .thenReturn(promotedReservation);

        reservationService.deleteById(reservationId);

        InOrder inOrder = inOrder(reservationRepository, waitingPromotionPolicy, waitingRepository);
        inOrder.verify(reservationRepository).deleteById(reservationId);
        inOrder.verify(waitingPromotionPolicy).promote(firstWaiting, oldReservation.getSlot());
        inOrder.verify(reservationRepository).save(promotedReservation);
        inOrder.verify(waitingRepository).deleteById(firstWaiting.getId());
        verify(waitingPromotionPolicy, never()).promote(secondWaiting, oldReservation.getSlot());
    }

    @Test
    @DisplayName("승격 예약 저장에 실패하면 승격된 대기를 삭제하지 않는다.")
    void failed_promotion_reservation_save_keeps_promoted_waiting() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        Waiting firstWaiting = roomescape.TestFixtures.waiting(1L, 2L, oldReservation.getSlotId());
        Reservation promotedReservation = Reservation.create(firstWaiting.getMember(), oldReservation.getSlot());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(waitingRepository.findAllBySlotIdOrderByIdForUpdate(oldReservation.getSlotId()))
                .thenReturn(List.of(firstWaiting));
        when(waitingPromotionPolicy.promote(firstWaiting, oldReservation.getSlot()))
                .thenReturn(promotedReservation);
        doThrow(new IllegalStateException()).when(reservationRepository).save(promotedReservation);

        assertThatThrownBy(() -> reservationService.deleteById(reservationId))
                .isInstanceOf(IllegalStateException.class);

        verify(waitingRepository, never()).deleteById(firstWaiting.getId());
    }

}
