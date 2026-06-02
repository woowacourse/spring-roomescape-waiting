package roomescape.fake;

import lombok.RequiredArgsConstructor;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationQueryRepository;

@RequiredArgsConstructor
public class FakeReservationQueryRepository implements ReservationQueryRepository {

    private final FakeReservationRepository fakeReservationRepository;

    @Override
    public boolean existsByTimeId(Long timeId) {
        return fakeReservationRepository.findAllReservations().stream()
                .map(Reservation::getTimeId)
                .anyMatch(timeId::equals);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return fakeReservationRepository.findAllReservations().stream()
                .map(Reservation::getThemeId)
                .anyMatch(themeId::equals);
    }
}
