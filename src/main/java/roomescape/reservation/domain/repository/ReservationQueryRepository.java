package roomescape.reservation.domain.repository;

public interface ReservationQueryRepository {
    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
