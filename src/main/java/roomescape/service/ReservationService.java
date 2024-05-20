package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.auth.LoginMember;
import roomescape.dto.reservation.MyReservationResponse;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;

import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final int MAX_RESERVATIONS_PER_TIME = 1;

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        final int count = reservationRepository.countByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        );
        validateDuplicatedReservation(count);
        reservation.toReserved();
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicatedReservation(final int count) {
        if (count >= MAX_RESERVATIONS_PER_TIME) {
            throw new IllegalArgumentException("해당 시간대에 예약이 모두 찼습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBy(final ReservationFilterParam filterParam) {
        final List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                filterParam.themeId(), filterParam.memberId(), filterParam.dateFrom(), filterParam.dateTo()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(final Long id) {
        final boolean isExist = reservationRepository.existsById(id);
        if (!isExist) {
            throw new IllegalArgumentException("해당 ID의 예약이 없습니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> findMyReservations(final LoginMember loginMember) {
        final List<Reservation> reservations = reservationRepository.findByMemberId(loginMember.id());
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
