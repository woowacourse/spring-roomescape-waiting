package roomescape.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import roomescape.exception.ResourceNotFoundException;
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
    void GET_admin_reservations_서비스가_반환한_목록과_hasNext를_그대로_응답한다() throws Exception {
        given(reservationService.getReservations(0, 20, null, MANAGER))
                .willReturn(ReservationResponses.of(List.of(Fixtures.sampleReservation(1L)), false));

        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1))
                .andExpect(jsonPath("$.reservations[0].name").value("브라운"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void GET_admin_reservations_page와_size_쿼리_파라미터를_그대로_위임한다() throws Exception {
        given(reservationService.getReservations(2, 5, null, MANAGER))
                .willReturn(ReservationResponses.of(List.of(), true));

        mockMvc.perform(get("/admin/reservations?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(reservationService).getReservations(2, 5, null, MANAGER);
    }

    @Test
    void GET_admin_reservations_name_쿼리_파라미터가_있으면_서비스에_위임한다() throws Exception {
        given(reservationService.getReservations(0, 20, "브라운", MANAGER))
                .willReturn(ReservationResponses.of(List.of(Fixtures.sampleReservation(1L)), false));

        mockMvc.perform(get("/admin/reservations?name=브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.size()").value(1));

        verify(reservationService).getReservations(0, 20, "브라운", MANAGER);
    }

    @Test
    void GET_admin_reservations_name이_빈_문자열이면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations?name="))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name은(는) 최소 1자 이상이어야 합니다."));
    }

    @Test
    void GET_admin_reservations_page가_음수면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page은(는) 0 이상이어야 합니다."));
    }

    @Test
    void GET_admin_reservations_size가_0이면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations?size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size은(는) 1 이상이어야 합니다."));
    }

    @Test
    void GET_admin_reservations_size가_상한_초과면_400과_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size은(는) 100 이하여야 합니다."));
    }

    @Test
    void POST_admin_reservations_id_cancel_200을_반환하고_서비스에_위임한다() throws Exception {
        mockMvc.perform(post("/admin/reservations/3/cancel"))
                .andExpect(status().isOk());

        verify(reservationService).cancelReservation(3L, MANAGER);
    }

    @Test
    void POST_admin_reservations_id_cancel_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new ResourceNotFoundException("예약", 9999L))
                .given(reservationService).cancelReservation(9999L, MANAGER);

        mockMvc.perform(post("/admin/reservations/9999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을(를) 찾을 수 없습니다. id=9999"));
    }

    @Test
    void DELETE_admin_reservations_id_과거_예약을_삭제하고_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/admin/reservations/3"))
                .andExpect(status().isOk());

        verify(reservationService).deletePastReservation(3L, MANAGER);
    }

    @Test
    void DELETE_admin_reservations_id_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        willThrow(new ResourceNotFoundException("예약", 9999L))
                .given(reservationService).deletePastReservation(9999L, MANAGER);

        mockMvc.perform(delete("/admin/reservations/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약을(를) 찾을 수 없습니다. id=9999"));
    }
}
