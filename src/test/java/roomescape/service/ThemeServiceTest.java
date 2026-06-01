package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.repository.ReservationQueryingDao;
import roomescape.repository.ReservationTimeQueryingDao;
import roomescape.repository.ReservationTimeUpdatingDao;
import roomescape.repository.ReservationUpdatingDao;
import roomescape.repository.ReservationWaitingDao;
import roomescape.repository.SlotDao;
import roomescape.repository.ThemeQueryingDao;
import roomescape.repository.ThemeUpdatingDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({ThemeService.class, ThemeQueryingDao.class, ThemeUpdatingDao.class,
        ReservationService.class, SlotDao.class, ReservationQueryingDao.class, ReservationUpdatingDao.class,
        ReservationTimeQueryingDao.class, ReservationTimeUpdatingDao.class,
        ReservationWaitingDao.class})
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeUpdatingDao reservationTimeUpdatingDao;

    @Test
    void 테마_생성_성공() {
        ThemeRequest request = new ThemeRequest("인형의 집", "공포 테마", "http://example.com");

        ThemeResponse saved = themeService.create(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("인형의 집");
    }

    @Test
    void 전체_테마_조회() {
        themeService.create(new ThemeRequest("무서운 이야기", "공포", "http://example1.com"));
        themeService.create(new ThemeRequest("명탐정의 부재", "탐험", "http://example2.com"));

        List<ThemeResponse> result = themeService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 테마_삭제() {
        ThemeResponse saved = themeService.create(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));

        themeService.delete(saved.getId());

        assertThat(themeService.findAll()).isEmpty();
    }

    @Test
    void 예약이_존재하는_테마_삭제시_예외가_발생한다() {
        ThemeResponse savedTheme = themeService.create(new ThemeRequest("명탐정의 부재", "탐험", "http://example.com"));
        Long timeId = reservationTimeUpdatingDao.insert(new ReservationTimeRequest(LocalTime.of(10, 0)));
        reservationService.create(new ReservationRequest("브라운", LocalDate.now().plusDays(1), timeId, savedTheme.getId()));

        assertThatThrownBy(() -> themeService.delete(savedTheme.getId()))
                .isInstanceOf(ReferencedDataException.class);
    }
}
