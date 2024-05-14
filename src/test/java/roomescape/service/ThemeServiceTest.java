package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.ExistReservationException;
import roomescape.exception.NotExistException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.input.ThemeInput;
import roomescape.service.dto.output.ReservationTimeOutput;
import roomescape.service.dto.output.ThemeOutput;
import roomescape.util.DatabaseCleaner;

@SpringBootTest
class ThemeServiceTest {

    @Autowired
    ThemeService themeService;
    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    ReservationDao reservationDao;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다.")
    void create_reservationTime() {
        final ThemeInput input = new ThemeInput(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        assertThatCode(() -> themeService.createTheme(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_input_is_invalid() {
        final ThemeInput input = new ThemeInput(
                "",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        assertThatThrownBy(() -> themeService.createTheme(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 요소를 받아온다.")
    void get_all_themes() {
        final ThemeInput input = new ThemeInput(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        themeService.createTheme(input);

        final var result = themeService.getAllThemes();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID 를 삭제하려 하면 에외를 발생한다.")
    void throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> themeService.deleteTheme(-1))
                .isInstanceOf(NotExistException.class);
    }

    @Test
    @DisplayName("특정 테마에 대한 예약이 존재하면 예외를 발생한다.")
    void throw_exception_when_delete_id_that_exist_reservation() {
        final ThemeOutput themeOutput = themeService.createTheme(
                ThemeFixture.getInput());

        final ReservationTimeOutput timeOutput = reservationTimeService.createReservationTime(
                new ReservationTimeInput("10:00"));

        reservationDao.create(Reservation.from(
                null,
                "2024-04-30",
                ReservationTime.from(timeOutput.id(), timeOutput.startAt()),
                Theme.of(themeOutput.id(), themeOutput.name(), themeOutput.description(), themeOutput.thumbnail()),
                MemberFixture.getDomain()
        ));
        final var themeId = themeOutput.id();

        assertThatThrownBy(() -> themeService.deleteTheme(themeId))
                .isInstanceOf(ExistReservationException.class);
    }

    @Test
    @DisplayName("예약이 많은 테마 순으로 조회한다.")
    void get_popular_themes() {
        final ThemeOutput themeOutput1 = themeService.createTheme(ThemeFixture.getInput());
        final ThemeOutput themeOutput2 = themeService.createTheme(new ThemeInput(
                "레벨3 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        ));
        final ReservationTimeOutput timeOutput = reservationTimeService.createReservationTime(
                new ReservationTimeInput("10:00"));

        reservationDao.create(Reservation.from(
                null,
                "2024-06-01",
                ReservationTime.from(timeOutput.id(), timeOutput.startAt()),
                Theme.of(themeOutput1.id(), themeOutput1.name(), themeOutput1.description(), themeOutput1.thumbnail()),
                MemberFixture.getDomain()
        ));
        reservationDao.create(Reservation.from(
                null,
                "2024-06-02",
                ReservationTime.from(timeOutput.id(), timeOutput.startAt()),
                Theme.of(themeOutput1.id(), themeOutput1.name(), themeOutput1.description(), themeOutput1.thumbnail()),
                MemberFixture.getDomain()
        ));
        reservationDao.create(Reservation.from(
                null,
                "2024-06-03",
                ReservationTime.from(timeOutput.id(), timeOutput.startAt()),
                Theme.of(themeOutput2.id(), themeOutput2.name(), themeOutput2.description(), themeOutput2.thumbnail()),
                MemberFixture.getDomain()
        ));

        final List<ThemeOutput> popularThemes = themeService.getPopularThemes(LocalDate.parse("2024-06-04"));

        assertThat(popularThemes).containsExactly(
                new ThemeOutput(themeOutput1.id(), themeOutput1.name(), themeOutput1.description(),
                        themeOutput1.thumbnail()),
                new ThemeOutput(themeOutput2.id(), themeOutput2.name(), themeOutput2.description(),
                        themeOutput2.thumbnail())
        );
    }
}
