package roomescape.theme.domain;

public interface ThemeCommandRepository {

    Theme save(Theme theme);

    void deleteById(Long id);
}
