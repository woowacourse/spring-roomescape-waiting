package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.ReservationService;
import roomescape.exception.client.ResourceNotFoundException;

/**
 * AdminReservationController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>고유 책임: 관리자 예약 생성 요청 본문의 @Valid 검증과 서비스 예외 변환.
 * 관리자 생성도 사용자 생성과 같은 ReservationRequest를 쓰므로 @Valid 게이트가 동작한다.
 *
 * <p>해피 패스(201/204)는 AdminAcceptanceTest가 실제 DB로 끝까지 검증하므로 여기서 두지 않는다.
 * 이 슬라이스는 인수 테스트에 대응물이 없는 웹 계층 고유 검증(입력 게이트·예외 변환)만 책임진다.
 */
@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Nested
    @DisplayName("예약 생성 POST /admin/reservations")
    class Create {

        @Test
        @DisplayName("[입력 게이트] 이름이 비어 있으면 400")
        void 빈_이름_400() throws Exception {
            mockMvc.perform(post("/admin/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","date":"2050-12-31","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("예약자 이름은 비어 있을 수 없습니다."));
        }

        @Test
        @DisplayName("[예외 변환] 존재하지 않는 테마면 404")
        void 존재하지_않는_테마_404() throws Exception {
            given(reservationService.create(any()))
                    .willThrow(new ResourceNotFoundException("존재하지 않는 테마입니다."));

            mockMvc.perform(post("/admin/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"브라운","date":"2050-12-31","timeId":1,"themeId":9999}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 테마입니다."));
        }
    }
}
