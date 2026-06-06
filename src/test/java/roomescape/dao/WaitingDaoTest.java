package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.config.FixedClockConfig.NOW_TIME;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.WaitingQueryResult;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;

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
        Waiting waiting = new Waiting(userName, date, time, theme, createdAt);

        // when
        Waiting saved = waitingDao.save(waiting);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(waiting.getName());
        assertThat(saved.getCreatedAt()).isEqualTo(waiting.getCreatedAt());
        assertThat(saved.getDate()).isEqualTo(waiting.getDate());
        assertThat(saved.getTime()).isEqualTo(waiting.getTime());
        assertThat(saved.getTheme()).isEqualTo(waiting.getTheme());
    }

    @Test
    void 예약_대기_삭제_정상_테스트() {
        Long id = 1L;

        assertDoesNotThrow(() -> waitingDao.delete(id));
        assertThat(waitingDao.findById(id)).isEmpty();
    }

    @Test
    void 예약_대기_사용자_이름_조회_정상_테스트() {
        String userName = "토리";

        List<WaitingQueryResult> waitings = waitingDao.findAllByUserName(userName);

        assertThat(waitings.size()).isEqualTo(1);
    }

    @Test
    void 사용자가_동일한_예약에_이미_대기_중이면_True를_반환한다() {
        Waiting waiting = new Waiting(userName, date, time, theme, createdAt);
        waitingDao.save(waiting);

        assertThat(waitingDao.existsBySlotAndName(
                waiting.getName().value(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        )).isTrue();
    }

    @Test
    void 해당_예약에_대기가_존재하지_않으면_False를_반환한다() {
        Waiting waiting = new Waiting(userName, date, time, theme, createdAt);

        assertThat(waitingDao.existsBySlotAndName(
                waiting.getName().value(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        )).isFalse();
    }

    @DisplayName("해당 테마를 사용하는 예약 대기가 있으면 existsByThemeId는 true를 반환한다.")
    @Test
    void existsByThemeIdResultTrue() {
        assertThat(waitingDao.existsByThemeId(1L)).isTrue();
    }

    @Test
    @DisplayName("해당 테마를 사용하는 예약 대기가 없으면 existsByThemeId는 false를 반환한다.")
    void existsByThemeIdResultFalse() {
        assertThat(waitingDao.existsByThemeId(15L)).isFalse();
    }

    @Test
    @DisplayName("해당 시간을 사용하는 예약 대기가 있으면 existsByTimeId는 true를 반환한다.")
    void existsByTimeIdResultTrue() {
        assertThat(waitingDao.existsByTimeId(5L)).isTrue();
    }

    @Test
    @DisplayName("해당 시간을 사용하는 예약 대기가 없으면 existsByTimeId는 false를 반환한다.")
    void existsByTimeIdResultFalse() {
        assertThat(waitingDao.existsByTimeId(1L)).isFalse();
    }

    @Test
    void 슬롯에_대기가_없으면_findFirstBySlot은_빈_Optional을_반환한다() {
        assertThat(waitingDao.findFirstBySlot(LocalDate.parse(TODAY), 1L, 1L))
                .isEmpty();
    }

    @Test
    void 슬롯에_대기자가_한_명이면_해당_대기를_반환한다() {
        Waiting first = waitingDao.findFirstBySlot(LocalDate.parse("2026-05-12"), 5L, 1L)
                .orElseThrow();

        assertThat(first.getName().value()).isEqualTo("토리");
    }

    @Test
    void 같은_슬롯에_대기가_여러_명이면_created_at이_빠른_사람을_반환한다() {
        LocalDate slotDate = LocalDate.parse("2026-05-04");
        ReservationTime slotTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme slotTheme = new Theme(
                2L,
                ThemeName.parse("미래 도시"),
                Description.parse("2050년 서울의 이야기"),
                ThumbnailUrl.parse("/images/future-city")
        );

        Waiting earlier = new Waiting(
                UserName.parse("먼저"),
                slotDate, slotTime, slotTheme,
                LocalDateTime.of(2026, 5, 1, 9, 0)
        );
        Waiting later = new Waiting(
                UserName.parse("나중"),
                slotDate, slotTime, slotTheme,
                LocalDateTime.of(2026, 5, 1, 10, 0)
        );
        waitingDao.save(earlier);
        waitingDao.save(later);

        Waiting first = waitingDao.findFirstBySlot(slotDate, slotTime.getId(), slotTheme.getId())
                .orElseThrow();

        assertThat(first.getName().value()).isEqualTo("먼저");
    }

    @Test
    void 같은_슬롯_대기의_created_at이_동일하면_id가_작은_대기를_반환한다() {
        LocalDate slotDate = LocalDate.parse("2026-05-04");
        ReservationTime slotTime = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDateTime sameCreatedAt = LocalDateTime.of(2026, 5, 1, 9, 0);
        Theme slotTheme = new Theme(
                2L,
                ThemeName.parse("미래 도시"),
                Description.parse("2050년 서울의 이야기"),
                ThumbnailUrl.parse("/images/future-city")
        );

        Waiting savedFirst = waitingDao.save(new Waiting(
                UserName.parse("먼저저장"),
                slotDate, slotTime, slotTheme,
                sameCreatedAt
        ));
        Waiting savedSecond = waitingDao.save(new Waiting(
                UserName.parse("나중저장"),
                slotDate, slotTime, slotTheme,
                sameCreatedAt
        ));

        Waiting first = waitingDao.findFirstBySlot(slotDate, slotTime.getId(), slotTheme.getId())
                .orElseThrow();

        assertThat(savedFirst.getId()).isLessThan(savedSecond.getId());
        assertThat(first.getId()).isEqualTo(savedFirst.getId());
        assertThat(first.getName().value()).isEqualTo("먼저저장");
    }
}
