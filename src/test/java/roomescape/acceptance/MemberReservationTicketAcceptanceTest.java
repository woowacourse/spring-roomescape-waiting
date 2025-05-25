package roomescape.acceptance;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.application.service.ReservationTicketService;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationTicketJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.jwt.JjwtJwtTokenProvider;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberReservationTicketAcceptanceTest {

    @Autowired
    JjwtJwtTokenProvider jjwtJwtTokenProvider;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    ThemeJpaRepository themeJpaRepository;

    @Autowired
    ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    ReservationTicketJpaRepository reservationTicketJpaRepository;
    @Autowired
    private ReservationTicketService reservationTicketService;

    @DisplayName("로그인 토큰이 요청되면 로그인된 사용자의 예약 결과가 응답으로 반환된다.")
    @Test
    void test1() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 30));
        ReservationTime savedReservationTime = this.reservationTimeJpaRepository.save(reservationTime);

        Theme theme = new Theme("테마", "공포", "image");
        Theme savedTheme = this.themeJpaRepository.save(theme);

        Member member = new Member("도기", "email@gamil.com", "password", Role.ADMIN);
        Member savedMember = this.memberJpaRepository.save(member);

        ReservationTicket reservationTicket = new ReservationTicket(
                new Reservation(LocalDate.now().plusDays(1), savedReservationTime, savedTheme,
                        savedMember, LocalDate.now()));
        ReservationTicket savedReservationTicket = this.reservationTicketJpaRepository.save(reservationTicket);

        String token = jjwtJwtTokenProvider.createToken(savedMember.getEmail());

        //when
        List<MemberReservationResponseDto> responses = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", MemberReservationResponseDto.class);

        //then
        List<MemberReservationResponseDto> comparedResponse = List.of(
                new MemberReservationResponseDto(savedReservationTicket));

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses).isEqualTo(comparedResponse)
        );

    }
}
