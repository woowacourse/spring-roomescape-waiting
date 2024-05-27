package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.domain.dto.ThemeRequest;
import roomescape.domain.dto.ThemeResponse;
import roomescape.exception.DeleteNotAllowException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ThemeServiceTest {
    private final ThemeService service;

    @Autowired
    public ThemeServiceTest(final ThemeService service) {
        this.service = service;
    }

    private long getThemeSize() {
        return service.findAll().getData().size();
    }

    @Test
    @DisplayName("테마 목록을 반환한다.")
    void given_when_findAll_then_returnThemeResponses() {
        //when, then
        assertThat(service.findAll().getData().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("테마 등록이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_themeRequestWithInitialSize_when_register_then_returnThemeResponseAndSaveDb() {
        //given
        long initialSize = getThemeSize();
        ThemeRequest themeRequest = new ThemeRequest("test", "testDescription", "testThumbnail");
        //when
        final ThemeResponse themeResponse = service.register(themeRequest);
        long afterCreateSize = getThemeSize();
        //then
        assertThat(themeResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("존재하는 테마를 삭제하면 Db에도 삭제된다.")
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getThemeSize();
        //when
        service.delete(initialSize);
        long afterCreateSize = getThemeSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    @Test
    @DisplayName("예약이 되어있는 테마를 지울 경우 예외를 발생시키고 Db에 반영하지 않는다.")
    void given_initialSize_when_registerWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getThemeSize();
        //when, then
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(DeleteNotAllowException.class);
        assertThat(getThemeSize()).isEqualTo(initialSize);

    }

    @DisplayName("기간이 주어지면 가장 많이 예약한 테마 목록 순으로 조회 결과가 반환된다.")
    @Test
    void givenStartDateEndDateCount_when_getPopularThemeListAndGetFirst_then_findMostReservedTheme() {
        //given
        LocalDate startDate = LocalDate.parse("2024-04-30");
        LocalDate endDate = LocalDate.parse("2024-05-02");
        Long count = 10L;
        //when
        final ResponsesWrapper<ThemeResponse> popularThemeList = service.findPopularTheme(startDate, endDate, count);
        //then
        assertThat(popularThemeList.getData().get(0).id()).isEqualTo(2L);
    }
}