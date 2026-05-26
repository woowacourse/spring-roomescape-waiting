package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    @DisplayName("전체 테마를 조회하면 전체 테마를 반환한다")
    void findAllThemes_returns15Themes() {
        List<ThemeResponse> result = themeService.findAllThemes();

        assertThat(result).hasSize(15);
    }


    @Test
    @DisplayName("인기 테마 상위 3개는 예약 수 기준으로 출력한다")
    void findTopTheme_returnsTop3InOrder() {
        List<ThemeResponse> result = themeService.findTopTheme(3L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("우테코 공포물");
        assertThat(result.get(1).name()).isEqualTo("미래 도시");
        assertThat(result.get(2).name()).isEqualTo("고대 이집트");
    }

    @Test
    @DisplayName("테마를 생성하면 전체 테마 수가 1 증가한다")
    void create_increasesThemeCountByOne() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes()
        );
        ThemeRequest request = new ThemeRequest("새 테마", "새 테마 설명", file);

        themeService.create(request);

        assertThat(themeService.findAllThemes()).hasSize(16);
    }


    @Test
    @DisplayName("예약이 없는 테마를 삭제할 수 있다")
    void delete_decreasesThemeCountByOne() {
        themeService.delete(11L);

        assertThat(themeService.findAllThemes()).hasSize(14);
    }


    @Test
    @DisplayName("theme1의 6일 전 날짜는 time_id 1~5가 예약되어 있어 가용 시간은 4개다")
    void findAvailableTime_returns4TimesForTheme1() {
        String date = LocalDate.now().minusDays(6).toString();

        List<ReservationTimeResponse> result = themeService.findAvailableTime(1L, date);

        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("예약이 전혀 없는 날짜는 모든 시간에 예약할 수 있다")
    void findAvailableTime_returnsAllTimesWhenNoReservation() {

        String date = LocalDate.now().plusDays(30).toString();

        List<ReservationTimeResponse> result = themeService.findAvailableTime(1L, date);

        assertThat(result).hasSize(9);
    }
}
