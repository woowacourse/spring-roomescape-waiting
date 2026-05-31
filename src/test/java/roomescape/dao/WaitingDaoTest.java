package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.config.FixedClockConfig.NOW_TIME;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.WaitingQueryResult;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.domain.waiting.Waiting;

@JdbcTest
@Import(WaitingDao.class)
public class WaitingDaoTest {

    private final UserName userName = UserName.parse("아나키");
    private final LocalDate date = LocalDate.parse(TODAY);
    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final ThemeName themeName = ThemeName.parse("공포");
    private final Description description = Description.parse("너무무서워");
    private final ThumbnailUrl url = ThumbnailUrl.parse("/images/horror");
    private final Theme theme = new Theme(1L, themeName, description, url);
    private final LocalDateTime createdAt = LocalDateTime.of(
            LocalDate.parse(TODAY),
            LocalTime.parse(NOW_TIME)
    );

    @Autowired
    private WaitingDao waitingDao;

    @Test
    void 예약_대기를_생성할_수_있다() {
        // given
        EventSlot originEventSlot = EventSlot.from(date, time, theme);
        Waiting waiting = new Waiting(userName, originEventSlot, createdAt);

        // when
        Waiting saved = waitingDao.save(waiting);
        EventSlot savedEventSlot = saved.getSlot();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(waiting.getName());
        assertThat(saved.getCreatedAt()).isEqualTo(waiting.getCreatedAt());
        assertThat(savedEventSlot.date()).isEqualTo(originEventSlot.date());
        assertThat(savedEventSlot.time()).isEqualTo(originEventSlot.time());
        assertThat(savedEventSlot.theme()).isEqualTo(originEventSlot.theme());
    }

    @Test
    void 예약_대기_삭제_정상_테스트() {
        Long id = 1L;

        assertDoesNotThrow(() -> waitingDao.delete(id));
    }

    @Test
    void 예약_대기_사용자_이름_조회_정상_테스트() {
        String userName = "토리";

        List<WaitingQueryResult> waitings = waitingDao.findByUserName(userName);

        assertThat(waitings.size()).isEqualTo(1);
    }

    @Test
    void 동일한_슬롯에_대기하면_빠른_사람이_순번이_빠르다() {
        List<WaitingQueryResult> results = waitingDao.findByUserName("토리");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).sequence()).isEqualTo(1);

        results = waitingDao.findByUserName("로운");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).sequence()).isEqualTo(2);
    }
}
