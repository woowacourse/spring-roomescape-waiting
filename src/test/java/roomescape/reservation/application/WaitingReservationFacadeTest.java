package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.DuplicateException;
import roomescape.reservation.application.dto.SimpleWaitingReservationResponse;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationViewQueryService;
import roomescape.reservation.application.service.WaitingReservationCommandService;
import roomescape.reservation.application.service.WaitingReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.WaitingReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WaitingReservationFacadeTest {

    @Mock
    private ReservationViewQueryService reservationViewQueryService;

    @Mock
    private WaitingReservationCommandService waitingReservationCommandService;

    @Mock
    private WaitingReservationQueryService waitingReservationQueryService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private WaitingReservationFacadeImpl waitingReservationFacade;

    @Test
    @DisplayName("모든 예약 대기를 조회할 수 있다")
    void getAll() {
        //given
        List<WaitingReservation> waiting = List.of(
                createWaiting(1L, 1),
                createWaiting(2L, 2)
        );
        given(waitingReservationQueryService.getAll()).willReturn(waiting);

        List<User> users = List.of(createUser(1L));
        given(userQueryService.getAllByIds(anyList())).willReturn(users);

        //when
        List<WaitingReservationResponse> result = waitingReservationFacade.getAll();

        //then
        assertThat(result).hasSize(2);
        then(waitingReservationQueryService).should(times(1)).getAll();
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 승격한다")
    void promotion() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        Reservation reservation = createReservation(1L);
        given(userQueryService.getById(any())).willReturn(createUser(1L));
        given(reservationCommandService.create(any())).willReturn(reservation);
        //when
        ReservationResponse result = waitingReservationFacade.promotion(1L, request);

        //then
        assertThat(result).isNotNull();
        then(reservationCommandService).should(times(1)).create(any());
        then(waitingReservationCommandService).should(times(1)).delete(any());
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 생성할 수 있다")
    void create() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        User user = createUser(1L);
        WaitingReservation waitingReservation = createWaiting(1L, 1);

        given(userQueryService.getById(any())).willReturn(user);
        given(reservationViewQueryService.existsByParams(any(), any())).willReturn(false);
        given(waitingReservationCommandService.create(any())).willReturn(waitingReservation);

        //when
        SimpleWaitingReservationResponse result = waitingReservationFacade.create(request);

        //then
        assertThat(result).isNotNull();
        then(userQueryService).should(times(1)).getById(request.userId());
        then(reservationViewQueryService).should(times(1)).existsByParams(any(), any());
        then(waitingReservationCommandService).should(times(1)).create(any());
    }

    @Test
    @DisplayName("이미 존재하는 예약과 동일한 조건으로 대기 예약 생성 시 예외가 발생한다")
    void createWhenDuplicateReservationExists_ThrowsException() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        User user = createUser(1L);

        given(userQueryService.getById(any())).willReturn(user);
        given(reservationViewQueryService.existsByParams(any(), any())).willReturn(true);

        //when & then
        assertThatThrownBy(() -> waitingReservationFacade.create(request))
                .isInstanceOf(DuplicateException.class);

        then(userQueryService).should(times(1)).getById(request.userId());
        then(reservationViewQueryService).should(times(1)).existsByParams(any(), any());
        then(waitingReservationCommandService).should(times(0)).create(any());
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 삭제할 수 있다")
    void delete() {
        //given
        Long waitingReservationId = 1L;

        //when
        waitingReservationFacade.delete(waitingReservationId);

        //then
        then(waitingReservationCommandService).should(times(1)).delete(waitingReservationId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 예약 대기 생성 시 예외가 발생한다")
    void createWhenUserNotFound() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();

        given(userQueryService.getById(any())).willThrow(new RuntimeException("User not found"));

        //when & then
        assertThatThrownBy(() -> waitingReservationFacade.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        then(userQueryService).should(times(1)).getById(request.userId());
        then(reservationViewQueryService).should(times(0)).existsByParams(any(), any());
        then(waitingReservationCommandService).should(times(0)).create(any());
    }

    private Reservation createReservation(Long id) {
        return new Reservation(
                id,
                1L,
                ReservationDate.from(LocalDate.now().plusDays(1)),
                new ReservationTime(
                        1L,
                        LocalTime.of(15, 0)
                ),
                new Theme(
                        1L,
                        ThemeName.from("테스트테마"),
                        ThemeDescription.from("설명"),
                        ThemeThumbnail.from("thumbnail.jpg")
                )
        );
    }

    private WaitingReservation createWaiting(Long id, int waitingNumber) {
        return new WaitingReservation(
                id,
                1L,
                waitingNumber,
                ReservationDate.from(LocalDate.now().plusDays(1)),
                new ReservationTime(
                        1L,
                        LocalTime.of(15, 0)
                ),
                new Theme(
                        1L,
                        ThemeName.from("테스트테마"),
                        ThemeDescription.from("설명"),
                        ThemeThumbnail.from("thumbnail.jpg")
                )
        );
    }

    private User createUser(Long id) {
        return new User(
                id,
                UserName.from("테스트"),
                Email.from("email@email.com"),
                Password.fromEncoded("password"),
                UserRole.NORMAL);
    }

    private CreateReservationWithUserIdWebRequest createCreateRequest() {
        return new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                1L, 1L, 1L
        );
    }
}
