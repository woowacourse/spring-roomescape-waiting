package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.User;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.fixture.Fixtures;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUser;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ReservationService;

@WebMvcTest(controllers = AdminReservationController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class AdminReservationControllerTest {

    private static final User MANAGER = Fixtures.memberWithId(7L, "매니저");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @TestConfiguration
    static class LoginUserIdResolverConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(LoginUser.class)
                            && parameter.getParameterType().equals(User.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    return MANAGER;
                }
            });
        }
    }

    @Test
    @DisplayName("GET /admin/reservations - 서비스가 반환한 목록과 hasNext를 그대로 응답한다")
    void getReservationsRespondsWithServiceListAndHasNext() throws Exception {
        given(reservationService.getReservations(0, 20, null, MANAGER))
                .willReturn(ReservationResponses.of(List.of(Fixtures.sampleReservation(1L)), false));

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /admin/reservations - page와 size 쿼리 파라미터를 그대로 위임한다")
    void getReservationsDelegatesPageAndSizeQueryParameters() throws Exception {
        given(reservationService.getReservations(2, 5, null, MANAGER))
                .willReturn(ReservationResponses.of(List.of(), true));

        mockMvc.perform(get("/admin/reservations?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(reservationService).getReservations(2, 5, null, MANAGER);
    }

    @Test
    @DisplayName("GET /admin/reservations - name 쿼리 파라미터가 있으면 서비스에 위임한다")
    void getReservationsDelegatesNameQueryParameter() throws Exception {
        given(reservationService.getReservations(0, 20, "브라운", MANAGER))
                .willReturn(ReservationResponses.of(List.of(Fixtures.sampleReservation(1L)), false));

        mockMvc.perform(get("/admin/reservations?name=브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1));

        verify(reservationService).getReservations(0, 20, "브라운", MANAGER);
    }

    @Test
    @DisplayName("GET /admin/reservations - name이 빈 문자열이면 400과 메시지를 반환한다")
    void getReservationsReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(get("/admin/reservations?name="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /admin/reservations - page가 음수면 400과 메시지를 반환한다")
    void getReservationsReturns400WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/admin/reservations?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /admin/reservations - size가 0이면 400과 메시지를 반환한다")
    void getReservationsReturns400WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/admin/reservations?size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /admin/reservations - size가 상한 초과면 400과 메시지를 반환한다")
    void getReservationsReturns400WhenSizeExceedsLimit() throws Exception {
        mockMvc.perform(get("/admin/reservations?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 200을 반환하고 서비스에 위임한다")
    void deleteReservationReturns200AndDelegates() throws Exception {
        mockMvc.perform(post("/admin/reservations/3/cancel"))
                .andExpect(status().isOk());

        verify(reservationService).deleteReservation(3L, MANAGER);
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void deleteReservationReturns404OnResourceNotFoundException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", 9999L))
                .given(reservationService).deleteReservation(9999L, MANAGER);

        mockMvc.perform(post("/admin/reservations/9999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 과거 예약을 삭제하고 서비스에 위임한다")
    void deletePastReservationDelegatesToService() throws Exception {
        mockMvc.perform(delete("/admin/reservations/3"))
                .andExpect(status().isOk());

        verify(reservationService).deletePastReservation(3L, MANAGER);
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void deletePastReservationReturns404OnResourceNotFoundException() throws Exception {
        willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "예약", 9999L))
                .given(reservationService).deletePastReservation(9999L, MANAGER);

        mockMvc.perform(delete("/admin/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
