package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationStatusRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservedQueryService {

    private static final ReservationStatus RESERVED = ReservationStatus.RESERVED;

    private final ReservationStatusRepository statusRepository;

    public Page<ReservationResponse> getFilteredReserved(Long themeId, Long memberId, LocalDate from,
                                                         LocalDate to, Pageable pageable) {
        if (themeId == null && memberId == null && from == null && to == null) {
            return getAllReserved(pageable);
        }

        Page<Reservation> reservations = statusRepository.findFilteredReservations(
                themeId, memberId, from, to, RESERVED, pageable);

        return reservations.map(ReservationResponse::from);
    }

    public Reservation getReserved(Long id) {
        return statusRepository.findByIdAndStatus(id, RESERVED)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }

    private Page<ReservationResponse> getAllReserved(Pageable pageable) {
        Page<Reservation> reservations = statusRepository.findByStatus(RESERVED, pageable);

        return reservations.map(ReservationResponse::from);
    }

    public List<MyReservationResponse> getReservations(Long memberId) {
        List<Reservation> reservations = statusRepository.findByMemberIdAndStatus(memberId, RESERVED);

        return MyReservationResponse.from(reservations);
    }

    public boolean existsReserved(Long memberId, LocalDate date, Long timeId) {
        return statusRepository.existsByMemberIdAndDateAndTimeIdAndStatus(memberId, date, timeId, RESERVED);
    }

    public boolean notExistsReserved(LocalDate date, Long timeId) {
        return !statusRepository.existsByDateAndTimeIdAndStatus(date, timeId, RESERVED);
    }
}
