package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import roomescape.domain.Theme;
import roomescape.domain.dto.ThemeRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThemeRepositoryTest {
    @Autowired
    private ThemeRepository themeRepository;

    private long getItemSize() {
        return themeRepository.findAll().size();
    }

    @DisplayName("Db에 등록된 테마 목록을 조회할 수 있다.")
    @Test
    void given_when_findAll_then_ReturnThemes() {
        //given, when, then
        assertThat(themeRepository.findAll().size()).isEqualTo(4);
    }

    @DisplayName("Db에 테마 정보를 저장한다.")
    @Test
    void given_themeRequest_when_create_then_returnCreatedThemeId() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("name", "description", "thumbnail");
        Theme expected = themeRequest.toEntity();
        //when
        Theme savedTheme = themeRepository.save(expected);
        //then
        assertThat(savedTheme).isEqualTo(expected);
    }

    @DisplayName("시간 id로 Db에서 테마 정보를 삭제한다.")
    @Test
    void given_when_delete_then_deletedFromDb() {
        //given
        long initialSize = getItemSize();
        //when
        themeRepository.deleteById(4L);
        long afterSize = getItemSize();
        //then
        assertThat(afterSize).isEqualTo(initialSize - 1);
    }

    @DisplayName("Id를 통해 Theme 객체를 반환할 수 있다.")
    @Test
    void given_when_findById_then_returnOptionalTheme() {
        //given, when, then
        assertThat(themeRepository.findById(1L).get().getId()).isEqualTo(1);
    }

    @DisplayName("등록된 테마 중 시작과 종료 기간을 지정하면 예약이 많은 순서로 테마가 반환된다.")
    @Test
    void given_when_findPopularThemeByDate_then_returnSortedThemes() {
        //given
        LocalDate startDate = LocalDate.parse("2024-04-30");
        LocalDate endDate = LocalDate.parse("2024-05-02");
        Pageable pageable = PageRequest.of(0, 10);
        //when
        final List<Theme> popularThemeByDate = themeRepository.findPopularThemeByDate(startDate, endDate, pageable);
        assertThat(popularThemeByDate.get(0).getId()).isEqualTo(2);
    }
}
