package roomescape.member.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void 모든_회원_조회_성공() throws Exception {
        // given
        memberService.save(new MemberRequest("hong@example.com", "password123", "홍길동"));
        memberService.save(new MemberRequest("kim@example.com", "password123", "김철수"));

        // when & then
        mockMvc.perform(get("/members")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("[0].name", is("홍길동")))
        .andExpect(jsonPath("[1].name", is("김철수")));
    }

    @Test
    void 회원_가입_성공() throws Exception {
        // given
        MemberRequest request = new MemberRequest("new@example.com", "password123", "신규회원");

        // when & then
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"신규회원\", \"email\": \"new@example.com\", \"password\": \"password123\"}")
        )
        .andExpect(status().isCreated());
    }

    @Test
    void 중복된_이메일로_회원_가입_실패() throws Exception {
        // given
        memberService.save(new MemberRequest("existing@example.com", "password123", "중복회원"));

        // when & then
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"중복회원\", \"email\": \"existing@example.com\", \"password\": \"password123\"}")
        )
        .andExpect(status().isBadRequest());
    }
}
