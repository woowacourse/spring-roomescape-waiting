package roomescape.controller.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.vo.Name;
import roomescape.dto.response.WaitingResponse;
import roomescape.auth.service.WaitingAuthorizationService;
import roomescape.service.WaitingService;

@WebMvcTest(ManagerWaitingController.class)
class ManagerWaitingControllerTest {

    private static final Long STORE_ID = 1L;

    private final Member manager = new Member(1L, "매니저", "manager@test.com", "password", MemberRole.MANAGER, STORE_ID);
    private final Member user = new Member(2L, "유저", "user@test.com", "password", MemberRole.USER);
    private final Time time = new Time(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, new Name("방탈출테마"), "http://example.com/img.jpg", "방탈출 테마 설명");
    private final Waiting waiting = Waiting.reconstruct(1L, user, LocalDate.of(2026, 5, 10), time, theme, STORE_ID, 1L);

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
        given(memberDao.findById(manager.getId())).willReturn(Optional.of(manager));
    }

    @Nested
    class Get {

        @Test
        @DisplayName("자신의 매장 대기 목록을 조회하면 200을 반환한다")
        void returnsOwnStoreWaitings() {
            given(waitingService.findAllByStoreId(STORE_ID)).willReturn(List.of(waiting));
            List<WaitingResponse> expected = List.of(WaitingResponse.from(waiting));

            List<WaitingResponse> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(manager.getId()))
                    .when().get("/manager/waitings")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("자신의 매장 대기를 삭제하면 204를 반환한다")
        void deletesOwnStoreWaiting() {
            willDoNothing().given(waitingAuthorizationService)
                    .validateManagerCanAccess(STORE_ID, waiting.getId());
            willDoNothing().given(waitingService).delete(waiting.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(manager.getId()))
                    .when().delete("/manager/waitings/" + waiting.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(waitingAuthorizationService).should()
                    .validateManagerCanAccess(STORE_ID, waiting.getId());
            then(waitingService).should().delete(waiting.getId());
        }

        @Test
        @DisplayName("타 매장 대기를 삭제하면 403을 반환한다")
        void rejectsOtherStoreWaiting() {
            willThrow(new UnauthorizedException())
                    .given(waitingAuthorizationService)
                    .validateManagerCanAccess(STORE_ID, waiting.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(manager.getId()))
                    .when().delete("/manager/waitings/" + waiting.getId())
                    .then()
                    .status(HttpStatus.FORBIDDEN);

            then(waitingService).shouldHaveNoInteractions();
        }
    }
}
