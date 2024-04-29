package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String rawTime = "10:00";

    @DisplayName("성공: 예약 시간을 저장하고, id 값과 함께 반환한다.")
    @Test
    void save() {
        ReservationTime saved = reservationTimeService.save(rawTime);
        assertThat(saved).isEqualTo(new ReservationTime(saved.getId(), rawTime));
    }

    @DisplayName("실패: 잘못된 시간 포맷을 저장하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"24:00", "-1:00", "10:60"})
    @NullAndEmptySource
    void save_IllegalTimeFormat(String invalidRawTime) {
        assertThatThrownBy(
            () -> reservationTimeService.save(invalidRawTime)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("잘못된 시간 형식입니다.");
    }

    @DisplayName("실패: 이미 존재하는 시간을 추가할 수 없다.")
    @Test
    void save_TimeAlreadyExists() {
        reservationTimeService.save(rawTime);
        assertThatThrownBy(
            () -> reservationTimeService.save(rawTime)
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 존재하는 시간은 추가할 수 없습니다.");
    }

    @DisplayName("실패: 시간을 사용하는 예약이 존재하는 경우 시간을 삭제할 수 없다.")
    @Test
    void delete_ReservationExists() {
        ReservationTime savedTime = reservationTimeService.save(rawTime);

        Theme savedTheme = themeRepository.save(
            new Theme("themeName", "themeDesc", "https://")
        );

        jdbcTemplate.update(
            "INSERT INTO member(name, email, password, role) VALUES ('admin', 'admin@a.com', '123a!', 'ADMIN')");

        reservationRepository.save(new Reservation(
            new LoginMember("1", "admin@a.com", "admin", "ADMIN"),
            "2060-01-01",
            savedTime,
            savedTheme)
        );

        assertThatThrownBy(
            () -> reservationTimeService.delete(savedTime.getId())
        ).isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }
}
