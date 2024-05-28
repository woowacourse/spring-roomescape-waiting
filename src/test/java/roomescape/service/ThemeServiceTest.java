package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateThemeResponse;
import roomescape.controller.dto.FindThemeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private final String name = "테마1";
    private final String description = "테마1에 대한 설명입니다.";
    private final String thumbnail = "https://test.com/test.jpg";

    @DisplayName("성공: 테마 추가")
    @Test
    void save() {
        CreateThemeResponse saved = themeService.save(name, description, thumbnail);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("성공: 테마 삭제")
    @Test
    void delete() {
        themeRepository.save(new Theme("테마1", "d1", "https://test.com/test1.jpg"));
        themeRepository.save(new Theme("테마2", "d2", "https://test.com/test2.jpg"));
        themeRepository.save(new Theme("테마3", "d3", "https://test.com/test3.jpg"));

        themeService.delete(2L);
        assertThat(themeService.findAll())
            .extracting(FindThemeResponse::id)
            .containsExactly(1L, 3L);
    }

    @DisplayName("성공: 전체 테마 조회")
    @Test
    void findAll() {
        themeRepository.save(new Theme("테마1", "d1", "https://test.com/test1.jpg"));
        themeRepository.save(new Theme("테마2", "d2", "https://test.com/test2.jpg"));
        themeRepository.save(new Theme("테마3", "d3", "https://test.com/test3.jpg"));

        assertThat(themeService.findAll())
            .extracting(FindThemeResponse::id)
            .containsExactly(1L, 2L, 3L);
    }

    @DisplayName("실패: 이름이 동일한 방탈출 테마를 저장하면 예외 발생")
    @Test
    void save_DuplicatedName() {
        themeService.save(name, description, thumbnail);

        assertThatThrownBy(() -> themeService.save(name, "description", "https://new.com/new.jpg"))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("같은 이름의 테마가 이미 존재합니다.");
    }

    @DisplayName("실패: 예약에 사용되는 테마 삭제 시도 시 예외 발생")
    @Test
    void delete_ReservationExists() {
        Member member = memberRepository.save(new Member("러너덕", "deock@test.com", "123a!", Role.USER));
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "https://test.com/test1.jpg"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime("10:00"));
        reservationRepository.save(new Reservation(
            member, LocalDate.parse("2060-01-01"), LocalDateTime.now(), time, theme, ReservationStatus.RESERVED));

        assertThatThrownBy(() -> themeService.delete(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }
}
