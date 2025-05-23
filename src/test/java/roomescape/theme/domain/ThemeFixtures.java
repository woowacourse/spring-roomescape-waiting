package roomescape.theme.domain;

import jakarta.persistence.EntityManager;

public class ThemeFixtures {

    public static Theme createAndPersistTheme(EntityManager entityManager) {
        Theme theme = new Theme("방탈출", "설명", "섬네일");
        entityManager.persist(theme);
        return theme;
    }
}
