package roomescape.admin.member;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.resolver.MemberArgumentResolver;

@WebMvcTest(value = AdminMemberController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebMvcConfig.class,
                        AuthorizationInterceptor.class,
                        MemberArgumentResolver.class
                }
        )
)
@DisplayName("관리자 회원 컨트롤러 테스트")
class AdminMemberControllerTest {

    private static final String MEMBERS_URL = "/members";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Nested
    @DisplayName("회원 목록 조회 API")
    class GetMembers {

        @Test
        @DisplayName("전체 회원 목록을 성공적으로 반환한다")
        void returnMemberList() throws Exception {
            // given
            List<MemberResponse> responses = List.of(
                    createMemberResponse(1L, "name1"),
                    createMemberResponse(2L, "name2")
            );

            doReturn(responses).when(memberService)
                    .getMembers();

            // when && then
            mockMvc.perform(get(MEMBERS_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("name1"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("name2"))
                    .andDo(print());
        }

        @Test
        @DisplayName("회원이 없는 경우 빈 목록을 반환한다")
        void returnEmptyList() throws Exception {
            // given
            doReturn(List.of()).when(memberService)
                    .getMembers();

            // when && then
            mockMvc.perform(get(MEMBERS_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty())
                    .andDo(print());
        }
    }

    private MemberResponse createMemberResponse(Long id, String name) {
        return new MemberResponse(
                new Member(id, "email@email.com", "password", name, Role.ADMIN)
        );
    }
}
