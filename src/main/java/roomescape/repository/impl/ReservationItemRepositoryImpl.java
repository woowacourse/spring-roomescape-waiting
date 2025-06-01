package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.domain.reservationitem.ReservationItemRepository;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationTime;
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
