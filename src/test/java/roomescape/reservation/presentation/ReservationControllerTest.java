package roomescape.reservation.presentation;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import roomescape.auth.presentation.AdminAuthorizationInterceptor;
import roomescape.auth.presentation.LoginMemberArgumentResolver;
import roomescape.common.ControllerTest;
import roomescape.common.TestWebMvcConfiguration;
import roomescape.global.config.WebMvcConfiguration;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.application.ReservationManageService;
import roomescape.reservation.application.BookingQueryService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.application.WaitingQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.dto.request.ReservationSaveRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.HORROR_THEME;
import static roomescape.TestFixture.HORROR_THEME_NAME;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.MIA_RESERVATION_TIME;
import static roomescape.TestFixture.TEST_ERROR_MESSAGE;
import static roomescape.TestFixture.USER_MIA;
import static roomescape.TestFixture.WOOTECO_THEME;
import static roomescape.TestFixture.WOOTECO_THEME_NAME;
import static roomescape.common.StubLoginMemberArgumentResolver.STUBBED_LOGIN_MEMBER;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

@Import(TestWebMvcConfiguration.class)
@WebMvcTest(
        value = ReservationController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class ReservationControllerTest extends ControllerTest {
    private static final Cookie COOKIE = new Cookie("token", "token");

    @MockBean
    private BookingQueryService bookingQueryService;

    @MockBean
    @Qualifier("bookingManageService")
    private ReservationManageService bookingManageService;

    @MockBean
    @Qualifier("waitingManageService")
    private ReservationManageService waitingManageService;

    @MockBean
    private WaitingQueryService waitingQueryService;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @MockBean
    private ThemeService themeService;

    @Test
    @DisplayName("예약 POST 요청 시 상태코드 201을 반환한다.")
    void createReservation() throws Exception {
        // given
        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, 1L, 1L, "BOOKING");
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Theme expectedTheme = WOOTECO_THEME(1L);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, expectedTheme, USER_MIA(1L), BOOKING);

        BDDMockito.given(bookingManageService.create(any()))
                .willReturn(expectedReservation);
        BDDMockito.given(reservationTimeService.findById(anyLong()))
                .willReturn(expectedTime);
        BDDMockito.given(themeService.findById(anyLong()))
                .willReturn(expectedTheme);

        // when & then
        mockMvc.perform(post("/reservations")
                        .cookie(COOKIE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberName").value(MIA_NAME))
                .andExpect(jsonPath("$.time.id").value(1L))
                .andExpect(jsonPath("$.time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$.date").value(MIA_RESERVATION_DATE.toString()));
    }

    @ParameterizedTest
    @MethodSource(value = "invalidPostRequests")
    @DisplayName("예약 POST 요청 시 하나의 필드라도 없다면 상태코드 400을 반환한다.")
    void createReservationWithNullFieldRequest(ReservationSaveRequest request) throws Exception {
        // when & then
        mockMvc.perform(post("/reservations")
                        .cookie(COOKIE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<ReservationSaveRequest> invalidPostRequests() {
        return Stream.of(
                new ReservationSaveRequest(null, 1L, 1L, BOOKING.getIdentifier()),
                new ReservationSaveRequest(MIA_RESERVATION_DATE, null, 1L, BOOKING.getIdentifier()),
                new ReservationSaveRequest(MIA_RESERVATION_DATE, 1L, null, BOOKING.getIdentifier()),
                new ReservationSaveRequest(MIA_RESERVATION_DATE, 1L, 1L, null)
        );
    }

    @Test
    @DisplayName("올바르지 않은 예약 날짜 형식으로 예약 POST 요청 시 상태코드 400을 반환한다.")
    void createReservationWithInvalidDateFormat() throws Exception {
        // given
        String invalidDateFormatRequest = """
                {
                    "date": "invalid"
                }
                """;

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(COOKIE)
                        .content(invalidDateFormatRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("유효하지 않은 예약 상태 값으로 예약 POST 요청 시 상태코드 400을 반환한다.")
    void createReservationWithInvalidReservationStatus() throws Exception {
        // given
        String invalidDateFormatRequest = """
                {
                    "status": "invalid",
                    "date": "2023-03-08",
                    "timeId": 1,
                    "themeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(COOKIE)
                        .content(invalidDateFormatRequest))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"booking\"", "\" booking\"", "\"BOOKING \"", "\"BOOKINg\""})
    @DisplayName("예약 POST 요청 시 예약 상태 값을 검증하여 유효한 경우 상태코드 201을 반환한다.")
    void crestReservationWithReservationStatusValidation(String statusValue) throws Exception {
        // given
        String request = """
                {
                    "status": "valid",
                    "date": "2023-03-08",
                    "timeId": 1,
                    "themeId": 1
                }
                """;
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Theme expectedTheme = WOOTECO_THEME(1L);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, expectedTheme, USER_MIA(1L), BOOKING);

        BDDMockito.given(bookingManageService.create(any()))
                .willReturn(expectedReservation);
        BDDMockito.given(reservationTimeService.findById(anyLong()))
                .willReturn(expectedTime);
        BDDMockito.given(themeService.findById(anyLong()))
                .willReturn(expectedTheme);

        // when & then
        mockMvc.perform(post("/reservations")
                        .cookie(COOKIE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.replace("\"valid\"", statusValue)))
                .andDo(print())
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("서비스 정책에 맞지 않는 예약 POST 요청 시 상태코드 400을 반환한다.")
    void createDuplicatedReservation() throws Exception {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, BOOKING.getIdentifier());

        BDDMockito.given(themeService.findById(themeId))
                .willReturn(WOOTECO_THEME(themeId));
        BDDMockito.given(reservationTimeService.findById(timeId))
                .willReturn(new ReservationTime(1L, MIA_RESERVATION_TIME));
        BDDMockito.willThrow(new ViolationException(TEST_ERROR_MESSAGE))
                .given(bookingManageService)
                .create(any());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(COOKIE)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간의 예약 POST 요청 시 상태코드 404를 반환한다.")
    void createReservationWithNotExistingTime() throws Exception {
        // given
        Long notExistingTimeId = 1L;
        Long themeId = 1L;
        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, notExistingTimeId, themeId, BOOKING.getIdentifier());

        BDDMockito.given(themeService.findById(themeId))
                .willReturn(WOOTECO_THEME(themeId));
        BDDMockito.willThrow(new NotFoundException(TEST_ERROR_MESSAGE))
                .given(reservationTimeService)
                .findById(anyLong());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(COOKIE)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("존재하지 않는 테마의 예약 POST 요청 시 상태코드 404를 반환한다.")
    void createReservationWithNotExistingTheme() throws Exception {
        // given
        Long timeId = 1L;
        Long notExistingThemeId = 1L;
        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, notExistingThemeId, BOOKING.getIdentifier());

        BDDMockito.given(reservationTimeService.findById(timeId))
                .willReturn(new ReservationTime(1L, MIA_RESERVATION_TIME));
        BDDMockito.willThrow(new NotFoundException(TEST_ERROR_MESSAGE))
                .given(themeService)
                .findById(anyLong());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(COOKIE)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("사용자의 예약 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findMyReservations() throws Exception {
        // given
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, WOOTECO_THEME(), USER_MIA(), BOOKING);
        WaitingReservation expectedWaitingReservation = new WaitingReservation(
                MIA_RESERVATION(expectedTime, HORROR_THEME(), USER_MIA(), WAITING), 0);

        BDDMockito.given(bookingQueryService.findReservationsInBookingByMember(any()))
                .willReturn(List.of(expectedReservation));
        BDDMockito.given(waitingQueryService.findWaitingReservationsWithPreviousCountByMember(any()))
                .willReturn(List.of(expectedWaitingReservation));

        // when & then
        mockMvc.perform(get("/reservations/mine").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].theme").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].date").value(MIA_RESERVATION_DATE.toString()))
                .andExpect(jsonPath("$[0].time").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].status").value("예약"))
                .andExpect(jsonPath("$[1].theme").value(HORROR_THEME_NAME))
                .andExpect(jsonPath("$[1].status").value("1번째 예약대기"));
    }

    @Test
    @DisplayName("사용자 대기 예약 DELETE 요청 시 상태코드 204를 반환한다.")
    void deleteMyWaitingReservation() throws Exception {
        // given
        BDDMockito.willDoNothing()
                .given(waitingManageService)
                .delete(1L, STUBBED_LOGIN_MEMBER);

        // when & then
        mockMvc.perform(delete("/reservations/{id}/waiting", 1L)
                        .cookie(COOKIE))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("서비스 정책에 맞지 않는 대기 예약 DELETE 요청 시 상태코드 400을 반환한다.")
    void deleteMyWaitingReservationWithoutOwnerShip() throws Exception {
        // given
        BDDMockito.willThrow(new ViolationException(TEST_ERROR_MESSAGE))
                .given(waitingManageService)
                .delete(1L, STUBBED_LOGIN_MEMBER);

        // when & then
        mockMvc.perform(delete("/reservations/{id}/waiting", 1L)
                        .cookie(COOKIE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(TEST_ERROR_MESSAGE));
    }
}
