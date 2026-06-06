package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationsResponse;
import roomescape.presentation.reservation.response.UserReservationsResponse;

@DisplayName("예약 서비스")
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2030-01-01T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationSlotRepository slotRepository;

    @Mock
    private UserRepository userRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, slotRepository, userRepository, FIXED_CLOCK);
    }

    @DisplayName("전체 예약 목록을 조회할 수 있다")
    @Test
    void getAllReservations() {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationRepository.findAll()).willReturn(List.of(reservation));

        // when
        ReservationsResponse response = reservationService.getAllReservations();

        // then
        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations()).singleElement()
                .satisfies((Object payload) -> {
                    assertThat(payload).extracting("username").isEqualTo("홍길동");
                    assertThat(payload).extracting("slot.date").isEqualTo(LocalDate.of(2030, 1, 2));
                    assertThat(payload).extracting("slot.theme.name").isEqualTo("도심 탈출");
                });
        verify(reservationRepository, times(1)).findAll();
        verifyNoInteractions(slotRepository, userRepository);
    }

    @DisplayName("사용자 이름으로 예약 목록을 조회할 수 있다")
    @Test
    void getUserReservations() {
        // given
        User user = User.of(10L, "김철수");
        Reservation reservation = Reservation.of(
                1L,
                user,
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 3),
                        ReservationTime.of(30L, LocalTime.of(14, 0)),
                        Theme.of(40L, "미로 탈출", "미로 탈출 설명", "/themes/40")
                ),
                1,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 5)
        );
        given(userRepository.findByName("김철수")).willReturn(Optional.of(user));
        given(reservationRepository.findAllReservationsByUserId(10L)).willReturn(List.of(reservation));

        // when
        UserReservationsResponse response = reservationService.getUserReservations("김철수");

        // then
        assertThat(response.username()).isEqualTo("김철수");
        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations()).singleElement()
                .satisfies((Object payload) -> {
                    assertThat(payload).extracting("slot.date").isEqualTo(LocalDate.of(2030, 1, 3));
                    assertThat(payload).extracting("status").isEqualTo(ReservationStatus.WAITING);
                });
        verify(userRepository, times(1)).findByName("김철수");
        verify(reservationRepository, times(1)).findAllReservationsByUserId(10L);
        verifyNoInteractions(slotRepository);
    }

    @DisplayName("기존 사용자가 있는 경우 예약을 생성할 수 있다")
    @Test
    void createReservationWithExistingUser() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                "홍길동",
                LocalDate.of(2030, 1, 1),
                30L,
                40L
        );
        User user = User.of(10L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(30L, LocalTime.of(13, 0)),
                Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
        );
        Reservation savedReservation = Reservation.of(
                100L,
                user,
                slot,
                null,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        Reservation waitingReservation = Reservation.of(
                101L,
                User.of(11L, "김철수"),
                slot,
                1,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 5)
        );

        given(userRepository.findByName("홍길동")).willReturn(Optional.of(user));
        given(slotRepository.findByScheduleForUpdate(30L, LocalDate.of(2030, 1, 1), 40L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(
                List.of(savedReservation, waitingReservation));

        // when
        ReservationCreateResponse response = reservationService.createReservation(request);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(13, 0));
        assertThat((Object) response.theme()).extracting("name").isEqualTo("도심 탈출");
        verify(userRepository, times(1)).findByName("홍길동");
        verify(userRepository, never()).save(any(User.class));
        verify(slotRepository, times(1)).findByScheduleForUpdate(30L, LocalDate.of(2030, 1, 1), 40L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(20L, 10L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, times(1)).batchUpdate(argThat((List<Reservation> updatedReservations) -> {
            assertThat(updatedReservations).hasSize(2);
            assertThat(updatedReservations.get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
            assertThat(updatedReservations.get(0).getWaitingNumber()).isEqualTo(0);
            assertThat(updatedReservations.get(1).getStatus()).isEqualTo(ReservationStatus.WAITING);
            assertThat(updatedReservations.get(1).getWaitingNumber()).isEqualTo(1);
            return true;
        }));
    }

    @DisplayName("사용자가 없으면 생성 후 재조회해서 예약을 생성할 수 있다")
    @Test
    void createReservationCreatesUserAfterDuplicateKey() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                "박민수",
                LocalDate.of(2030, 1, 1),
                31L,
                41L
        );
        User user = User.of(12L, "박민수");
        ReservationSlot slot = ReservationSlot.of(
                21L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(31L, LocalTime.of(14, 0)),
                Theme.of(41L, "미로 탈출", "미로 탈출 설명", "/themes/41")
        );
        Reservation savedReservation = Reservation.of(
                102L,
                user,
                slot,
                null,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(userRepository.findByName("박민수"))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willThrow(new DuplicateKeyException("duplicate"));
        given(slotRepository.findByScheduleForUpdate(31L, LocalDate.of(2030, 1, 1), 41L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(21L, 12L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(21L)).willReturn(List.of());

        // when
        ReservationCreateResponse response = reservationService.createReservation(request);

        // then
        assertThat(response.id()).isEqualTo(102L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(14, 0));
        verify(userRepository, times(2)).findByName("박민수");
        verify(userRepository, times(1)).save(any(User.class));
        verify(slotRepository, times(1)).findByScheduleForUpdate(31L, LocalDate.of(2030, 1, 1), 41L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(21L, 12L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(21L);
        verify(reservationRepository, never()).batchUpdate(any());
    }

    @DisplayName("이미 같은 슬롯에 예약이 있으면 예외를 던진다")
    @Test
    void createReservationWhenAlreadyExists() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                "홍길동",
                LocalDate.of(2030, 1, 1),
                32L,
                42L
        );
        User user = User.of(13L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                22L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(32L, LocalTime.of(15, 0)),
                Theme.of(42L, "우주 탈출", "우주 탈출 설명", "/themes/42")
        );

        given(userRepository.findByName("홍길동")).willReturn(Optional.of(user));
        given(slotRepository.findByScheduleForUpdate(32L, LocalDate.of(2030, 1, 1), 42L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(22L, 13L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_EXISTS);
        verify(userRepository, times(1)).findByName("홍길동");
        verify(slotRepository, times(1)).findByScheduleForUpdate(32L, LocalDate.of(2030, 1, 1), 42L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(22L, 13L);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
    }

    @DisplayName("관리자가 예약을 삭제할 수 있다")
    @Test
    void deleteReservationByAdmin() {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());
        given(reservationRepository.deleteById(1L)).willReturn(1);

        // when
        reservationService.deleteReservationByAdmin(1L);

        // then
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).deleteById(1L);
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
    }

    @DisplayName("관리자가 찾을 수 없는 예약을 삭제하면 예외를 던진다")
    @Test
    void deleteReservationByAdminWhenNotFound() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservationByAdmin(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
    }

    @DisplayName("사용자가 본인 예약을 취소할 수 있다")
    @Test
    void cancelReservationByUser() {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(slotRepository.findByScheduleForUpdate(30L, LocalDate.of(2030, 1, 2), 40L)).willReturn(
                Optional.of(reservation.getSlot()));
        given(reservationRepository.deleteById(1L)).willReturn(1);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());

        // when
        reservationService.cancelReservationByUser(1L, "홍길동");

        // then
        verify(reservationRepository, times(1)).findById(1L);
        verify(slotRepository, times(1)).findByScheduleForUpdate(30L, LocalDate.of(2030, 1, 2), 40L);
        verify(reservationRepository, times(1)).deleteById(1L);
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다")
    @Test
    void cancelReservationByUserWhenNotOwner() {
        // given
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelReservationByUser(1L, "김철수"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_OWNER);
        verify(reservationRepository, times(1)).findById(1L);
        verify(slotRepository, never()).findByScheduleForUpdate(anyLong(), any(), anyLong());
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
    }
}
