package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;
import roomescape.repository.ThemeDao;

@JdbcTest
@Import(ThemeDao.class)
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @DisplayName("테마를 저장하면 생성된 ID를 반환한다.")
    @Test
    void 테마_저장() {
        Long id = themeDao.save("테스트 테마", "테스트 설명", "https://example.com/image.jpg");

        assertThat(id).isNotNull().isPositive();
    }

    @DisplayName("전체 테마 목록을 조회하면 초기 데이터 4건이 반환된다.")
    @Test
    void 전체_테마_조회() {
        List<Theme> themes = themeDao.findAll();

        assertThat(themes).hasSize(4);
    }

    @DisplayName("인기 테마를 예약 수 내림차순으로 조회한다.")
    @Test
    void 인기_테마_조회() {
        // data.sql 기준 2026-04-22 ~ 2026-05-26 구간 스케줄 수:
        //   공포의 저택(1) 4건 / 탐정 사무소(4) 4건 / 마법사의 연구실(3) 3건 / 우주 정거장(2) 2건
        LocalDate from = LocalDate.of(2026, 4, 22);
        LocalDate to = LocalDate.of(2026, 5, 26);

        List<Theme> popularThemes = themeDao.findPopularThemes(4, from, to);

        assertThat(popularThemes).hasSize(4);
        // 3위: 마법사의 연구실 (3건)
        assertThat(popularThemes.get(2).getName()).isEqualTo("마법사의 연구실");
        // 4위: 우주 정거장 (2건)
        assertThat(popularThemes.get(3).getName()).isEqualTo("우주 정거장");
        // 1·2위는 공포의 저택·탐정 사무소 동점 (DB 반환 순서 비결정)
        assertThat(popularThemes.subList(0, 2))
                .extracting(Theme::getName)
                .containsExactlyInAnyOrder("공포의 저택", "탐정 사무소");
    }

    @DisplayName("size 제한을 적용하면 그 수만큼만 반환된다.")
    @Test
    void 인기_테마_size_제한() {
        LocalDate from = LocalDate.of(2026, 4, 22);
        LocalDate to = LocalDate.of(2026, 5, 26);

        List<Theme> popularThemes = themeDao.findPopularThemes(2, from, to);

        assertThat(popularThemes).hasSize(2);
    }

    @DisplayName("예약이 있는 시간대는 isAvailable=false이고 대기 수가 표시된다.")
    @Test
    void 이용_가능_시간_조회_예약_있음() {
        // schedule ID 1: 2026-04-29 / theme 1 / time_id=3(12:00)
        // reservation 1·23 모두 RESERVED → available=false, waitNumber=1(= 2-1)
        List<AvailableTimeResponse> times = themeDao.findAvailableTimeById(1L, "2026-04-29");

        assertThat(times).hasSize(13);

        AvailableTimeResponse bookedSlot = times.stream()
                .filter(t -> t.startAt().equals(LocalTime.of(12, 0)))
                .findFirst()
                .orElseThrow();

        assertThat(bookedSlot.isAvailable()).isFalse();
        assertThat(bookedSlot.waitNumber()).isEqualTo(1);
    }

    @DisplayName("예약이 없는 날짜는 모든 시간이 이용 가능하고 대기 수는 0이다.")
    @Test
    void 이용_가능_시간_조회_예약_없음() {
        List<AvailableTimeResponse> times = themeDao.findAvailableTimeById(1L, "2099-12-31");

        assertThat(times).hasSize(13)
                .allMatch(AvailableTimeResponse::isAvailable)
                .allMatch(t -> t.waitNumber() == 0);
    }

    @DisplayName("테마를 삭제하면 전체 목록에서 제거된다.")
    @Test
    void 테마_삭제() {
        Long id = themeDao.save("삭제용 테마", "설명", "https://example.com/image.jpg");

        themeDao.delete(id);

        assertThat(themeDao.findAll()).hasSize(4);
    }
}
