package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.exception.DomainErrorCode.REFERENTIAL_INTEGRITY;
import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ThemeRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Transactional
@SpringBootTest
class ThemeServiceTest {

    private static final String THEME_NAME = "방탈출 제목";
    private static final String THEME_DESCRIPTION = "방탈출 설명";
    private static final String THEME_THUMBNAIL = "thumbnail.png";

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void 테마를_추가한다() {
        ThemeRequest request = new ThemeRequest(
                THEME_NAME,
                THEME_DESCRIPTION,
                THEME_THUMBNAIL
        );

        Theme theme = themeService.addTheme(request);

        assertThat(theme.getId()).isNotNull();
        assertThat(theme.getName()).isEqualTo(THEME_NAME);
        assertThat(theme.getDescription()).isEqualTo(THEME_DESCRIPTION);
        assertThat(theme.getThumbnailImageUrl()).isEqualTo(THEME_THUMBNAIL);
    }

    @Test
    void 모든_테마를_조회한다() {
        ThemeRequest request = new ThemeRequest(
                THEME_NAME,
                THEME_DESCRIPTION,
                THEME_THUMBNAIL
        );
        themeService.addTheme(request);

        List<Theme> themes = themeService.getThemes();

        assertThat(themes).hasSize(1);

        Theme theme = themes.getFirst();
        assertThat(theme.getId()).isNotNull();
        assertThat(theme.getName()).isEqualTo(THEME_NAME);
        assertThat(theme.getDescription()).isEqualTo(THEME_DESCRIPTION);
        assertThat(theme.getThumbnailImageUrl()).isEqualTo(THEME_THUMBNAIL);
    }

    @Test
    void id에_맞는_테마를_조회한다() {
        ThemeRequest request = new ThemeRequest(
                THEME_NAME,
                THEME_DESCRIPTION,
                THEME_THUMBNAIL
        );
        Long saveId = themeService.addTheme(request).getId();

        Theme theme = themeService.getTheme(saveId);

        assertThat(theme.getId()).isNotNull();
        assertThat(theme.getName()).isEqualTo(THEME_NAME);
        assertThat(theme.getDescription()).isEqualTo(THEME_DESCRIPTION);
        assertThat(theme.getThumbnailImageUrl()).isEqualTo(THEME_THUMBNAIL);
    }

    @Test
    void 테마를_삭제한다() {
        ThemeRequest request = new ThemeRequest(
                THEME_NAME,
                THEME_DESCRIPTION,
                THEME_THUMBNAIL
        );
        Long saveId = themeService.addTheme(request).getId();

        themeService.deleteTheme(saveId);

        assertThatRoomEscapeExceptionCode(
                () -> themeService.getTheme(saveId),
                THEME_NOT_FOUND
        );
    }

    @Test
    void 없는_테마를_삭제할_수_없다() {
        assertThatRoomEscapeExceptionCode(
                () -> themeService.deleteTheme(1L),
                THEME_NOT_FOUND
        );
    }

    @Test
    void 예약_목록에_삭제할_테마가_존재한다면_테마를_삭제할_수_없다() {
        Long timeSaveId = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(timeSaveId);
        Theme theme = themeService.addTheme(new ThemeRequest(
                THEME_NAME,
                THEME_DESCRIPTION,
                THEME_THUMBNAIL
        ));

        reservationRepository.save(new Reservation(
                "브라운",
                LocalDate.now().plusDays(1),
                reservationTime.orElseThrow(),
                theme
        ));

        assertThatRoomEscapeExceptionCode(
                () -> themeService.deleteTheme(theme.getId()),
                REFERENTIAL_INTEGRITY
        );
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 최근_일주일_동안_예약_수가_많은_인기_테마_탑10_조회() {
        List<Theme> popularTop10Themes = themeService.getPopularTop10Themes(LocalDate.now(), 7);

        assertThat(popularTop10Themes)
                .hasSize(10)
                .extracting(Theme::getName)
                .containsExactly(
                        "미스터리 저택",
                        "해적선의 보물",
                        "마법사의 탑",
                        "좀비 아포칼립스",
                        "고대 이집트",
                        "우주 정거장",
                        "시간 여행자의 실험실",
                        "폐쇄 병동",
                        "침몰하는 잠수함",
                        "은행 금고"
                );
    }

    private void assertThatRoomEscapeExceptionCode(ThrowingCallable callable, DomainErrorCode expectedCode) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(RoomEscapeException.class,
                        exception -> assertThat(exception.code()).isEqualTo(expectedCode));
    }
}
