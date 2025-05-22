package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.theme.ThemeRequest;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마 생성 테스트")
    void createTheme() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("테스트 테마", "테스트 설명", "https://test.com/image.jpg");
        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("테스트 테마"));
    }

    @Test
    @DisplayName("테마 전체 조회 테스트")
    void getThemes() throws Exception {
        //given
        Theme theme = new Theme("제목", "설명", "썸네일");
        themeRepository.save(theme);
        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("테마 삭제 테스트")
    void deleteTheme() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("삭제용 테마", "설명", "https://test.com/image2.jpg");
        String response = mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("jwt", "테스트용_관리자_토큰")))
                .andReturn().getResponse().getContentAsString();
        Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("id").asLong();
        // when & then
        mockMvc.perform(delete("/themes/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("인기 테마 조회 테스트")
    void getPopularThemes() throws Exception {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme("이름", "설명", "썸네일");
        Member member = new Member(null, "슬링키", "email", "password", Role.USER);
        Reservation reservation = new Reservation(LocalDate.now().minusDays(1), reservationTime, theme, member);

        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(reservation);

        // when & then
        mockMvc.perform(get("/themes/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
