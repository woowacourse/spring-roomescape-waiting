package roomescape.reservation.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.Fixtures;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("예약 서비스")
class ReservationServiceTest {

    private ReservationService reservationService;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private MemberRepository memberRepository;
    private Long id;
    private String name;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        this.reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
        this.id = 1L;
        this.name = "클로버";
        this.date = LocalDate.now().plusMonths(6);
    }

    @DisplayName("예약 서비스는 예약들을 조회한다.")
    @Test
    void readReservations() {
        // given
        Mockito.when(reservationRepository.findAll())
                .thenReturn(List.of(Fixtures.reservationFixture));

        // when
        List<ReservationResponse> reservations = reservationService.readReservations();

        // then
        assertThat(reservations.size()).isEqualTo(1);
    }

    @DisplayName("예약 서비스는 id에 맞는 예약을 조회한다.")
    @Test
    void readReservation() {
        // given
        Mockito.when(reservationRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationFixture));

        // when
        ReservationResponse reservation = reservationService.readReservation(id);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.member().name()).isEqualTo(name);
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 예약을 생성한다.")
    @Test
    void createReservation() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.themeFixture));
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixture));
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, 1L, 1L);
        Mockito.when(reservationRepository.save(any()))
                .thenReturn(Fixtures.reservationFixture);

        // when
        ReservationResponse reservation = reservationService.createReservation(request);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.member().name()).isEqualTo(name);
        softAssertions.assertThat(reservation.time().getStartAt()).isEqualTo(LocalTime.of(10, 10));
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 사용자 예약을 생성한다.")
    @Test
    void createMemberReservation() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.themeFixture));
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixture));

        MemberReservationCreateRequest request = new MemberReservationCreateRequest(date, 1L, 1L);
        LoginMember loginMember = Fixtures.loginMemberFixture;
        Mockito.when(reservationRepository.save(any()))
                .thenReturn(Fixtures.reservationFixture);

        // when
        ReservationResponse reservation = reservationService.createReservation(request, loginMember);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.member().name()).isEqualTo(name);
        softAssertions.assertThat(reservation.time().getStartAt()).isEqualTo(LocalTime.of(10, 10));
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 지난 시점의 예약이 요청되면 예외가 발생한다.")
    @Test
    void validateRequestedTime() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.themeFixture));
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixture));

        LocalDate date = LocalDate.MIN;
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, id, id);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 지난 날짜는 예약할 수 없습니다.");
    }

    @DisplayName("예약 서비스는 중복된 예약 요청이 들어오면 예외가 발생한다.")
    @Test
    void validateIsDuplicated() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.themeFixture));
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixture));
        Mockito.when(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(Fixtures.reservationFixture));
        ReservationCreateRequest request = new ReservationCreateRequest(
                Fixtures.reservationFixture.getMember().getId(),
                Fixtures.reservationFixture.getDate(),
                id,
                id
        );

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 예약입니다.");
    }

    @DisplayName("예약 서비스는 예약 요청에 존재하지 않는 시간이 포함된 경우 예외가 발생한다.")
    @Test
    void createWithNonExistentTime() {
        // given
        Mockito.when(memberRepository.findById(id))
                        .thenReturn(Optional.of(Fixtures.memberFixture));
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request = new ReservationCreateRequest(id, date, id, id);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @DisplayName("예약 서비스는 예약 요청에 존재하지 않는 테마가 포함된 경우 예외가 발생한다.")
    @Test
    void createWithNonExistentTheme() {
        // given
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixture));
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.empty());

        ReservationCreateRequest request = new ReservationCreateRequest(id, date, id, id);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }


    @DisplayName("예약 서비스는 요청받은 테마가 동시간대에 이미 예약된 경우 예외가 발생한다.")
    @Test
    void createWithReservedTheme() {
        // given
        Mockito.when(reservationTimeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.reservationTimeFixture));
        Mockito.when(themeRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.themeFixture));
        Mockito.when(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(Fixtures.reservationFixture));
        Mockito.when(memberRepository.findById(id))
                .thenReturn(Optional.of(Fixtures.memberFixtures.get(0)));
        ReservationCreateRequest request = new ReservationCreateRequest(id, date, id, id);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 예약된 테마입니다.");
    }

    @DisplayName("예약 서비스는 id에 맞는 예약을 삭제한다.")
    @Test
    void deleteReservation() {
        // given
        Mockito.when(reservationRepository.deleteById(id)).
                thenReturn(1);

        // when & then
        assertThatCode(() -> reservationService.deleteReservation(id))
                .doesNotThrowAnyException();
    }
}
