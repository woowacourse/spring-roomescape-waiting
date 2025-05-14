package roomescape.theme.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.PopularThemeResponse;
import roomescape.theme.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private List<Theme> data = new ArrayList<>();
    private AtomicLong atomicLong = new AtomicLong();

    public FakeThemeRepository(Theme... themes) {
        data.addAll(List.of(themes));
        long maxId = data.stream()
                .mapToLong(Theme::getId)
                .max()
                .orElse(0L);
        atomicLong.set(maxId);
    }

    @Override
    public Theme save(Theme theme) {
        Theme savedTheme = new Theme(atomicLong.incrementAndGet(), theme.getName(),
                theme.getDescription(), theme.getThumbnail());
        data.add(savedTheme);
        return savedTheme;
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(data);
    }

    @Override
    public List<PopularThemeResponse> findAllPopular() {
        return List.of();
    }

    @Override
    public void deleteById(Long id) {
        boolean isDeleted = data.removeIf(theme -> theme.getId().equals(id));
        if (isDeleted) {
            return 1;
        }
        return 0;
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return data.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
    }
}
