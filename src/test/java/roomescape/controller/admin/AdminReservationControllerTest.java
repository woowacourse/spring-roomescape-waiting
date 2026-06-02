package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.response.AdminReservationResponseDto;
import roomescape.dto.response.PageResponse;
import roomescape.service.AdminReservationService;
import java.util.Optional;
import roomescape.dao.MemberDao;

@WebMvcTest(AdminReservationController.class)
class AdminReservationControllerTest {

    private final Member admin = new Member(1L, "어드민", "admin@test.com", "password", MemberRole.ADMIN);
    private final Time time = new Time(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, new Name("방탈출테마"), "http://example.com/img.jpg", "방탈출 테마 설명");
    private final Reservation reservation = Reservation.reconstruct(1L, admin, LocalDate.now().plusDays(1), time, theme);

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AdminReservationService reservationService;
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
        @DisplayName("전체 예약 목록을 페이지로 조회한다")
        void returnsAllReservations() {
            List<Reservation> reservations = List.of(reservation);
            List<AdminReservationResponseDto> content = reservations.stream()
                    .map(AdminReservationResponseDto::from)
                    .toList();
            PageResponse<Reservation> pageResponse = new PageResponse<>(reservations, 1L, 1, 0, 10);
            given(reservationService.findAll(anyInt(), anyInt())).willReturn(pageResponse);

            PageResponse<AdminReservationResponseDto> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .when().get("/admin/reservations")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual.content()).isEqualTo(content);
            assertThat(actual.totalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하는 예약 id를 조회하면 200을 반환한다")
        void returnsReservationById() {
            given(reservationService.findById(reservation.getId())).willReturn(reservation);
            AdminReservationResponseDto expected = AdminReservationResponseDto.from(reservation);

            AdminReservationResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().get("/admin/reservations/" + reservation.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(AdminReservationResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("유효한 요청으로 예약을 생성하면 201을 반환한다")
        void createsReservation() {
            AdminReservationRequestDto requestDto = new AdminReservationRequestDto(
                    admin.getId(), reservation.getDate(), time.getId(), theme.getId(), null);
            given(reservationService.createByAdmin(any(AdminReservationRequestDto.class))).willReturn(reservation);
            AdminReservationResponseDto expected = AdminReservationResponseDto.from(reservation);

            AdminReservationResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().post("/admin/reservations")
                    .then()
                    .status(HttpStatus.CREATED)
                    .header("Location", "http://localhost/admin/reservations/" + reservation.getId())
                    .extract().as(AdminReservationResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Patch {

        @Test
        @DisplayName("유효한 요청으로 예약을 수정하면 200을 반환한다")
        void updatesReservation() {
            ReservationPatchDto requestDto = new ReservationPatchDto(LocalDate.now().plusDays(2), time.getId());
            given(reservationService.update(eq(reservation.getId()), any(ReservationPatchDto.class)))
                    .willReturn(reservation);
            AdminReservationResponseDto expected = AdminReservationResponseDto.from(reservation);

            AdminReservationResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().patch("/admin/reservations/" + reservation.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(AdminReservationResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Cancel {

        @Test
        @DisplayName("어드민이 예약을 취소하면 204를 반환한다")
        void cancelsReservation() {
            willDoNothing().given(reservationService).cancelByAdmin(reservation.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().delete("/admin/reservations/" + reservation.getId() + "/cancel")
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(reservationService).should().cancelByAdmin(reservation.getId());
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("예약을 삭제하면 204를 반환한다")
        void deletesReservation() {
            willDoNothing().given(reservationService).delete(reservation.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().delete("/admin/reservations/" + reservation.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(reservationService).should().delete(reservation.getId());
        }
    }
}
