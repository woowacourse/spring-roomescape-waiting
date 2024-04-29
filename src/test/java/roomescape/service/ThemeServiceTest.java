package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.member.LoginMember;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String name = "themeName";
    private final String description = "themeDesc";
    private final String thumbnail = "https://";

    @DisplayName("실패: 이름이 null 또는 빈 값이면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    void save_IllegalName(String invalidName) {
        assertThatThrownBy(
            () -> themeService.save(invalidName, description, thumbnail)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("테마 이름은 null이거나 비어 있을 수 없습니다.");
    }

    @DisplayName("실패: description이 null 또는 빈 값이면 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    void save_IllegalDescription(String invalidDescription) {
        assertThatThrownBy(
            () -> themeService.save(name, invalidDescription, thumbnail)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("테마 설명은 null이거나 비어 있을 수 없습니다.");
    }

    @DisplayName("thumbnail 형식이 잘못된 경우 예외 발생")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ftp://hello.jpg"})
    void save_IllegalThumbnail(String invalidThumbnail) {
        assertThatThrownBy(
            () -> themeService.save(name, description, invalidThumbnail)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("썸네일 URL은 https://로 시작해야 합니다.");
    }

    @DisplayName("실패: 이름이 동일한 방탈출 테마를 저장하면 예외 발생")
    @Test
    void save_DuplicatedName() {
        themeService.save(name, description, thumbnail);

        assertThatThrownBy(
            () -> themeService.save(name, "d", "https://d")
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("같은 이름의 테마가 이미 존재합니다.");
    }

    @DisplayName("실패: 예약에 사용되는 테마 삭제 시도 시 예외 발생")
    @Test
    void delete_ReservationExists() {
        Theme savedTheme = themeService.save(name, description, thumbnail);

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime("10:00"));

        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('admin', 'admin@a.com', '123a!', 'ADMIN')");

        reservationRepository.save(new Reservation(
            new LoginMember("1", "admin@a.com", "admin", "ADMIN"),
            "2060-01-01",
            savedTime,
            savedTheme)
        );

        assertThatThrownBy(() -> themeService.delete(savedTheme.getId()))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }
}
