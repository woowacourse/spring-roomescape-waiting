package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.exception.UniqueConstraintViolationException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.presentation.reservation.request.AdminReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
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
        given(reservationRepository.findAllReservationsByUserId(10L)).willReturn(List.of(reservation));

        // when
        UserReservationsResponse response = reservationService.getUserReservations(user);

        // then
        assertThat(response.username()).isEqualTo("김철수");
        assertThat(response.reservations()).hasSize(1);
        assertThat(response.reservations()).singleElement()
                .satisfies((Object payload) -> {
                    assertThat(payload).extracting("slot.date").isEqualTo(LocalDate.of(2030, 1, 3));
                    assertThat(payload).extracting("status").isEqualTo(ReservationStatus.WAITING);
                });
        verify(reservationRepository, times(1)).findAllReservationsByUserId(10L);
        verifyNoInteractions(slotRepository, userRepository);
    }

    @DisplayName("기존 사용자가 있는 경우 예약을 생성할 수 있다")
    @Test
    void createReservationByUserWithExistingUser() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(20L);
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

        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(
                List.of(savedReservation, waitingReservation));

        // when
        ReservationCreateResponse response = reservationService.createReservationByUser(request, user);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(13, 0));
        assertThat((Object) response.theme()).extracting("name").isEqualTo("도심 탈출");
        verify(slotRepository, times(1)).findByIdForUpdate(20L);
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

    @DisplayName("관리자가 예약을 생성할 수 있다")
    @Test
    void createReservationByAdmin() {
        // given
        AdminReservationCreateRequest request = new AdminReservationCreateRequest("홍길동", 20L);
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
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(
                List.of(savedReservation, waitingReservation));

        // when
        ReservationCreateResponse response = reservationService.createReservationByAdmin(request);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(13, 0));
        assertThat((Object) response.theme()).extracting("name").isEqualTo("도심 탈출");
        verify(userRepository, times(1)).findByName("홍길동");
        verify(slotRepository, times(1)).findByIdForUpdate(20L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(20L, 10L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, times(1)).batchUpdate(any());
    }

    @DisplayName("관리자는 과거 시간에도 예약을 생성할 수 있다")
    @Test
    void createReservationByAdminInPast() {
        // given
        AdminReservationCreateRequest request = new AdminReservationCreateRequest("홍길동", 20L);
        User user = User.of(10L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                20L,
                LocalDate.of(2029, 12, 31),
                ReservationTime.of(30L, LocalTime.of(9, 0)),
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

        given(userRepository.findByName("홍길동")).willReturn(Optional.of(user));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of(savedReservation));

        // when
        ReservationCreateResponse response = reservationService.createReservationByAdmin(request);

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2029, 12, 31));
        assertThat(response.startAt()).isEqualTo(LocalTime.of(9, 0));
        verify(userRepository, times(1)).findByName("홍길동");
        verify(slotRepository, times(1)).findByIdForUpdate(20L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(20L, 10L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationRepository, times(1)).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, times(1)).batchUpdate(any());
    }

    @DisplayName("예약 저장 중 유니크 제약 위반이 발생하면 이미 예약된 시간으로 처리한다")
    @Test
    void createReservationByUserWhenSaveDuplicate() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(20L);
        User user = User.of(10L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(30L, LocalTime.of(13, 0)),
                Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
        );

        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willThrow(new UniqueConstraintViolationException());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservationByUser(request, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_EXISTS);
        verify(slotRepository, times(1)).findByIdForUpdate(20L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(20L, 10L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("예약 슬롯이 없으면 예외를 던진다")
    @Test
    void createReservationByUserWhenSlotNotFound() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(21L);
        given(slotRepository.findByIdForUpdate(21L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservationByUser(request, User.of(99L, "박민수")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SLOT_NOT_FOUND);
        verify(slotRepository, times(1)).findByIdForUpdate(21L);
        verify(reservationRepository, never()).existsBySlotIdAndUserId(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("이미 같은 슬롯에 예약이 있으면 예외를 던진다")
    @Test
    void createReservationByUserWhenAlreadyExists() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(22L);
        User user = User.of(13L, "홍길동");
        ReservationSlot slot = ReservationSlot.of(
                22L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(32L, LocalTime.of(15, 0)),
                Theme.of(42L, "우주 탈출", "우주 탈출 설명", "/themes/42")
        );

        given(slotRepository.findByIdForUpdate(22L)).willReturn(Optional.of(slot));
        given(reservationRepository.existsBySlotIdAndUserId(22L, 13L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservationByUser(request, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_EXISTS);
        verify(slotRepository, times(1)).findByIdForUpdate(22L);
        verify(reservationRepository, times(1)).existsBySlotIdAndUserId(22L, 13L);
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("사용자 예약을 수정할 때 예약 행과 슬롯을 순서대로 잠근다")
    @Test
    void updateReservationByUser() {
        // given
        User user = User.of(10L, "홍길동");
        ReservationSlot currentSlot = ReservationSlot.of(
                30L,
                LocalDate.of(2030, 1, 2),
                ReservationTime.of(31L, LocalTime.of(13, 0)),
                Theme.of(41L, "현재 테마", "현재 테마 설명", "/themes/current")
        );
        ReservationSlot targetSlot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 3),
                ReservationTime.of(32L, LocalTime.of(15, 0)),
                Theme.of(42L, "목표 테마", "목표 테마 설명", "/themes/target")
        );
        Reservation reservation = Reservation.of(
                1L,
                user,
                currentSlot,
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(reservationRepository.findByIdAndUsernameForUpdate(1L, "홍길동")).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(targetSlot));
        given(slotRepository.findByIdForUpdate(30L)).willReturn(Optional.of(currentSlot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.update(any(Reservation.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(30L)).willReturn(List.of());
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());

        // when
        ReservationUpdateResponse updatedResponse = reservationService.updateReservationByUser(
                1L,
                new ReservationUpdateRequest(20L),
                user
        );

        // then
        assertThat(updatedResponse.id()).isEqualTo(1L);
        assertThat(updatedResponse.date()).isEqualTo(LocalDate.of(2030, 1, 3));
        assertThat(updatedResponse.startAt()).isEqualTo(LocalTime.of(15, 0));

        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdAndUsernameForUpdate(1L, "홍길동");
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(slotRepository).findByIdForUpdate(30L);
        inOrder.verify(reservationRepository).existsBySlotIdAndUserId(20L, 10L);
        inOrder.verify(reservationRepository).update(any(Reservation.class));
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(30L);
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("사용자가 같은 슬롯으로 수정하면 예외를 던진다")
    @Test
    void updateReservationByUserWhenSameSlot() {
        // given
        User user = User.of(10L, "홍길동");
        ReservationSlot currentSlot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 2),
                ReservationTime.of(31L, LocalTime.of(13, 0)),
                Theme.of(41L, "현재 테마", "현재 테마 설명", "/themes/current")
        );
        Reservation reservation = Reservation.of(
                1L,
                user,
                currentSlot,
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(reservationRepository.findByIdAndUsernameForUpdate(1L, "홍길동")).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationByUser(
                1L,
                new ReservationUpdateRequest(20L),
                user
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SAME_SLOT);
        verify(reservationRepository, times(1)).findByIdAndUsernameForUpdate(1L, "홍길동");
        verifyNoInteractions(slotRepository, userRepository);
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @DisplayName("관리자 예약을 수정할 때 예약 행과 슬롯을 순서대로 잠근다")
    @Test
    void updateReservationByAdmin() {
        // given
        User user = User.of(10L, "홍길동");
        ReservationSlot currentSlot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 2),
                ReservationTime.of(31L, LocalTime.of(13, 0)),
                Theme.of(41L, "현재 테마", "현재 테마 설명", "/themes/current")
        );
        ReservationSlot targetSlot = ReservationSlot.of(
                30L,
                LocalDate.of(2030, 1, 3),
                ReservationTime.of(32L, LocalTime.of(15, 0)),
                Theme.of(42L, "목표 테마", "목표 테마 설명", "/themes/target")
        );
        Reservation reservation = Reservation.of(
                1L,
                user,
                currentSlot,
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(currentSlot));
        given(slotRepository.findByIdForUpdate(30L)).willReturn(Optional.of(targetSlot));
        given(reservationRepository.existsBySlotIdAndUserId(30L, 10L)).willReturn(false);
        given(reservationRepository.update(any(Reservation.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(30L)).willReturn(List.of());

        // when
        ReservationUpdateResponse updatedResponse = reservationService.updateReservationByAdmin(
                1L,
                new ReservationUpdateRequest(30L)
        );

        // then
        assertThat(updatedResponse.id()).isEqualTo(1L);
        assertThat(updatedResponse.date()).isEqualTo(LocalDate.of(2030, 1, 3));
        assertThat(updatedResponse.startAt()).isEqualTo(LocalTime.of(15, 0));

        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(1L);
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(slotRepository).findByIdForUpdate(30L);
        inOrder.verify(reservationRepository).existsBySlotIdAndUserId(30L, 10L);
        inOrder.verify(reservationRepository).update(any(Reservation.class));
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(20L);
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(30L);
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("관리자가 같은 슬롯으로 수정하면 예외를 던진다")
    @Test
    void updateReservationByAdminWhenSameSlot() {
        // given
        User user = User.of(10L, "홍길동");
        ReservationSlot currentSlot = ReservationSlot.of(
                20L,
                LocalDate.of(2030, 1, 2),
                ReservationTime.of(31L, LocalTime.of(13, 0)),
                Theme.of(41L, "현재 테마", "현재 테마 설명", "/themes/current")
        );
        Reservation reservation = Reservation.of(
                1L,
                user,
                currentSlot,
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationByAdmin(
                1L,
                new ReservationUpdateRequest(20L)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SAME_SLOT);
        verify(reservationRepository, times(1)).findByIdForUpdate(1L);
        verifyNoInteractions(slotRepository, userRepository);
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @DisplayName("관리자는 과거 예약도 수정할 수 있다")
    @Test
    void updateReservationByAdminInPast() {
        // given
        User user = User.of(10L, "홍길동");
        ReservationSlot currentSlot = ReservationSlot.of(
                30L,
                LocalDate.of(2029, 12, 31),
                ReservationTime.of(31L, LocalTime.of(9, 0)),
                Theme.of(41L, "현재 테마", "현재 테마 설명", "/themes/current")
        );
        ReservationSlot targetSlot = ReservationSlot.of(
                20L,
                LocalDate.of(2029, 12, 30),
                ReservationTime.of(32L, LocalTime.of(8, 0)),
                Theme.of(42L, "목표 테마", "목표 테마 설명", "/themes/target")
        );
        Reservation reservation = Reservation.of(
                1L,
                user,
                currentSlot,
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );

        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(targetSlot));
        given(slotRepository.findByIdForUpdate(30L)).willReturn(Optional.of(currentSlot));
        given(reservationRepository.existsBySlotIdAndUserId(20L, 10L)).willReturn(false);
        given(reservationRepository.update(any(Reservation.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(30L)).willReturn(List.of());
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());

        // when
        ReservationUpdateResponse updatedResponse = reservationService.updateReservationByAdmin(
                1L,
                new ReservationUpdateRequest(20L)
        );

        // then
        assertThat(updatedResponse.id()).isEqualTo(1L);
        assertThat(updatedResponse.date()).isEqualTo(LocalDate.of(2029, 12, 30));
        assertThat(updatedResponse.startAt()).isEqualTo(LocalTime.of(8, 0));

        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(1L);
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(slotRepository).findByIdForUpdate(30L);
        inOrder.verify(reservationRepository).existsBySlotIdAndUserId(20L, 10L);
        inOrder.verify(reservationRepository).update(any(Reservation.class));
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(30L);
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
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
        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(reservation.getSlot()));
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());
        given(reservationRepository.deleteById(1L)).willReturn(1);

        // when
        reservationService.deleteReservationByAdmin(1L);

        // then
        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(1L);
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(reservationRepository).deleteById(1L);
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("관리자가 찾을 수 없는 예약을 삭제하면 예외를 던진다")
    @Test
    void deleteReservationByAdminWhenNotFound() {
        // given
        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservationByAdmin(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        verify(reservationRepository, times(1)).findByIdForUpdate(1L);
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
    }

    @DisplayName("관리자가 예약 삭제 중 영향받은 행이 없으면 재계산하지 않는다")
    @Test
    void deleteReservationByAdminWhenDeletedRowZero() {
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
        given(reservationRepository.findByIdForUpdate(1L)).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(reservation.getSlot()));
        given(reservationRepository.deleteById(1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservationByAdmin(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdForUpdate(1L);
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(reservationRepository).deleteById(1L);
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
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
        given(reservationRepository.findByIdAndUsernameForUpdate(1L, "홍길동")).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(reservation.getSlot()));
        given(reservationRepository.deleteById(1L)).willReturn(1);
        given(reservationRepository.findAllBySlotIdOrderByReservedAt(20L)).willReturn(List.of());

        // when
        reservationService.cancelReservationByUser(1L, User.of(10L, "홍길동"));

        // then
        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdAndUsernameForUpdate(1L, "홍길동");
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(reservationRepository).deleteById(1L);
        inOrder.verify(reservationRepository).findAllBySlotIdOrderByReservedAt(20L);
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }

    @DisplayName("본인 예약이 아니면 취소할 수 없다")
    @Test
    void cancelReservationByUserWhenNotOwner() {
        // given
        given(reservationRepository.findByIdAndUsernameForUpdate(1L, "김철수")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.cancelReservationByUser(1L, User.of(11L, "김철수")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        verify(reservationRepository, times(1)).findByIdAndUsernameForUpdate(1L, "김철수");
        verify(reservationRepository, never()).deleteById(anyLong());
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
    }

    @DisplayName("사용자가 예약 취소 중 영향받은 행이 없으면 재계산하지 않는다")
    @Test
    void cancelReservationByUserWhenDeletedRowZero() {
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
        given(reservationRepository.findByIdAndUsernameForUpdate(1L, "홍길동")).willReturn(Optional.of(reservation));
        given(slotRepository.findByIdForUpdate(20L)).willReturn(Optional.of(reservation.getSlot()));
        given(reservationRepository.deleteById(1L)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> reservationService.cancelReservationByUser(1L, User.of(10L, "홍길동")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        InOrder inOrder = inOrder(reservationRepository, slotRepository);
        inOrder.verify(reservationRepository).findByIdAndUsernameForUpdate(1L, "홍길동");
        inOrder.verify(slotRepository).findByIdForUpdate(20L);
        inOrder.verify(reservationRepository).deleteById(1L);
        verify(reservationRepository, never()).findAllBySlotIdOrderByReservedAt(anyLong());
        verify(reservationRepository, never()).batchUpdate(any());
        verifyNoInteractions(userRepository);
    }
}
