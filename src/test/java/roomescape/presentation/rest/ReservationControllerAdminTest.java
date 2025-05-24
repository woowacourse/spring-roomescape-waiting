package roomescape.presentation.rest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.application.ReservationService;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.user.UserRole;
import roomescape.exception.NotFoundException;
import roomescape.presentation.GlobalExceptionHandler;
import roomescape.presentation.StubAuthenticationInfoArgumentResolver;

class ReservationControllerAdminTest {

    private final ReservationService reservationService = Mockito.mock(ReservationService.class);
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new ReservationController(reservationService))
        .setCustomArgumentResolvers(new StubAuthenticationInfoArgumentResolver(new AuthenticationInfo(99L, UserRole.ADMIN)))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("예약 삭제 요청시, 주어진 아이디에 해당하는 예약이 있다면 삭제하고 NO CONTENT를 응답한다.")
    void deleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/reservations/1"))
            .andExpect(status().isNoContent());

        Mockito.verify(reservationService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("예약 삭제 요청시, 주어진 아이디에 해당하는 예약이 없다면 NOT FOUND를 응답한다.")
    void deleteWhenNotFound() throws Exception {
        Mockito.doThrow(new NotFoundException("should be thrown"))
            .when(reservationService).removeById(eq(999L));

        mockMvc.perform(delete("/reservations/999"))
            .andExpect(status().isNotFound());
    }
}
