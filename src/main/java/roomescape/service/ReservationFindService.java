package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.RankCalculator;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.repository.ReservationRepository;
import roomescape.system.exception.RoomescapeException;

@Service
@Transactional(readOnly = true)
public class ReservationFindService {

    public static final int DEFAULT_RANK = 0;

    private final ReservationRepository reservationRepository;
    private final RankCalculator rankCalculator;

    public ReservationFindService(ReservationRepository reservationRepository, RankCalculator rankCalculator) {
        this.reservationRepository = reservationRepository;
        this.rankCalculator = rankCalculator;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll().stream().filter(reservation -> reservation.isNotWaiting()).toList();
    }

    public List<Reservation> findAllBy(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        if (dateFrom.isAfter(dateTo)) {
            throw new RoomescapeException("날짜 조회 범위가 올바르지 않습니다.");
        }
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateIsBetween(themeId, memberId,
            new Date(dateFrom.toString()), new Date(dateTo.toString()));
    }

    public List<ReservationWithRank> findMyReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);

        return reservations.stream()
            .map(reservation -> new ReservationWithRank(
                reservation.getId(),
                reservation.getMember(),
                reservation.getDate().getValue().toString(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getStatus(),
                reservation.isWaiting() ? rankCalculator.calculate(reservation) : DEFAULT_RANK
            ))
            .toList();
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllByStatus(WAITING);
    }
}
