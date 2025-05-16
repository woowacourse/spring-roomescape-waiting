package roomescape.member.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.admin.domain.dto.AdminReservationRequestDto;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.domain.dto.TokenResponseDto;
import roomescape.auth.fixture.AuthFixture;
import roomescape.auth.service.AuthService;
import roomescape.member.domain.dto.ReservationWithBookStateDto;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.fixture.ReservationTimeFixture;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.UserFixture;
import roomescape.user.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class MemberControllerTest {

    @Autowired
    private UserService service;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AuthService authService;
    
    @LocalServerPort
    private int port;

    private static LocalDate date;
    private User savedMember;
    private Theme savedTheme;
    private ReservationTime savedTime;
    private TokenResponseDto memberTokenResponseDto;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        
        date = LocalDate.now().plusDays(1);

        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail", "member_dummyPassword");
        Theme theme = ThemeFixture.create("dummyName", "dummyDescription", "dummyThumbnail");
        ReservationTime time = ReservationTimeFixture.create(LocalTime.of(2, 40));

        savedMember = entityManager.persist(member);
        savedTheme = entityManager.persist(theme);
        savedTime = entityManager.persist(time);
        
        entityManager.flush();

        memberTokenResponseDto = authService.login(
                AuthFixture.createTokenRequestDto(savedMember.getEmail(), savedMember.getPassword()));
    }

    @DisplayName("유저의 예약 리스트 조회 기능 : 성공 시 200 OK 반환")
    @Test
    void findAllReservationsByMember_success_byMember() {
        // given
        AdminReservationRequestDto dto = new AdminReservationRequestDto(date,
                savedTheme.getId(),
                savedTime.getId(),
                savedMember.getId());

        String token = memberTokenResponseDto.accessToken();

        // when
        // then
        RestAssured.given().log().all()
                .cookies("token", token)
                .contentType(ContentType.JSON)
                .body(dto)
                .when().get("/members/reservations-mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
