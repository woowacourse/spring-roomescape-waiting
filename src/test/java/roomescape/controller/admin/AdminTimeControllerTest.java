package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;
import roomescape.dao.MemberDao;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Time;
import roomescape.dto.request.TimeRequestDto;
import roomescape.dto.response.TimeResponseDto;
import roomescape.service.TimeService;

import static org.mockito.BDDMockito.given;

@WebMvcTest(AdminTimeController.class)
class AdminTimeControllerTest {

    private final Member admin = new Member(1L, "어드민", "admin@test.com", "password", MemberRole.ADMIN);
    private final Time time = new Time(1L, LocalTime.of(13, 0));

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private TimeService timeService;
    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        given(memberDao.findById(admin.getId())).willReturn(Optional.of(admin));
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 시간 목록을 조회하면 200을 반환한다")
        void returnsAllTimes() {
            given(timeService.findAll()).willReturn(List.of(time));
            List<TimeResponseDto> expected = List.of(TimeResponseDto.from(time));

            List<TimeResponseDto> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().get("/admin/times")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하는 시간 id를 조회하면 200을 반환한다")
        void returnsTimeById() {
            given(timeService.findById(time.getId())).willReturn(time);
            TimeResponseDto expected = TimeResponseDto.from(time);

            TimeResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().get("/admin/times/" + time.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(TimeResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("유효한 요청으로 시간을 생성하면 201을 반환한다")
        void createsTime() {
            TimeRequestDto requestDto = new TimeRequestDto(LocalTime.of(13, 0));
            given(timeService.create(any())).willReturn(time);
            TimeResponseDto expected = TimeResponseDto.from(time);

            TimeResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().post("/admin/times")
                    .then()
                    .status(HttpStatus.CREATED)
                    .header("Location", "http://localhost/admin/times/" + time.getId())
                    .extract().as(TimeResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("시간을 삭제하면 204를 반환한다")
        void deletesTime() {
            willDoNothing().given(timeService).delete(time.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().delete("/admin/times/" + time.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(timeService).should().delete(time.getId());
        }
    }
}
