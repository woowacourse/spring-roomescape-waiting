package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findByThemeIdAndDate(final Long themeId, final LocalDate date) {
        return reservationRepository.findBy(themeId, date);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findBySearchCondition(final SearchCondition condition) {
        return reservationRepository.findBy(
            condition.memberId(), condition.themeId(), condition.dateFrom(), condition.dateTo()
        );
    }

    public List<Reservation> findByMemberId(final Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }
}
