package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.AlreadyInUseException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 인기_테마_상위_3개_조회() {
        Theme theme1 = themeDao.save(new Theme("인기테마1", "설명", "url"));
        Theme theme2 = themeDao.save(new Theme("인기테마2", "설명", "url"));
        Theme theme3 = themeDao.save(new Theme("인기테마3", "설명", "url"));
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));

        reservationDao.save(new Reservation("A", LocalDate.now().minusDays(1), time, theme1, ReservationStatus.CONFIRMED));
        reservationDao.save(new Reservation("B", LocalDate.now().minusDays(1), time, theme1, ReservationStatus.CONFIRMED));
        reservationDao.save(new Reservation("C", LocalDate.now().minusDays(1), time, theme2, ReservationStatus.CONFIRMED));
        reservationDao.save(new Reservation("D", LocalDate.now().minusDays(1), time, theme3, ReservationStatus.CONFIRMED));

        List<ThemeResponse> result = themeService.findTopTheme(3L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("인기테마1");
        assertThat(result.get(1).name()).isEqualTo("인기테마2");
    }

    @Test
    void 테마_생성() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes()
        );
        ThemeRequest request = new ThemeRequest("새 테마", "설명", file);

        ThemeResponse created = themeService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("새 테마");
    }

    @Test
    void 예약_없는_테마_삭제() {
        Theme saved = themeDao.save(new Theme("삭제 테마", "설명", "url"));
        themeService.delete(saved.getId());
        
        assertThat(themeDao.findThemeById(saved.getId())).isEmpty();
    }

    @Test
    void 예약_존재하는_테마_삭제_시_예외() {
        Theme theme = themeDao.save(new Theme(null, "사용 테마", "설명", "url"));
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
        reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(AlreadyInUseException.class);
    }
}
