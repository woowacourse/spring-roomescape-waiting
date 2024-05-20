package roomescape.member.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import roomescape.global.config.WebMvcConfiguration;
import roomescape.auth.presentation.AdminAuthorizationInterceptor;
import roomescape.auth.presentation.LoginMemberArgumentResolver;
import roomescape.common.ControllerTest;
import roomescape.member.application.MemberService;
import roomescape.member.dto.request.MemberJoinRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.TestFixture.*;

@WebMvcTest(
        value = MemberController.class,
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebMvcConfiguration.class, LoginMemberArgumentResolver.class, AdminAuthorizationInterceptor.class})
)
class MemberControllerTest extends ControllerTest {
    @MockBean
    MemberService memberService;

    @Test
    @DisplayName("사용자 가입 POST 요청 시 상태코드 201을 반환한다.")
    void join() throws Exception {
        // given
        MemberJoinRequest request = new MemberJoinRequest(MIA_EMAIL, TEST_PASSWORD, MIA_NAME);

        BDDMockito.given(memberService.create(any()))
                .willReturn(USER_MIA());

        // when & then
        mockMvc.perform(post("/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(MIA_NAME))
                .andExpect(jsonPath("$.email").value(MIA_EMAIL));
    }

    @ParameterizedTest
    @MethodSource(value = "invalidRequests")
    @DisplayName("사용자 가입 POST 요청 시 비어있는 필드가 있다면 상태코드 400을 반환한다.")
    void joinWithInvalidRequest(MemberJoinRequest request) throws Exception {
        BDDMockito.given(memberService.create(any()))
                .willReturn(USER_MIA());

        // when & then
        mockMvc.perform(post("/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<MemberJoinRequest> invalidRequests() {
        return Stream.of(
                new MemberJoinRequest(null, TEST_PASSWORD, MIA_NAME),
                new MemberJoinRequest(MIA_EMAIL, "", MIA_NAME),
                new MemberJoinRequest(MIA_EMAIL, TEST_PASSWORD, " ")
        );
    }

    @Test
    @DisplayName("사용자 목록 조회 GET 요청시 상태코드 200을 반환한다.")
    void findAll() throws Exception {
        // given
        BDDMockito.given(memberService.findAll())
                .willReturn(List.of(USER_MIA(), USER_TOMMY()));

        // when & then
        mockMvc.perform(get("/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(MIA_NAME))
                .andExpect(jsonPath("$[1].name").value(TOMMY_NAME));
    }
}
