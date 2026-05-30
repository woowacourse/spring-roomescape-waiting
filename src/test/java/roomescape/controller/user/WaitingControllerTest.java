package roomescape.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.vo.Name;
import roomescape.dto.request.WaitingRequestDto;
import roomescape.dto.response.WaitingResponse;
import roomescape.auth.service.WaitingAuthorizationService;
import roomescape.service.WaitingService;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    private final Member member = new Member(1L, "유저1", "user@test.com", "password", MemberRole.USER);
    private final Time time = new Time(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, new Name("방탈출테마"), "http://example.com/img.jpg", "방탈출 테마 설명");
    private final Waiting waiting = Waiting.reconstruct(1L, member, LocalDate.of(2026, 5, 10), time, theme, 1L, 1L);

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private WaitingService waitingService;
    @MockitoBean
    private WaitingAuthorizationService waitingAuthorizationService;
    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
    }

    @Nested
    class Post {

        @Test
        @DisplayName("유효한 요청으로 대기를 생성하면 201을 반환한다")
        void createsWaiting() {
            WaitingRequestDto requestDto = new WaitingRequestDto(
                    LocalDate.of(2026, 5, 10), time.getId(), theme.getId(), 1L);
            given(waitingService.create(any(), eq(member))).willReturn(waiting);
            WaitingResponse expected = WaitingResponse.from(waiting);

            WaitingResponse actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().post("/waitings")
                    .then()
                    .status(HttpStatus.CREATED)
                    .extract().as(WaitingResponse.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Get {

        @Test
        @DisplayName("내 대기 목록을 조회하면 200을 반환한다")
        void returnsMyWaitings() {
            given(waitingService.findAllByMemberId(member.getId())).willReturn(List.of(waiting));
            List<WaitingResponse> expected = List.of(WaitingResponse.from(waiting));

            List<WaitingResponse> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .when().get("/waitings")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("본인 대기를 삭제하면 204를 반환한다")
        void deletesWaiting() {
            willDoNothing().given(waitingAuthorizationService)
                    .validateMemberCanAccess(member.getId(), waiting.getId());
            willDoNothing().given(waitingService).delete(waiting.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .when().delete("/waitings/" + waiting.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(waitingAuthorizationService).should()
                    .validateMemberCanAccess(member.getId(), waiting.getId());
            then(waitingService).should().delete(waiting.getId());
        }
    }
}
