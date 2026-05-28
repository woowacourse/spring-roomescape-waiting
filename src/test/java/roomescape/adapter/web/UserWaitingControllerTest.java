package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.WaitingService;
import roomescape.application.dto.result.ReservationTimeResult;
import roomescape.application.dto.result.ThemeResult;
import roomescape.application.dto.result.WaitingResult;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;

/**
 * UserWaitingController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>검증 대상은 "웹 계층 고유의 책임"이다:
 * <ul>
 *   <li>요청 본문 @Valid 검증이 작동해 400을 내는가 (입력 게이트)</li>
 *   <li>정상 생성 시 201과 응답 형식</li>
 *   <li>서비스가 던진 예외가 GlobalExceptionHandler를 거쳐 약속된 상태코드+{"message":...}로 변환되는가</li>
 * </ul>
 * 서비스는 @MockBean으로 대체한다 — 여기서 "유스케이스가 진짜 동작하는가"는 검증하지 않는다.
 * 그건 WaitingServiceTest(통합)의 책임이다.
 *
 * <p>학습 메모: 이 슬라이스가 "인수 테스트로 흡수해도 되는데 왜 따로 두나"의 사례다.
 * 직렬화·@Valid·@RestControllerAdvice는 HTTP 계층을 통과해야만 동작하므로, 서비스를 직접
 * 호출하는 통합 테스트로는 검증되지 않는다. 인수 테스트로도 검증 가능하지만, 슬라이스로 좁히면
 * 컨텍스트가 가볍고 입력검증/예외변환의 케이스를 빠르게 늘릴 수 있다.
 * 인수 테스트와 무엇이 겹치고 무엇이 다른지는 WaitingAcceptanceTest와 비교해 보면 드러난다.
 */
@WebMvcTest(UserWaitingController.class)
class UserWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitingService waitingService;

    @Nested
    @DisplayName("대기 신청 POST /user/waitings")
    class Create {

        @Test
        @DisplayName("정상 요청이면 201과 대기 정보를 반환한다")
        void 정상_생성() throws Exception {
            given(waitingService.create(any())).willReturn(new WaitingResult(
                    1L, "콘", LocalDate.of(2050, 12, 31),
                    new ReservationTimeResult(1L, java.time.LocalTime.of(10, 0)),
                    new ThemeResult(1L, "테마A", "설명", "url"),
                    1));

            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"콘","date":"2050-12-31","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("콘"))
                    .andExpect(jsonPath("$.orderIndex").value(1));
        }

        @Test
        @DisplayName("[입력 게이트] 이름이 비어 있으면 @Valid가 막아 400 + 메시지")
        void 빈_이름_400() throws Exception {
            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","date":"2050-12-31","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("대기자 이름은 비어 있을 수 없습니다."));
        }

        @Test
        @DisplayName("[입력 게이트] timeId가 누락되면 400")
        void timeId_누락_400() throws Exception {
            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"콘","date":"2050-12-31","themeId":1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("대기 시간을 선택해 주세요."));
        }

        @Test
        @DisplayName("[예외 변환] 서비스가 비즈니스 규칙 위반을 던지면 400 + {\"message\":...}")
        void 비즈니스_예외_400() throws Exception {
            given(waitingService.create(any()))
                    .willThrow(new BusinessRuleViolationException(
                            "예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요."));

            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"콘","date":"2050-12-31","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요."));
        }

        @Test
        @DisplayName("[예외 변환] 서비스가 리소스 부재를 던지면 404 + {\"message\":...}")
        void 리소스_부재_404() throws Exception {
            given(waitingService.create(any()))
                    .willThrow(new ResourceNotFoundException("존재하지 않는 시간입니다."));

            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"콘","date":"2050-12-31","timeId":9999,"themeId":1}
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 시간입니다."));
        }

        @Test
        @DisplayName("[예외 변환] 잘못된 날짜 형식이면 파싱 단계에서 400")
        void 잘못된_날짜형식_400() throws Exception {
            mockMvc.perform(post("/user/waitings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"콘","date":"2050-13-99","timeId":1,"themeId":1}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("대기 취소 DELETE /user/waitings/{id}")
    class Cancel {

        @Test
        @DisplayName("정상 취소면 204")
        void 정상_취소() throws Exception {
            mockMvc.perform(delete("/user/waitings/1").param("name", "콘"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[예외 변환] 서비스가 리소스 부재를 던지면 404")
        void 타인_취소_404() throws Exception {
            doThrow(new ResourceNotFoundException("존재하지 않는 대기입니다."))
                    .when(waitingService).cancelByOwner(any(), any());

            mockMvc.perform(delete("/user/waitings/1").param("name", "모카"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 대기입니다."));
        }
    }
}
