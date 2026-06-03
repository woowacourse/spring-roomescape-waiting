package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.dto.ThemeCreationRequest;
import roomescape.domain.theme.dto.ThemeCreationResponse;
import roomescape.domain.theme.dto.ThemeResponse;
import roomescape.support.exception.RoomescapeException;

class ThemeServiceTest {

    private ThemeService themeService;
    private FakeThemeRepository themeRepository;
    private FakeReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        reservationRepository = new FakeReservationRepository();
        themeService = new ThemeService(themeRepository, reservationRepository);
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        ThemeCreationRequest request = new ThemeCreationRequest("테마", "설명", "url");
        
        ThemeCreationResponse response = themeService.createTheme(request);

        assertThat(response.name()).isEqualTo("테마");
        assertThat(themeRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사용 중인 테마를 삭제하려 하면 예외가 발생한다.")
    void deleteInUseTheme() {
        Theme theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));
        reservationRepository.setCount(1);

        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId()))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("인기 테마 순위를 조회한다.")
    void getThemeRank() {
        themeRepository.save(Theme.createWithoutId("테마1", "설명", "url"));
        
        var responses = themeService.getThemeRank();

        assertThat(responses).isNotNull();
    }

    private static class FakeThemeRepository implements ThemeRepository {
        private final List<Theme> themes = new ArrayList<>();
        private Long idCounter = 1L;

        @Override
        public Optional<Theme> findById(Long id) { return themes.stream().filter(t -> t.getId().equals(id)).findFirst(); }
        @Override
        public List<Theme> findAll() { return themes; }
        @Override
        public Theme save(Theme theme) {
            Theme saved = Theme.of(idCounter++, theme.getName(), theme.getContent(), theme.getUrl());
            themes.add(saved);
            return saved;
        }
        @Override
        public int deleteById(Long id) { return themes.removeIf(t -> t.getId().equals(id)) ? 1 : 0; }
        @Override
        public List<Theme> findPopularThemes(int limit, LocalDate start, LocalDate end) { return themes; }
    }

    private static class FakeReservationRepository implements ReservationRepository {
        private int count = 0;
        public void setCount(int count) { this.count = count; }
        @Override public int countByThemeId(Long id) { return count; }
        
        @Override public Reservation save(Reservation r) { return null; }
        @Override public List<Reservation> findAll() { return null; }
        @Override public int deleteById(Long id) { return 0; }
        @Override public int countByTimeId(Long id) { return 0; }
        @Override public int countByReservationDateId(Long id) { return 0; }
        @Override public List<Long> findReservedTimes(Long themeId, Long dateId) { return null; }
        @Override public List<Reservation> findByName(String name) { return null; }
        @Override public List<Reservation> findUpcomingByName(String name, LocalDate currentDate, java.time.LocalTime currentTime) { return null; }
        @Override public Optional<Reservation> findById(Long id) { return Optional.empty(); }
        @Override public int updateReservation(Long id, Long d, Long t) { return 0; }
        @Override public boolean existsByDateIdAndTimeIdAndThemeId(Long d, Long t, Long th) { return false; }
    }
}
