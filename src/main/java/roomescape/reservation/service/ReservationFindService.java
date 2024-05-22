package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadArgumentRequestException;
import roomescape.reservation.domain.ReservationSearch;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class ReservationFindService {
    private final ReservationRepository reservationRepository;

    public ReservationFindService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse findReservation(Long id) {
        return reservationRepository.findById(id)
                .map(ReservationResponse::from)
                .orElseThrow(() -> new BadArgumentRequestException("해당 예약을 찾을 수 없습니다."));
    }

    public List<ReservationResponse> findReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservations(ReservationSearchRequest request) {
        ReservationSearch search = request.createReservationSearch();
        return reservationRepository.findByCondition(search.memberId(), search.themeId(), search.startDate(),
                        search.endDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findReservationsByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}
