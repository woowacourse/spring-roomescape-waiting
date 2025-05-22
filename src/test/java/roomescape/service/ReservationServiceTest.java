package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Member testMember;
    private Theme testTheme;
    private ReservationTime testReservationTime;
    private Reservation testReservation;
    private ReservationCreateRequest createRequest;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testMember = new Member(1L, "Test User", "test@example.com", Role.USER, "password");
        testTheme = new Theme(1L, "Test Theme", "Test Description", "test-thumbnail.jpg");
        
        // 현재 시간보다 미래의 시간으로 설정
        LocalTime futureTime = LocalTime.now().plusHours(2);
        testReservationTime = new ReservationTime(1L, futureTime);
        
        testDate = LocalDate.now().plusDays(1); // 내일 날짜로 설정
        testReservation = new Reservation(1L, testMember, testDate, testReservationTime, testTheme);
        
        createRequest = new ReservationCreateRequest(testDate, 1L, 1L, 1L);
    }

    @Test
    @DisplayName("새로운 예약을 생성할 수 있다")
    void createReservation_WithValidRequest_ReturnsReservationResponse() {
        // given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.of(testReservationTime));
        when(themeRepository.findById(createRequest.themeId())).thenReturn(Optional.of(testTheme));
        when(memberRepository.findById(createRequest.memberId())).thenReturn(Optional.of(testMember));
        when(reservationRepository.findReservationsByDateAndTimeIdAndThemeId(
                createRequest.date(), createRequest.timeId(), createRequest.themeId()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // when
        ReservationResponse response = reservationService.createReservation(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testReservation.getId());
        assertThat(response.date()).isEqualTo(testReservation.getDate());
        assertThat(response.member().name()).isEqualTo(testReservation.getMember().getName());
        assertThat(response.theme().id()).isEqualTo(testReservation.getTheme().getId());
        assertThat(response.time().id()).isEqualTo(testReservation.getTime().getId());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약을 생성하면 예외가 발생한다")
    void createReservation_WithNonExistingTimeId_ThrowsNotFoundException() {
        // given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("예약 시간을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약을 생성하면 예외가 발생한다")
    void createReservation_WithNonExistingThemeId_ThrowsNotFoundException() {
        // given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.of(testReservationTime));
        when(themeRepository.findById(createRequest.themeId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("테마를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 예약을 생성하면 예외가 발생한다")
    void createReservation_WithNonExistingMemberId_ThrowsNotFoundException() {
        // given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.of(testReservationTime));
        when(themeRepository.findById(createRequest.themeId())).thenReturn(Optional.of(testTheme));
        when(memberRepository.findById(createRequest.memberId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이미 예약이 존재하는 시간에 예약을 생성하면 예외가 발생한다")
    void createReservation_WithDuplicateReservation_ThrowsDuplicateContentException() {
        // given
        when(reservationTimeRepository.findById(createRequest.timeId())).thenReturn(Optional.of(testReservationTime));
        when(reservationRepository.findReservationsByDateAndTimeIdAndThemeId(
                createRequest.date(), createRequest.timeId(), createRequest.themeId()))
                .thenReturn(Collections.singletonList(testReservation));

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(createRequest))
                .isInstanceOf(DuplicateContentException.class)
                .hasMessageContaining("이미 예약이 존재합니다");
    }

    @Test
    @DisplayName("모든 예약을 조회할 수 있다")
    void findAllReservationResponses_ReturnsAllReservations() {
        // given
        Reservation reservation1 = testReservation;
        Reservation reservation2 = new Reservation(2L, testMember, testDate.plusDays(1), testReservationTime, testTheme);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findAll()).thenReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findAllReservationResponses();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("특정 기간, 테마, 회원의 예약을 조회할 수 있다")
    void findReservationBetween_ReturnsReservationsInPeriod() {
        // given
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(7);
        long themeId = 1L;
        long memberId = 1L;

        Reservation reservation1 = testReservation;
        Reservation reservation2 = new Reservation(2L, testMember, testDate.plusDays(2), testReservationTime, testTheme);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(from, to, themeId, memberId))
                .thenReturn(reservations);

        // when
        List<ReservationResponse> responses = reservationService.findReservationBetween(themeId, memberId, from, to);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation_WithExistingId_DeletesReservation() {
        // given
        Long reservationId = 1L;
        when(reservationRepository.existsById(reservationId)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(reservationId);

        // when
        reservationService.deleteReservation(reservationId);

        // then
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다")
    void deleteReservation_WithNonExistingId_ThrowsNotFoundException() {
        // given
        Long reservationId = 999L;
        when(reservationRepository.existsById(reservationId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(reservationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("등록된 예약번호만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("로그인한 회원의 예약을 조회할 수 있다")
    void findMyReservations_ReturnsUserReservations() {
        // given
        LoginInfo loginInfo = new LoginInfo(1L, "Test User", "test@example.com", Role.USER);
        
        Reservation reservation1 = testReservation;
        Reservation reservation2 = new Reservation(2L, testMember, testDate.plusDays(2), testReservationTime, testTheme);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findReservationsByMemberId(loginInfo.id())).thenReturn(reservations);

        // when
        List<MyReservationResponse> responses = reservationService.findMyReservations(loginInfo);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).reservationId()).isEqualTo(1L);
        assertThat(responses.get(0).theme()).isEqualTo(testTheme.getName());
        assertThat(responses.get(0).date()).isEqualTo(testDate);
        assertThat(responses.get(0).time()).isEqualTo(testReservationTime.getStartAt());
        assertThat(responses.get(0).status()).isEqualTo("예약");
        
        assertThat(responses.get(1).reservationId()).isEqualTo(2L);
    }
}
