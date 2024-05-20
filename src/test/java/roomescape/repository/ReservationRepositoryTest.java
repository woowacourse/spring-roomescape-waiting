package roomescape.repository;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location=classpath:/application.properties"})
class ReservationRepositoryTest {

    @LocalServerPort
    private int port;
    private final Member adminMember =
            new Member((long) 1, "어드민", "testDB@email.com", "1234", Role.ADMIN);
    private final Member userMember =
            new Member((long) 2, "사용자", "test2DB@email.com", "1234", Role.USER);
    private final Theme themeOne =
            new Theme((long) 1, "레벨1 탈출", "우테코 레벨2를 탈출하는 내용입니다",
                    "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    private final Theme themeTwo =
            new Theme((long) 2, "레벨2 탈출", "우테코 레벨3를 탈출하는 내용입니다",
                    "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    private final LocalDate fromDate = LocalDate.of(2024, 5, 18);
    private final LocalDate toDate = LocalDate.of(2024, 5, 20);
    private final TimeSlot timeOne = new TimeSlot((long) 1, LocalTime.of(10, 0));
    private final TimeSlot timeTwo = new TimeSlot((long) 2, LocalTime.of(11, 0));

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("member를 기준으로 해당 member의 모든 예약 목록을 조회한다.")
    @Test
    void findAllByMember() {
        //given, when
        List<Reservation> reservations = reservationRepository.findAllByMember(adminMember);

        //then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("date, theme를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByDateAndTheme() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByDateAndTheme(fromDate, themeOne);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("member, theme, date를 기준으로 해당하는 모든 예약 목록을 조회한다.")
    @Test
    void findAllByMemberAndThemeAndDateBetween() {
        //given, when
        List<Reservation> reservations = reservationRepository
                .findAllByMemberAndThemeAndDateBetween(adminMember, themeOne, fromDate, toDate);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("해당 theme에 예약이 존재하는지 확인한다.")
    @Test
    void existsByTheme() {
        //given, when
        boolean isReservationExistsAtThemeOne = reservationRepository.existsByTheme(themeOne);
        boolean isReservationExistsAtThemeTwo = reservationRepository.existsByTheme(themeTwo);

        assertAll(
                ()-> assertThat(isReservationExistsAtThemeOne).isTrue(),
                ()-> assertThat(isReservationExistsAtThemeTwo).isFalse()
        );
    }

    @DisplayName("해당 time에 예약이 존재하는지 확인한다.")
    @Test
    void existsByTime() {
        //given, when
        boolean isReservationExistsAtTimeOne = reservationRepository.existsByTime(timeOne);
        boolean isReservationExistsAtTimeTwo = reservationRepository.existsByTime(timeTwo);

        assertAll(
                ()-> assertThat(isReservationExistsAtTimeOne).isTrue(),
                ()-> assertThat(isReservationExistsAtTimeTwo).isFalse()
        );
    }

    @DisplayName("해당 date와 theme와 time에 해당하는 예약이 존재하는지 확인한다.")
    @Test
    void existsByDateAndTimeAndTheme() {
        //given, when
        boolean isReservationExists_true = reservationRepository
                .existsByDateAndTimeAndTheme(fromDate, timeOne, themeOne);
        boolean isReservationExists_false = reservationRepository
                .existsByDateAndTimeAndTheme(fromDate, timeTwo, themeTwo);

        assertAll(
                ()-> assertThat(isReservationExists_true).isTrue(),
                ()-> assertThat(isReservationExists_false).isFalse()
        );
    }
}
