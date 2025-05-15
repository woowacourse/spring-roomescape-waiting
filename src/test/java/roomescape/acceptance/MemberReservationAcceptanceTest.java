package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.JwtProvider;
import roomescape.service.ReservationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberReservationAcceptanceTest {

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("로그인 토큰이 요청되면 로그인된 사용자의 예약 결과가 응답으로 반환된다.")
    @Test
    void test1() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = this.reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        Theme savedTheme = this.themeRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        Member savedMember = this.memberRepository.save(member);

        Reservation reservation = new Reservation(LocalDate.now().plusDays(1), savedReservationTime, savedTheme,
                savedMember, LocalDate.now());
        Reservation savedReservation = this.reservationRepository.save(reservation);

        String token = jwtProvider.createToken(savedMember.getEmail());

        //when
        List<MemberReservationResponseDto> responses = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", MemberReservationResponseDto.class);

        //then
        List<MemberReservationResponseDto> comparedResponse = List.of(
                new MemberReservationResponseDto(savedReservation));

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses).isEqualTo(comparedResponse)
        );

    }
}
