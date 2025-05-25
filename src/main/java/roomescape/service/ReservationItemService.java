package roomescape.service;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationItem;
import roomescape.domain.ReservationItemRepository;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;

@RequiredArgsConstructor
@Service
public class ReservationItemService {

    private final ReservationItemRepository reservationItemRepository;

    public ReservationItem addReservationItem(LocalDate date, ReservationTime reservationTime, ReservationTheme theme) {
        if (isExistReservationItem(date, reservationTime, theme)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약입니다.");
        }
        return reservationItemRepository.save(
                ReservationItem.builder()
                        .date(date)
                        .time(reservationTime)
                        .theme(theme)
                        .build()
        );
    }

    public ReservationItem getReservationItemByDateAndTimeAndTheme(LocalDate date,
                                                                   ReservationTime reservationTime,
                                                                   ReservationTheme theme) {
        return reservationItemRepository.findReservationItemByDateAndTimeAndTheme(date, reservationTime, theme)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약 항목을 찾지 못하였습니다."));
    }

    public boolean isExistReservationItem(LocalDate date, ReservationTime reservationTime, ReservationTheme theme) {
        return reservationItemRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme);
    }
}
