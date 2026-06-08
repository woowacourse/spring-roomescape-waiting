package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Theme;

@FunctionalInterface
public interface ThemeLockedAction<T> {
    T execute(Optional<Theme> lockedTheme, LockedReservationWriter writer);
}
