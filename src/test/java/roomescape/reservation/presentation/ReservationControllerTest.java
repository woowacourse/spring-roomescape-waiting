package roomescape.reservation.presentation;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
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
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationSaveRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.*;

@Import(TestWebMvcConfiguration.class)
@WebMvcTest(
        value = ReservationController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class ReservationControllerTest extends ControllerTest {
    private static final Cookie COOKIE = new Cookie("token", "token");

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @MockBean
    private ThemeService themeService;

    @Test
    @DisplayName("예약 목록 GET 요청 시 상태코드 200을 반환한다.")
    void findReservations() throws Exception {
        // given
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, WOOTECO_THEME(), USER_MIA());

        BDDMockito.given(reservationService.findAll())
                .willReturn(List.of(expectedReservation));

        // when & then
        mockMvc.perform(get("/reservations").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberName").value(MIA_NAME))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].theme.name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].date").value(MIA_RESERVATION_DATE.toString()));
    }

    @Test
    @DisplayName("사용자, 테마, 예약 날짜로 예약 목록 검색 요청 시 상태코드 200을 반환한다.")
    void findReservationsByMemberIdAndThemeIdAndDateBetween() throws Exception {
        // given
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, WOOTECO_THEME(), USER_MIA());

        BDDMockito.given(reservationService.findAllByMemberIdAndThemeIdAndDateBetween(anyLong(), anyLong(), any(), any()))
                .willReturn(List.of(expectedReservation));

        // when & then
        mockMvc.perform(get("/reservations/searching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("memberId", String.valueOf(1))
                        .param("themeId", String.valueOf(1))
                        .param("fromDate", MIA_RESERVATION_DATE.toString())
                        .param("toDate", MIA_RESERVATION_DATE.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberName").value(MIA_NAME))
                .andExpect(jsonPath("$[0].time.id").value(1L))
                .andExpect(jsonPath("$[0].time.startAt").value(MIA_RESERVATION_TIME.toString()))
                .andExpect(jsonPath("$[0].theme.name").value(WOOTECO_THEME_NAME))
                .andExpect(jsonPath("$[0].date").value(MIA_RESERVATION_DATE.toString()));
    }

    @Test
    @DisplayName("사용자, 테마, 예약 날짜로 예약 목록 검색 요청 시 검색 조건이 하나라도 없다면 상태코드 400을 반환한다.")
    void findReservationsByMemberIdAndThemeIdAndDateBetweenNotExistingAllParams() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations/searching")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("memberId", String.valueOf(1))
                        .param("themeId", String.valueOf(1))
                        .param("toDate", MIA_RESERVATION_DATE.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("예약 POST 요청 시 상태코드 201을 반환한다.")
    void createReservation() throws Exception {
        // given
        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, 1L, 1L);
        ReservationTime expectedTime = new ReservationTime(1L, MIA_RESERVATION_TIME);
        Theme expectedTheme = WOOTECO_THEME(1L);
        Reservation expectedReservation = MIA_RESERVATION(expectedTime, expectedTheme, USER_MIA(1L));

        BDDMockito.given(reservationService.create(any()))
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
                new ReservationSaveRequest(null, 1L, 1L),
                new ReservationSaveRequest(MIA_RESERVATION_DATE, null, 1L),
                new ReservationSaveRequest(MIA_RESERVATION_DATE, 1L, null)
        );
    }

    @Test
    @DisplayName("올바르지 않은 예약 날짜 형식으로 예약 POST 요청 시 상태코드 400을 반환한다.")
    void createReservationWithInvalidDateFormat() throws Exception {
        // given
        String invalidDateFormatRequest = "{\"date\": \"dfdf\"}";

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
    @DisplayName("존재하지 않는 예약 시간의 예약 POST 요청 시 상태코드 404를 반환한다.")
    void createReservationWithNotExistingTime() throws Exception {
        // given
        Long notExistingTimeId = 1L;
        Long themeId = 1L;
        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, notExistingTimeId, themeId);

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
        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, timeId, notExistingThemeId);

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
    @DisplayName("예약 DELETE 요청 시 상태코드 204를 반환한다.")
    void deleteReservation() throws Exception {
        // given
        BDDMockito.willDoNothing()
                .given(reservationService)
                .delete(anyLong());

        // when & then
        mockMvc.perform(delete("/reservations/{id}", anyLong())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
