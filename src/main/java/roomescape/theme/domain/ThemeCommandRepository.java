package roomescape.theme.domain;

public interface ThemeCommandRepository {
    
    Long save(Theme theme);

    void deleteById(Long id);
}
