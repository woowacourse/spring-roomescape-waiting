package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.DomainTerm;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchWebRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReservationFacadeTest {

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @Mock
    private UserQueryService userQueryService;

    @InjectMocks
    private ReservationFacadeImpl reservationFacade;

    @Test
    @DisplayName("모든 예약을 조회할 수 있다")
    void getAll() {
        //given
        List<Reservation> reservations = List.of(
                createReservation(1L),
                createReservation(2L)
        );
        given(reservationQueryService.getAll()).willReturn(reservations);

        List<User> users = List.of(createUser(1L));
        given(userQueryService.getAllByIds(anyList())).willReturn(users);

        //when
        List<ReservationResponse> result = reservationFacade.getAll();

        //then
        assertThat(result).hasSize(2);
        then(reservationQueryService).should(times(1)).getAll();
    }

    @Test
    @DisplayName("빈 예약 목록을 조회할 수 있다")
    void getAllWhenEmpty() {
        //given
        given(reservationQueryService.getAll()).willReturn(List.of());
        List<User> users = List.of(createUser(1L));
        given(userQueryService.getAllByIds(anyList())).willReturn(users);
        //when
        List<ReservationResponse> result = reservationFacade.getAll();

        //then
        assertThat(result).isEmpty();
        then(reservationQueryService).should(times(1)).getAll();
    }

    @Test
    @DisplayName("특정 사용자의 모든 예약을 조회할 수 있다")
    void getAllByUserId() {
        //then
        Long userId = 1L;
        given(userQueryService.getById(any())).willReturn(createUser(1L));
        List<Reservation> reservations = List.of(createReservation(1L));
        given(reservationQueryService.getAllByUserId(any(UserId.class))).willReturn(reservations);

        //when
        List<ReservationResponse> result = reservationFacade.getAllByUserId(userId);

        //then
        assertThat(result).hasSize(1);
        then(reservationQueryService).should(times(1)).getAllByUserId(any(UserId.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 예약 조회 시 예외가 발생한다")
    void getAllByUserIdWithNonExistentUserId() {
        //given
        Long nonExistentUserId = 9999L;
        given(userQueryService.getById(any())).willReturn(createUser(nonExistentUserId));
        given(reservationQueryService.getAllByUserId(any(UserId.class)))
                .willThrow(new NotFoundException(DomainTerm.USER_ID));

        //when
        //then
        assertThatThrownBy(() -> reservationFacade.getAllByUserId(nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[USER_ID] not found");
    }

    @Test
    @DisplayName("검색 조건으로 예약을 조회할 수 있다")
    void getByParams() {
        //given
        ReservationSearchWebRequest searchRequest = createSearchRequest();
        List<Reservation> reservations = List.of(createReservation(1L));
        given(reservationQueryService.getByParams(any())).willReturn(reservations);

        List<User> users = List.of(createUser(1L));
        given(userQueryService.getAllByIds(anyList())).willReturn(users);

        //when
        List<ReservationResponse> result = reservationFacade.getByParams(searchRequest);

        //then
        assertThat(result).hasSize(1);
        then(reservationQueryService).should(times(1)).getByParams(any());
    }

    @Test
    @DisplayName("예약을 성공적으로 생성한다")
    void create() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        Reservation reservation = createReservation(1L);
        given(userQueryService.getById(any())).willReturn(createUser(1L));
        given(reservationCommandService.create(any())).willReturn(reservation);

        //when
        ReservationResponse result = reservationFacade.create(request);

        //then
        assertThat(result).isNotNull();
        then(reservationCommandService).should(times(1)).create(any());
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID로 예약 생성 시 예외가 발생한다")
    void createWithNonExistentThemeId() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        given(reservationCommandService.create(any()))
                .willThrow(new NotFoundException(DomainTerm.THEME));

        //when
        //then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[THEME] not found");
    }

    @Test
    @DisplayName("존재하지 않는 시간 ID로 예약 생성 시 예외가 발생한다")
    void createWithNonExistentTimeId() {
        //given
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        given(reservationCommandService.create(any()))
                .willThrow(new NoSuchElementException());

        //when
        //then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 예약 생성 시 예외가 발생한다")
    void createWithNonExistentUserId() {
        //when
        CreateReservationWithUserIdWebRequest request = createCreateRequest();
        given(reservationCommandService.create(any()))
                .willThrow(new NotFoundException(DomainTerm.USER_ID));

        //when
        //then
        assertThatThrownBy(() -> reservationFacade.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[USER_ID] not found");
    }

    @Test
    @DisplayName("예약을 성공적으로 삭제한다")
    void delete() {
        //given
        Long reservationId = 1L;

        //when
        reservationFacade.delete(reservationId);

        //then
        then(reservationCommandService).should(times(1)).delete(any(ReservationId.class));
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID로 삭제 시 예외가 발생한다")
    void deleteWithNonExistentReservationId() {
        //given
        Long nonExistentReservationId = 9999L;
        willThrow(new NotFoundException(DomainTerm.RESERVATION))
                .given(reservationCommandService).delete(any(ReservationId.class));

        //when
        //then
        assertThatThrownBy(() -> reservationFacade.delete(nonExistentReservationId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("[RESERVATION] not found");
    }

    private Reservation createReservation(Long id) {
        return Reservation.withId(
                ReservationId.from(id),
                UserId.from(1L),
                ReservationDate.from(LocalDate.now().plusDays(1)),
                ReservationTime.withId(
                        ReservationTimeId.from(1L),
                        LocalTime.of(15, 0)
                ),
                Theme.withId(
                        ThemeId.from(1L),
                        ThemeName.from("테스트테마"),
                        ThemeDescription.from("설명"),
                        ThemeThumbnail.from("thumbnail.jpg")
                )
        );
    }

    private ReservationSearchWebRequest createSearchRequest() {
        return new ReservationSearchWebRequest(
                1L, 1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(1)
        );
    }

    private User createUser(Long Id) {
        return User.withId(
                UserId.from(Id),
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
