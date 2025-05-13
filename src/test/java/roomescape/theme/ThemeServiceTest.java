package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.reservation.FakeReservationRepository;
import roomescape.reservation.Reservation;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

class ThemeServiceTest {


    private final ThemeService themeService;
    private final FakeThemeRepository fakeThemeRepository;
    private final FakeReservationRepository fakeReservationRepository;

    public ThemeServiceTest() {
        fakeThemeRepository = new FakeThemeRepository();
        fakeReservationRepository = new FakeReservationRepository();
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);
    }

    @BeforeEach
    void setUp() {
        fakeThemeRepository.clear();
        fakeReservationRepository.clear();
    }

    @Nested
    @DisplayName("테마 생성")
    class Create {

        @DisplayName("테마를 생성한다.")
        @Test
        void create() {
            // given
            final ThemeRequest themeRequest = new ThemeRequest("로키", "로키로키", "http://www.google.com");
            final ThemeResponse expect = new ThemeResponse(1L, "로키", "로키로키", "http://www.google.com");

            // when
            final ThemeResponse actual = themeService.create(themeRequest);

            // then
            assertThat(actual).isEqualTo(expect);
        }
    }

    @Nested
    @DisplayName("테마 모두 조회")
    class FindAll {

        @DisplayName("테마를 모두 조회한다.")
        @Test
        void findAll1() {
            // given
            fakeThemeRepository.save(new Theme("로키1", "로키로키1", "http://www.google.com/1"));
            fakeThemeRepository.save(new Theme("로키2", "로키로키2", "http://www.google.com/2"));
            fakeThemeRepository.save(new Theme("로키3", "로키로키3", "http://www.google.com/3"));

            // when
            final List<ThemeResponse> actual = themeService.findAll();

            // then
            assertThat(actual).hasSize(3);
        }

        @DisplayName("테마가 없다면 빈 컬렉션을 반환한다.")
        @Test
        void findAll2() {
            // given & when
            final List<ThemeResponse> actual = themeService.findAll();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("주어진 사이즈만큼 일주일간의 예약이 많은 순서대로 탑 랭크 조회")
    class FindTopRankThemes {

        @DisplayName("탑 랭크 조회")
        @Test
        void findTopRankThemes() {
            // given
            final List<Theme> themes = List.of(
                    new Theme(1L, "1", "2", "3"),
                    new Theme(2L, "1", "2", "3"),
                    new Theme(3L, "1", "2", "3"),
                    new Theme(4L, "1", "2", "3"),
                    new Theme(5L, "1", "2", "3")
            );
            fakeThemeRepository.stubFindAllOrderByRank(themes);

            // when
            final List<ThemeResponse> actual = themeService.findTopRankThemes(5);

            // then
            assertThat(actual).hasSize(5);
        }

    }

    @Nested
    @DisplayName("테마 삭제")
    class Delete {

        @DisplayName("주어진 id에 해당하는 테마를 삭제한다.")
        @Test
        void deleteById1() {
            // given
            fakeThemeRepository.save(new Theme("로키1", "로키로키1", "http://www.google.com/1"));
            final Long id = 1L;

            // when
            themeService.deleteById(id);

            // then
            assertThat(fakeThemeRepository.isInvokeDeleteId(id)).isTrue();
        }

        @DisplayName("주어진 id에 해당하는 테마가 존재하지 않는다면 예외가 발생한다.")
        @Test
        void deleteById2() {
            // given
            final Long id = 1L;

            // when & then
            assertThatThrownBy(() -> {
                themeService.deleteById(id);
            }).isInstanceOf(ThemeNotFoundException.class);
        }

        @DisplayName("주어진 id에 해당하는 테마가 예약에서 사용중이라면 예외가 발생한다.")
        @Test
        void deleteById3() {
            // given
            final Long id = 1L;
            fakeThemeRepository.save(new Theme("로키1", "로키로키1", "http://www.google.com/1"));
            fakeReservationRepository.save(new Reservation(LocalDate.of(2026, 12, 1)), 1L, id, 1L);

            // when & then
            assertThatThrownBy(() -> {
                themeService.deleteById(id);
            }).isInstanceOf(ThemeUsedException.class);
        }
    }
}
