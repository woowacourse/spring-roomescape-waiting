package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.FixtureGenerator;

@JdbcTest
@Import({ThemeDao.class, ReservationTimeDao.class, ReservationDao.class, SlotDao.class, WaitingDao.class})
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private SlotDao slotDao;

    @Autowired
    private WaitingDao waitingDao;

    private FixtureGenerator fixture;

    @BeforeEach
    void setUp() {
        fixture = new FixtureGenerator(themeDao, timeDao, slotDao, reservationDao, waitingDao);
    }

    @Test
    void 테마를_등록한다() {
        // given
        String name = "방탈출1";
        String description = "로지와 러키의 신나는 방탈출";
        String thumbnail = "https://abc.asdfdsa";
        Theme theme = new Theme(name, description, thumbnail);

        // when
        Theme savedTheme = themeDao.save(theme);

        // then
        assertAll(
                () -> assertThat(savedTheme.getId()).isNotNull(),
                () -> assertThat(savedTheme.getName()).isEqualTo(name),
                () -> assertThat(savedTheme.getDescription()).isEqualTo(description),
                () -> assertThat(savedTheme.getThumbnail()).isEqualTo(thumbnail)
        );
    }

    @Test
    void 아이디에_맞는_테마를_조회한다() {
        String name = "방탈출1";
        String description = "로지와 러키의 신나는 방탈출";
        String thumbnail = "https://abc.asdfdsa";
        Theme savedTheme = fixture.saveTheme(name, description, thumbnail);

        // when
        Optional<Theme> foundTheme = themeDao.findById(savedTheme.getId());

        // then
        assertAll(
                () -> assertThat(foundTheme.isPresent()).isTrue(),
                () -> assertThat(foundTheme.get().getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(foundTheme.get().getName()).isEqualTo(name),
                () -> assertThat(foundTheme.get().getDescription()).isEqualTo(description),
                () -> assertThat(foundTheme.get().getThumbnail()).isEqualTo(thumbnail)
        );
    }

    @Test
    void 인기_테마_10개를_조회한다() {
        // given
        setupPopularThemesData();
        LocalDate startDate = LocalDate.of(2026, 4, 29);
        LocalDate endDate = LocalDate.of(2026, 5, 5);

        // when
        List<Theme> popularThemes = themeDao.findPopularThemesByPeriod(startDate, endDate);

        // then
        assertAll(
                () -> assertThat(popularThemes.getFirst().getName()).isEqualTo("공포의 저택"),
                () -> assertThat(popularThemes.get(1).getName()).isEqualTo("사라진 연구소"),
                () -> assertThat(popularThemes.get(2).getName()).isEqualTo("시간 여행자"),
                () -> assertThat(popularThemes.get(3).getName()).isEqualTo("감옥 탈출"),
                () -> assertThat(popularThemes.get(4).getName()).isEqualTo("마법사의 방"),
                () -> assertThat(popularThemes.get(5).getName()).isEqualTo("좀비 바이러스"),
                () -> assertThat(popularThemes.get(6).getName()).isEqualTo("해적의 보물"),
                () -> assertThat(popularThemes.get(7).getName()).isEqualTo("스파이 미션"),
                () -> assertThat(popularThemes.get(8).getName()).isEqualTo("우주 정거장"),
                () -> assertThat(popularThemes.get(9).getName()).isEqualTo("고대 유적")
        );
    }

    @Test
    void 아이디에_맞는_테마가_없으면_빈_객체를_반환한다() {
        // when
        Optional<Theme> foundTheme = themeDao.findById(0L);

        // then
        assertThat(foundTheme).isEmpty();
    }

    private void setupPopularThemesData() {
        ReservationTime time1 = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = fixture.saveReservationTime(LocalTime.of(11, 0));
        ReservationTime time3 = fixture.saveReservationTime(LocalTime.of(12, 0));
        ReservationTime time4 = fixture.saveReservationTime(LocalTime.of(13, 0));
        ReservationTime time5 = fixture.saveReservationTime(LocalTime.of(14, 0));
        ReservationTime time6 = fixture.saveReservationTime(LocalTime.of(15, 0));
        ReservationTime time7 = fixture.saveReservationTime(LocalTime.of(16, 0));
        ReservationTime time8 = fixture.saveReservationTime(LocalTime.of(17, 0));
        ReservationTime time9 = fixture.saveReservationTime(LocalTime.of(18, 0));

        Theme theme1 = fixture.saveTheme("공포의 저택", "오래된 저택에서 탈출하세요", "https://example.com/theme1.jpg");
        Theme theme2 = fixture.saveTheme("사라진 연구소", "비밀 연구소의 진실을 밝혀내세요", "https://example.com/theme2.jpg");
        Theme theme3 = fixture.saveTheme("시간 여행자", "시간의 틈에서 탈출하세요", "https://example.com/theme3.jpg");
        Theme theme4 = fixture.saveTheme("감옥 탈출", "제한 시간 안에 감옥을 탈출하세요", "https://example.com/theme4.jpg");
        Theme theme5 = fixture.saveTheme("마법사의 방", "마법사의 숨겨진 방을 탐험하세요", "https://example.com/theme5.jpg");
        Theme theme6 = fixture.saveTheme("좀비 바이러스", "바이러스가 퍼진 도시에서 살아남으세요", "https://example.com/theme6.jpg");
        Theme theme7 = fixture.saveTheme("해적의 보물", "해적선에 숨겨진 보물을 찾으세요", "https://example.com/theme7.jpg");
        Theme theme8 = fixture.saveTheme("스파이 미션", "비밀 요원이 되어 임무를 완수하세요", "https://example.com/theme8.jpg");
        Theme theme9 = fixture.saveTheme("우주 정거장", "고장난 우주 정거장에서 탈출하세요", "https://example.com/theme9.jpg");
        Theme theme10 = fixture.saveTheme("고대 유적", "고대 유적의 수수께끼를 풀어보세요", "https://example.com/theme10.jpg");
        Theme theme11 = fixture.saveTheme("미스터리 호텔", "호텔에서 벌어진 사건을 해결하세요", "https://example.com/theme11.jpg");
        Theme theme12 = fixture.saveTheme("지하 벙커", "폐쇄된 지하 벙커에서 탈출하세요", "https://example.com/theme12.jpg");

        LocalDate date1 = LocalDate.of(2026, 5, 5);
        LocalDate date2 = LocalDate.of(2026, 5, 4);
        LocalDate date3 = LocalDate.of(2026, 5, 3);
        LocalDate date4 = LocalDate.of(2026, 5, 2);
        LocalDate date5 = LocalDate.of(2026, 5, 1);
        LocalDate date6 = LocalDate.of(2026, 4, 30);
        LocalDate date7 = LocalDate.of(2026, 4, 29);
        LocalDate oldDate = LocalDate.of(2026, 4, 20);

        // theme1: 12건
        fixture.saveReservation("예약자", date1, time1, theme1);
        fixture.saveReservation("예약자", date1, time2, theme1);
        fixture.saveReservation("예약자", date1, time3, theme1);
        fixture.saveReservation("예약자", date1, time4, theme1);
        fixture.saveReservation("예약자", date1, time5, theme1);
        fixture.saveReservation("예약자", date2, time1, theme1);
        fixture.saveReservation("예약자", date2, time2, theme1);
        fixture.saveReservation("예약자", date3, time3, theme1);
        fixture.saveReservation("예약자", date3, time4, theme1);
        fixture.saveReservation("예약자", date4, time5, theme1);
        fixture.saveReservation("예약자", date5, time6, theme1);
        fixture.saveReservation("예약자", date6, time7, theme1);

        // theme2: 10건
        fixture.saveReservation("예약자", date1, time1, theme2);
        fixture.saveReservation("예약자", date1, time2, theme2);
        fixture.saveReservation("예약자", date1, time3, theme2);
        fixture.saveReservation("예약자", date2, time4, theme2);
        fixture.saveReservation("예약자", date2, time5, theme2);
        fixture.saveReservation("예약자", date3, time6, theme2);
        fixture.saveReservation("예약자", date3, time7, theme2);
        fixture.saveReservation("예약자", date4, time8, theme2);
        fixture.saveReservation("예약자", date5, time9, theme2);
        fixture.saveReservation("예약자", date6, time1, theme2);

        // theme3: 9건
        fixture.saveReservation("예약자", date1, time1, theme3);
        fixture.saveReservation("예약자", date1, time2, theme3);
        fixture.saveReservation("예약자", date1, time3, theme3);
        fixture.saveReservation("예약자", date2, time4, theme3);
        fixture.saveReservation("예약자", date3, time5, theme3);
        fixture.saveReservation("예약자", date4, time6, theme3);
        fixture.saveReservation("예약자", date5, time7, theme3);
        fixture.saveReservation("예약자", date6, time8, theme3);
        fixture.saveReservation("예약자", date7, time9, theme3);

        // theme4: 8건
        fixture.saveReservation("예약자", date1, time1, theme4);
        fixture.saveReservation("예약자", date1, time2, theme4);
        fixture.saveReservation("예약자", date2, time3, theme4);
        fixture.saveReservation("예약자", date3, time4, theme4);
        fixture.saveReservation("예약자", date4, time5, theme4);
        fixture.saveReservation("예약자", date5, time6, theme4);
        fixture.saveReservation("예약자", date6, time7, theme4);
        fixture.saveReservation("예약자", date7, time8, theme4);

        // theme5: 7건
        fixture.saveReservation("예약자", date1, time1, theme5);
        fixture.saveReservation("예약자", date2, time2, theme5);
        fixture.saveReservation("예약자", date3, time3, theme5);
        fixture.saveReservation("예약자", date4, time4, theme5);
        fixture.saveReservation("예약자", date5, time5, theme5);
        fixture.saveReservation("예약자", date6, time6, theme5);
        fixture.saveReservation("예약자", date7, time7, theme5);

        // theme6: 6건
        fixture.saveReservation("예약자", date1, time1, theme6);
        fixture.saveReservation("예약자", date2, time2, theme6);
        fixture.saveReservation("예약자", date3, time3, theme6);
        fixture.saveReservation("예약자", date4, time4, theme6);
        fixture.saveReservation("예약자", date5, time5, theme6);
        fixture.saveReservation("예약자", date6, time6, theme6);

        // theme7: 5건
        fixture.saveReservation("예약자", date1, time1, theme7);
        fixture.saveReservation("예약자", date2, time2, theme7);
        fixture.saveReservation("예약자", date3, time3, theme7);
        fixture.saveReservation("예약자", date4, time4, theme7);
        fixture.saveReservation("예약자", date5, time5, theme7);

        // theme8: 4건
        fixture.saveReservation("예약자", date1, time1, theme8);
        fixture.saveReservation("예약자", date2, time2, theme8);
        fixture.saveReservation("예약자", date3, time3, theme8);
        fixture.saveReservation("예약자", date4, time4, theme8);

        // theme9: 3건
        fixture.saveReservation("예약자", date1, time1, theme9);
        fixture.saveReservation("예약자", date2, time2, theme9);
        fixture.saveReservation("예약자", date3, time3, theme9);

        // theme10: 2건
        fixture.saveReservation("예약자", date1, time1, theme10);
        fixture.saveReservation("예약자", date2, time2, theme10);

        // theme11: 1건
        fixture.saveReservation("예약자", date1, time1, theme11);

        // theme12: 11건 but outside period
        fixture.saveReservation("예약자", oldDate, time1, theme12);
        fixture.saveReservation("예약자", oldDate, time2, theme12);
        fixture.saveReservation("예약자", oldDate, time3, theme12);
        fixture.saveReservation("예약자", oldDate, time4, theme12);
        fixture.saveReservation("예약자", oldDate, time5, theme12);
        fixture.saveReservation("예약자", oldDate, time6, theme12);
        fixture.saveReservation("예약자", oldDate, time7, theme12);
        fixture.saveReservation("예약자", oldDate, time8, theme12);
        fixture.saveReservation("예약자", oldDate, time9, theme12);
        fixture.saveReservation("예약자", oldDate, time1, theme12);
        fixture.saveReservation("예약자", oldDate, time2, theme12);
    }

    }
