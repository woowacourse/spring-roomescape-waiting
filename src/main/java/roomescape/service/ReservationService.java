package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.auth.LoginMember;
import roomescape.dto.reservation.MyReservationWithRankResponse;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.repository.ReservationRepository;

import java.util.List;

@Transactional
@Service
public class ReservationService {

    private static final int MAX_RESERVATIONS_PER_TIME = 1;

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse create(final Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.RESERVED) {
            validateDuplicatedReservation(reservation);
        }
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicatedReservation(final Reservation reservation) {
        final int count = reservationRepository.countByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        );

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
        final List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetweenAndStatus(
                filterParam.themeId(), filterParam.memberId(),
                filterParam.dateFrom(), filterParam.dateTo(), ReservationStatus.RESERVED
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

    public List<MyReservationWithRankResponse> findMyReservationsAndWaitings(final LoginMember loginMember) {
        final List<MyReservationWithRankResponse> reservations = reservationRepository.findByMemberId(loginMember.id());
        return reservations;
    }
}
