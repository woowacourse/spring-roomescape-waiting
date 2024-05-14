package roomescape.service;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotExistException;
import roomescape.exception.PastTimeReservationException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.util.DatabaseCleaner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationTimeDao reservationTimeDao;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ThemeDao themeDao;

    @Autowired
    private MemberService memberService;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다")
    void create_reservation() {
        final long timeId = reservationTimeDao.create(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeDao.create(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final ReservationInput input = new ReservationInput("2023-03-13", timeId, themeId, memberId);

        assertThatCode(() -> reservationService.createReservation(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 ID 를 삭제하려 하면 에외를 발생한다.")
    void throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> reservationService.deleteReservation(-1))
                .isInstanceOf(NotExistException.class);
    }

    @Test
    @DisplayName("중복 예약 이면 예외를 발생한다.")
    void throw_exception_when_duplicate_reservationTime() {
        final long timeId = reservationTimeDao.create(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeDao.create(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        reservationService.createReservation(new ReservationInput("2011-11-24", timeId, themeId, memberId));
        final var input = new ReservationInput("2011-11-24", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> reservationService.createReservation(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("지나간 날짜와 시간으로 예약 생성 시 예외가 발생한다.")
    void throw_exception_when_create_past_time_reservation() {
        final long timeId = reservationTimeDao.create(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeDao.create(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final var input = new ReservationInput("1300-03-10", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> reservationService.createReservation(input))
                .isInstanceOf(PastTimeReservationException.class);
    }

    @Test
    @DisplayName("테마,멤버,날짜 범위에 맞는 예약을 검색한다.")
    void search_reservation_with_theme_member_and_date() {
        final Long timeId = reservationTimeDao.create(ReservationTime.from(null, "10:00"))
                .getId();
        final Long themeId = themeDao.create(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final var input1 = new ReservationInput("2024-05-10", timeId, themeId, memberId);
        final var input2 = new ReservationInput("2024-05-30", timeId, themeId, memberId);
        final var input3 = new ReservationInput("2024-05-15", timeId, themeDao.create(ThemeFixture.getDomain())
                .getId(), memberId);
        reservationService.createReservation(input1);
        reservationService.createReservation(input2);
        reservationService.createReservation(input3);

        assertThat(reservationService.searchReservation(new ReservationSearchInput(themeId, memberId,
                LocalDate.parse("2024-05-01"), LocalDate.parse("2024-05-20"))))
                .hasSize(1);
    }
}
