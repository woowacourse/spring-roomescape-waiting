package roomescape.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.Name;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.response.ReservationResponseDto;
import java.util.Optional;
import roomescape.dao.MemberDao;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private final Member member = new Member(1L, "유저1", "user@test.com", "password", MemberRole.USER);
    private final Time time = new Time(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, new Name("방탈출테마"), "http://example.com/img.jpg", "방탈출 테마 설명");
    private final Reservation reservation = Reservation.reconstruct(1L, member, LocalDate.of(2026, 5, 10), time, theme);

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        given(memberDao.findById(member.getId())).willReturn(Optional.of(member));
    }

    @Nested
    class Get {

        @Test
        @DisplayName("내 예약 목록을 조회하면 200을 반환한다")
        void returnsMyReservations() {
            given(reservationService.findAllByMemberId(member.getId())).willReturn(List.of(reservation));
            List<ReservationResponseDto> expected = List.of(ReservationResponseDto.from(reservation));

            List<ReservationResponseDto> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .when().get("/reservations")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("예약을 취소하면 204를 반환한다")
        void cancelsReservation() {
            willDoNothing().given(reservationService).cancel(reservation.getId(), member.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .when().delete("/reservations/" + reservation.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(reservationService).should().cancel(reservation.getId(), member.getId());
        }
    }

    @Nested
    class Patch {

        @Test
        @DisplayName("본인 예약을 수정하면 200을 반환한다")
        void updatesReservation() {
            ReservationPatchDto requestDto = new ReservationPatchDto(LocalDate.of(2026, 6, 1), 1L);
            given(reservationService.updateByUser(any(), any(), any())).willReturn(reservation);
            ReservationResponseDto expected = ReservationResponseDto.from(reservation);

            ReservationResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().patch("/reservations/" + reservation.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(ReservationResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("유효한 요청으로 예약을 생성하면 201을 반환한다")
        void createsReservation() {
            ReservationRequestDto requestDto = new ReservationRequestDto(LocalDate.of(2026, 5, 10), 1L, 1L, null);
            given(reservationService.create(any(), any())).willReturn(reservation);
            ReservationResponseDto expected = ReservationResponseDto.from(reservation);

            ReservationResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(member.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().post("/reservations")
                    .then()
                    .status(HttpStatus.CREATED)
                    .extract().as(ReservationResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }
}
