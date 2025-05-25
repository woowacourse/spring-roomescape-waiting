package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationItem;
import roomescape.domain.ReservationItemRepository;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.repository.jpa.ReservationItemJpaRepository;

@RequiredArgsConstructor
@Repository
public class ReservationItemRepositoryImpl implements ReservationItemRepository {

    private final ReservationItemJpaRepository reservationItemJpaRepository;

    @Override
    public ReservationItem save(ReservationItem reservationItem) {
        return reservationItemJpaRepository.save(reservationItem);
    }

    @Override
    public void delete(ReservationItem reservationItem) {
        reservationItemJpaRepository.delete(reservationItem);
    }

    @Override
    public Optional<ReservationItem> findReservationItemByDateAndTimeAndTheme(LocalDate date,
                                                                              ReservationTime time,
                                                                              ReservationTheme theme) {
        return reservationItemJpaRepository.findReservationItemByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme) {
        return reservationItemJpaRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }
}
