package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ThemeRepository.class, ReservationTimeRepository.class, ReservationRepository.class})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 테마를_등록한다() {
        // when
        Theme saved = saveTheme("방탈출1", "로지와 러키의 신나는 방탈출", "https://abc.asdfdsa");

        // then
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getName()).isEqualTo("방탈출1"),
                () -> assertThat(saved.getDescription()).isEqualTo("로지와 러키의 신나는 방탈출"),
                () -> assertThat(saved.getThumbnail()).isEqualTo("https://abc.asdfdsa")
        );
    }

    @Test
    void 전체_테마를_조회한다() {
        // given
        saveTheme("방탈출1", "설명1", "https://thumb1.com");
        saveTheme("방탈출2", "설명2", "https://thumb2.com");

        // when
        List<Theme> themes = themeRepository.selectAll();

        // then
        assertThat(themes).hasSize(2);
    }

    @Test
    void 아이디에_맞는_테마를_조회한다() {
        // given
        Theme saved = saveTheme("방탈출1", "로지와 러키의 신나는 방탈출", "https://abc.asdfdsa");

        // when
        Optional<Theme> found = themeRepository.selectById(saved.getId());

        // then
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.get().getName()).isEqualTo(saved.getName()),
                () -> assertThat(found.get().getDescription()).isEqualTo(saved.getDescription()),
                () -> assertThat(found.get().getThumbnail()).isEqualTo(saved.getThumbnail())
        );
    }

    @Test
    void 인기_테마_10개를_조회한다() {
        // given
        setupPopularThemesData();
        LocalDate startDate = LocalDate.of(2026, 4, 29);
        LocalDate endDate = LocalDate.of(2026, 5, 5);

        // when
        List<Theme> popular = themeRepository.selectPopularThemesByPeriod(startDate, endDate);

        // then
        assertAll(
                () -> assertThat(popular.getFirst().getName()).isEqualTo("공포의 저택"),
                () -> assertThat(popular.get(1).getName()).isEqualTo("사라진 연구소"),
                () -> assertThat(popular.get(2).getName()).isEqualTo("시간 여행자"),
                () -> assertThat(popular.get(3).getName()).isEqualTo("감옥 탈출"),
                () -> assertThat(popular.get(4).getName()).isEqualTo("마법사의 방"),
                () -> assertThat(popular.get(5).getName()).isEqualTo("좀비 바이러스"),
                () -> assertThat(popular.get(6).getName()).isEqualTo("해적의 보물"),
                () -> assertThat(popular.get(7).getName()).isEqualTo("스파이 미션"),
                () -> assertThat(popular.get(8).getName()).isEqualTo("우주 정거장"),
                () -> assertThat(popular.get(9).getName()).isEqualTo("고대 유적")
        );
    }

    @Test
    void 아이디에_맞는_테마가_없으면_빈_객체를_반환한다() {
        // when
        Optional<Theme> found = themeRepository.selectById(900L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme saved = saveTheme("방탈출1", "설명", "https://thumb.com");

        // when
        int deleted = themeRepository.delete(saved.getId());

        // then
        assertThat(deleted).isEqualTo(1);
    }

    private void setupPopularThemesData() {
        ReservationTime time1 = saveTime(10, 0);
        ReservationTime time2 = saveTime(11, 0);
        ReservationTime time3 = saveTime(12, 0);
        ReservationTime time4 = saveTime(13, 0);
        ReservationTime time5 = saveTime(14, 0);
        ReservationTime time6 = saveTime(15, 0);
        ReservationTime time7 = saveTime(16, 0);
        ReservationTime time8 = saveTime(17, 0);
        ReservationTime time9 = saveTime(18, 0);

        Theme theme1 = saveTheme("공포의 저택", "오래된 저택에서 탈출하세요", "https://example.com/theme1.jpg");
        Theme theme2 = saveTheme("사라진 연구소", "비밀 연구소의 진실을 밝혀내세요", "https://example.com/theme2.jpg");
        Theme theme3 = saveTheme("시간 여행자", "시간의 틈에서 탈출하세요", "https://example.com/theme3.jpg");
        Theme theme4 = saveTheme("감옥 탈출", "제한 시간 안에 감옥을 탈출하세요", "https://example.com/theme4.jpg");
        Theme theme5 = saveTheme("마법사의 방", "마법사의 숨겨진 방을 탐험하세요", "https://example.com/theme5.jpg");
        Theme theme6 = saveTheme("좀비 바이러스", "바이러스가 퍼진 도시에서 살아남으세요", "https://example.com/theme6.jpg");
        Theme theme7 = saveTheme("해적의 보물", "해적선에 숨겨진 보물을 찾으세요", "https://example.com/theme7.jpg");
        Theme theme8 = saveTheme("스파이 미션", "비밀 요원이 되어 임무를 완수하세요", "https://example.com/theme8.jpg");
        Theme theme9 = saveTheme("우주 정거장", "고장난 우주 정거장에서 탈출하세요", "https://example.com/theme9.jpg");
        Theme theme10 = saveTheme("고대 유적", "고대 유적의 수수께끼를 풀어보세요", "https://example.com/theme10.jpg");
        Theme theme11 = saveTheme("미스터리 호텔", "호텔에서 벌어진 사건을 해결하세요", "https://example.com/theme11.jpg");
        Theme theme12 = saveTheme("지하 벙커", "폐쇄된 지하 벙커에서 탈출하세요", "https://example.com/theme12.jpg");

        LocalDate date1 = LocalDate.of(2026, 5, 5);
        LocalDate date2 = LocalDate.of(2026, 5, 4);
        LocalDate date3 = LocalDate.of(2026, 5, 3);
        LocalDate date4 = LocalDate.of(2026, 5, 2);
        LocalDate date5 = LocalDate.of(2026, 5, 1);
        LocalDate date6 = LocalDate.of(2026, 4, 30);
        LocalDate date7 = LocalDate.of(2026, 4, 29);
        LocalDate oldDate = LocalDate.of(2026, 4, 20);

        // theme1: 12건
        saveReservation(date1, time1, theme1);
        saveReservation(date1, time2, theme1);
        saveReservation(date1, time3, theme1);
        saveReservation(date1, time4, theme1);
        saveReservation(date1, time5, theme1);
        saveReservation(date2, time1, theme1);
        saveReservation(date2, time2, theme1);
        saveReservation(date3, time3, theme1);
        saveReservation(date3, time4, theme1);
        saveReservation(date4, time5, theme1);
        saveReservation(date5, time6, theme1);
        saveReservation(date6, time7, theme1);

        // theme2: 10건
        saveReservation(date1, time1, theme2);
        saveReservation(date1, time2, theme2);
        saveReservation(date1, time3, theme2);
        saveReservation(date2, time4, theme2);
        saveReservation(date2, time5, theme2);
        saveReservation(date3, time6, theme2);
        saveReservation(date3, time7, theme2);
        saveReservation(date4, time8, theme2);
        saveReservation(date5, time9, theme2);
        saveReservation(date6, time1, theme2);

        // theme3: 9건
        saveReservation(date1, time1, theme3);
        saveReservation(date1, time2, theme3);
        saveReservation(date1, time3, theme3);
        saveReservation(date2, time4, theme3);
        saveReservation(date3, time5, theme3);
        saveReservation(date4, time6, theme3);
        saveReservation(date5, time7, theme3);
        saveReservation(date6, time8, theme3);
        saveReservation(date7, time9, theme3);

        // theme4: 8건
        saveReservation(date1, time1, theme4);
        saveReservation(date1, time2, theme4);
        saveReservation(date2, time3, theme4);
        saveReservation(date3, time4, theme4);
        saveReservation(date4, time5, theme4);
        saveReservation(date5, time6, theme4);
        saveReservation(date6, time7, theme4);
        saveReservation(date7, time8, theme4);

        // theme5: 7건
        saveReservation(date1, time1, theme5);
        saveReservation(date2, time2, theme5);
        saveReservation(date3, time3, theme5);
        saveReservation(date4, time4, theme5);
        saveReservation(date5, time5, theme5);
        saveReservation(date6, time6, theme5);
        saveReservation(date7, time7, theme5);

        // theme6: 6건
        saveReservation(date1, time1, theme6);
        saveReservation(date2, time2, theme6);
        saveReservation(date3, time3, theme6);
        saveReservation(date4, time4, theme6);
        saveReservation(date5, time5, theme6);
        saveReservation(date6, time6, theme6);

        // theme7: 5건
        saveReservation(date1, time1, theme7);
        saveReservation(date2, time2, theme7);
        saveReservation(date3, time3, theme7);
        saveReservation(date4, time4, theme7);
        saveReservation(date5, time5, theme7);

        // theme8: 4건
        saveReservation(date1, time1, theme8);
        saveReservation(date2, time2, theme8);
        saveReservation(date3, time3, theme8);
        saveReservation(date4, time4, theme8);

        // theme9: 3건
        saveReservation(date1, time1, theme9);
        saveReservation(date2, time2, theme9);
        saveReservation(date3, time3, theme9);

        // theme10: 2건
        saveReservation(date1, time1, theme10);
        saveReservation(date2, time2, theme10);

        // theme11: 1건
        saveReservation(date1, time1, theme11);

        // theme12: 기간 밖 11건
        saveReservation(oldDate, time1, theme12);
        saveReservation(oldDate, time2, theme12);
        saveReservation(oldDate, time3, theme12);
        saveReservation(oldDate, time4, theme12);
        saveReservation(oldDate, time5, theme12);
        saveReservation(oldDate, time6, theme12);
        saveReservation(oldDate, time7, theme12);
        saveReservation(oldDate, time8, theme12);
        saveReservation(oldDate, time9, theme12);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.insert(Theme.createWithoutId(name, description, thumbnail));
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }

    private void saveReservation(LocalDate date, ReservationTime time, Theme theme) {
        reservationRepository.insert(Reservation.createWithoutId("예약자", new ReservationSlot(date, time, theme)));
    }
}
