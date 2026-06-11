package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import common.exception.RoomEscapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
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
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
public class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private SlotRepository slotRepository;

    @Nested
    @DisplayName("예약 생성시")
    class Reserve {
        @Test
        @DisplayName("이미 승인 예약이 있는 슬롯에 예약하면 대기로 생성된다")
        void waiting_when_slot_already_has_approved() {
            Slot slot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            String name = "zeze";
            saveReservation(slot, name, Status.APPROVED);

            String newCustomerName = "dalsu";

            ReservationCreateRequest request = createRequestTo(newCustomerName, slot);

            RankedReservation reserved = reservationService.reserve(request, LocalDateTime.now());

            Reservation saved = reservationRepository.findById(reserved.getReservation().getId()).get();
            assertThat(saved.getStatus()).isEqualTo(Status.WAITING);
            assertThat(saved.getName().getValue()).isEqualTo(newCustomerName);
        }

        @Test
        @DisplayName("같은 슬롯에 같은 이름으로 예약하면 예외가 발생한다")
        void reject_duplicate_name_in_same_slot() {
            Slot slot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME, RoomEscapeFixture.FUTURE_DATE);
            String duplicatedName = "zeze";

            saveReservation(slot, duplicatedName, Status.APPROVED);

            ReservationCreateRequest request = createRequestTo(duplicatedName, slot);

            assertThatThrownBy(() -> reservationService.reserve(request, LocalDateTime.now())).isInstanceOf(
                    RoomEscapeException.class);
        }

        private static ReservationCreateRequest createRequestTo(String name, Slot slot) {
            return new ReservationCreateRequest(name,
                    slot.getDate().getValue(), slot.getTime().getId(),
                    slot.getTheme().getId());
        }
    }

    @Nested
    @DisplayName("예약 삭제시")
    class Cancel {
        @Test
        @DisplayName("ID를 찾을 수 없으면 예외가 발생한다")
        void throw_when_id_not_found() {
            assertThatThrownBy(() -> reservationService.cancel(999L, LocalDateTime.now())).isInstanceOf(
                    RoomEscapeException.class);
        }

        @Test
        @DisplayName("과거 예약을 삭제하면 예외가 발생한다")
        void throw_when_cancel_past_reservation() {
            Slot pastSlot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME, RoomEscapeFixture.PAST_DATE);
            Reservation pastTarget = saveReservation(pastSlot, "zeze", Status.APPROVED);

            // when & then
            assertThatThrownBy(() -> reservationService.cancel(pastTarget.getId(), LocalDateTime.MAX))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        @DisplayName("승인 예약이 삭제되면 첫 대기 예약이 승격된다")
        void promote_first_waiting_when_approved_canceled() {
            // given
            Slot slot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            Reservation approvedTarget = saveReservation(slot, "zeze", Status.APPROVED);
            Reservation firstWaiting = saveReservation(slot, "dalsu", Status.WAITING);

            // when
            reservationService.cancel(approvedTarget.getId(), LocalDateTime.MIN);

            // then
            assertThat(reservationRepository.findById(approvedTarget.getId())).isNotPresent();
            assertThat(reservationRepository.findById(firstWaiting.getId()).get().getStatus()).isEqualTo(
                    Status.APPROVED);
        }

        @Test
        @DisplayName("대기 예약이 삭제되면 남은 대기 예약은 그대로여야 한다")
        void keep_remaining_waiting_when_waiting_canceled() {
            // given
            Slot slot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            saveReservation(slot, "zeze", Status.APPROVED);

            Reservation canceledWaiting = saveReservation(slot, "dalsu", Status.WAITING);
            Reservation remainingWaiting = saveReservation(slot, "mingu", Status.WAITING);

            // when
            reservationService.cancel(canceledWaiting.getId(), LocalDateTime.MIN);

            // then
            assertThat(reservationRepository.findById(canceledWaiting.getId())).isNotPresent();
            assertThat(reservationRepository.findById(remainingWaiting.getId()).get().getStatus()).isEqualTo(
                    Status.WAITING);
        }
    }

    @Nested
    @DisplayName("예약 수정시")
    class Update {

        @Test
        @DisplayName("ID를 찾을 수 없으면 예외가 발생한다")
        void throw_when_id_not_found() {
            ReservationUpdateRequest request = RoomEscapeFixture.reservationUpdateRequest();
            assertThatThrownBy(() -> reservationService.update(request, 999L, LocalDateTime.now()))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        @DisplayName("과거 예약을 수정하면 예외가 발생한다")
        void throw_when_update_past_reservation() {
            // given
            String approvedName = "zeze";

            Slot futureSlot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            Reservation approvedReservation = saveReservation(futureSlot, approvedName, Status.APPROVED);

            ReservationUpdateRequest request = RoomEscapeFixture.reservationUpdateRequest();

            // when & then
            assertThatThrownBy(
                    () -> reservationService.update(request, approvedReservation.getId(), LocalDateTime.MAX))
                    .isInstanceOf(RoomEscapeException.class);
        }

        @Test
        @DisplayName("승인된 예약이 변경되면 대기 중인 예약이 승격되어야 한다")
        void promote_waiting_reservation_if_approved() {
            // given
            Slot origin = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            String approvedName = "zeze";
            String waitingName = "dalsu";

            Reservation approvedTarget = saveReservation(origin, approvedName, Status.APPROVED);
            Reservation firstWaiting = saveReservation(origin, waitingName, Status.WAITING);

            ReservationUpdateRequest request = new ReservationUpdateRequest("zeze", LocalDate.MAX,
                    origin.getTime().getId(),
                    origin.getTheme().getId());

            // when
            RankedReservation updated = reservationService.update(request, approvedTarget.getId(),
                    LocalDateTime.now());

            // then
            assertThat(updated.getRank().getValue()).isEqualTo(0);
            assertThat(updated.getReservation().getStatus()).isEqualTo(Status.APPROVED);
            assertThat(reservationRepository.findById(firstWaiting.getId()).get().getStatus())
                    .isEqualTo(Status.APPROVED);

        }

        @Test
        @DisplayName("대기 예약이 옮겨지면 남은 대기 예약은 그대로다")
        void keep_remaining_waiting_when_waiting_moved_out() {
            // given
            Slot origin = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            saveReservation(origin, "zeze", Status.APPROVED);

            String waitingName = "dalsu";
            String waitingName2 = "mingu";

            Reservation waitingTarget = saveReservation(origin, waitingName, Status.WAITING);
            Reservation remainingWaiting = saveReservation(origin, waitingName2, Status.WAITING);

            ReservationUpdateRequest request = new ReservationUpdateRequest(waitingName, LocalDate.MAX,
                    origin.getTime().getId(),
                    origin.getTheme().getId());

            // when
            RankedReservation updated = reservationService.update(request, waitingTarget.getId(),
                    LocalDateTime.now());

            // then
            assertThat(updated.getRank().getValue()).isEqualTo(0);
            assertThat(updated.getReservation().getStatus()).isEqualTo(Status.APPROVED);
            assertThat(reservationRepository.findById(remainingWaiting.getId()).get().getStatus())
                    .isEqualTo(Status.WAITING);
        }

        @Test
        @DisplayName("승인 예약이 빈 슬롯으로 옮겨지면 첫 대기 예약이 승격된다")
        void promote_first_waiting_when_approved_moved_out() {
            // given
            LocalDate targetDate = LocalDate.MAX;
            Slot futureSlot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    RoomEscapeFixture.FUTURE_DATE);
            Slot targetSlot = saveSlot(RoomEscapeFixture.theme(), RoomEscapeFixture.TIME,
                    new ReservationDate(targetDate));

            String approvedName = "zeze";
            String targetSlotName = "dalsu";

            Reservation approvedReservation = saveReservation(futureSlot, approvedName, Status.APPROVED);
            Reservation targetSlotReservation = saveReservation(targetSlot, targetSlotName, Status.APPROVED);

            ReservationUpdateRequest request = new ReservationUpdateRequest(approvedName, targetDate,
                    targetSlot.getTime().getId(),
                    targetSlot.getTheme().getId());

            // when
            RankedReservation updated = reservationService.update(request, approvedReservation.getId(),
                    LocalDateTime.now());

            // then
            assertThat(updated.getRank().getValue()).isEqualTo(1);
            assertThat(updated.getReservation().getStatus()).isEqualTo(Status.WAITING);
        }
    }

    private Reservation saveReservation(Slot slot, String name, Status status) {
        return reservationRepository.save(
                RoomEscapeFixture.reservation().name(name).slot(slot).status(status).build());
    }

    private Slot saveSlot(Theme theme, ReservationTime time, ReservationDate date) {
        Theme targetTheme = themeRepository.save(theme);
        ReservationTime targetTime = reservationTimeRepository.save(time);
        return slotRepository.save(
                RoomEscapeFixture.slot().time(targetTime).theme(targetTheme).date(date).build());
    }
}
