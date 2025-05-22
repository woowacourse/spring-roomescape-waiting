package roomescape.theme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.themeWithId;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private ThemeRepository themeRepository;
    private ThemeService themeService;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        themeRepository = mock(ThemeRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Nested
    @DisplayName("테마 생성")
    class Create {

        @DisplayName("테마를 생성한다.")
        @Test
        void create() {
            // given
            final ThemeRequest themeRequest = new ThemeRequest("로키", "로키로키", "http://www.google.com");
            final Theme theme = new Theme(themeRequest.name(), themeRequest.description(), themeRequest.thumbnail());
            final Theme savedTheme = themeWithId(1L, new Theme(theme.getName(), theme.getDescription(), theme.getThumbnail()));
            given(themeRepository.save(theme))
                    .willReturn(savedTheme);

            // when
            final ThemeResponse actual = themeService.create(themeRequest);

            // then
            assertThat(actual).isEqualTo(ThemeResponse.from(savedTheme));
        }
    }

    @Nested
    @DisplayName("테마 모두 조회")
    class FindAll {

        @DisplayName("테마를 모두 조회한다.")
        @Test
        void findAll1() {
            // given
            final List<Theme> themes = List.of(
                    themeWithId(1L, new Theme("로키1", "로키로키1", "http://www.google.com/1")),
                    themeWithId(2L, new Theme("로키2", "로키로키2", "http://www.google.com/2")),
                    themeWithId(3L, new Theme("로키3", "로키로키3", "http://www.google.com/3"))
            );
            given(themeRepository.findAll())
                    .willReturn(themes);

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
                    themeWithId(1L, new Theme("1", "2", "3")),
                    themeWithId(2L, new Theme("1", "2", "3")),
                    themeWithId(3L, new Theme("1", "2", "3")),
                    themeWithId(4L, new Theme("1", "2", "3")),
                    themeWithId(5L, new Theme("1", "2", "3"))
            );
            given(themeRepository.findAllOrderByRank(any(), any(), anyInt()))
                    .willReturn(themes);

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
            final Long id = 1L;
            final Theme theme = themeWithId(1L, new Theme("로키", "로키로키", "http://www.google.com"));
            given(themeRepository.findById(id))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.existsByTheme(theme))
                    .willReturn(false);

            // when
            themeService.deleteById(id);

            // then
            then(themeRepository).should().delete(theme);
        }

        @DisplayName("주어진 id에 해당하는 테마가 존재하지 않는다면 예외가 발생한다.")
        @Test
        void deleteById2() {
            // given
            final Long id = 1L;
            given(themeRepository.findById(id))
                    .willReturn(Optional.empty());

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
            final Theme theme = themeWithId(id, new Theme("로키", "로키로키", "http://www.google.com"));
            given(themeRepository.findById(id))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.existsByTheme(theme))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                themeService.deleteById(id);
            }).isInstanceOf(ThemeUsedException.class);
        }
    }
}
