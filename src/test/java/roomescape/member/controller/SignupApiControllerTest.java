//package roomescape.member.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import roomescape.auth.application.TokenProvider;
//import roomescape.auth.ui.AdminAuthorizationInterceptor;
//import roomescape.common.config.WebMvcConfiguration;
//import roomescape.common.exception.GlobalExceptionHandler;
//import roomescape.common.security.TokenAuthorizationHandler;
//import roomescape.member.application.MemberService;
//import roomescape.member.application.dto.MemberRequest;
//import roomescape.member.application.dto.MemberResponse;
//
//@WebMvcTest(SignupApiController.class)
//@Import({WebMvcConfiguration.class, GlobalExceptionHandler.class})
//class SignupApiControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private MemberService memberService;
//    @MockitoBean
//    private TokenAuthorizationHandler tokenAuthorizationHandler;
//    @MockitoBean
//    private AdminAuthorizationInterceptor adminAuthorizationInterceptor;
//    @MockitoBean
//    private TokenProvider tokenProvider;
//
//    private static final String URI = "/signup";
//
//    @DisplayName("회원가입 요청을 처리한다")
//    @Test
//    void signup() throws Exception {
//        when(memberService.create(any(MemberRequest.class)))
//                .thenReturn(new MemberResponse(1L, "test-user", "test@example.com"));
//
//        MemberRequest signupRequest = new MemberRequest("test@example.com", "1234", "test-user");
//        String requestBody = objectMapper.writeValueAsString(signupRequest);
//
//        mockMvc.perform(post(URI)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isCreated());
//    }
//}
