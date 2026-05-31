package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationResponses;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.StoreManagementForbiddenException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.StoreRepository;

@Service
public class AdminReservationService {
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;

    public AdminReservationService(ReservationRepository reservationRepository, StoreRepository storeRepository) {
        this.reservationRepository = reservationRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public ReservationResponses getReservations(int page, int size, String name, Long managerId) {
        List<Long> storeIds = storeRepository.findStoreIdsByUserId(managerId);
        if (storeIds.isEmpty()) {
            return ReservationResponses.of(List.of(), false);
        }
        List<Reservation> reservations = fetchReservations(page, size, name, storeIds);
        boolean hasNext = reservations.size() > size;
        if (hasNext) {
            reservations = reservations.subList(0, size);
        }
        return ReservationResponses.of(reservations, hasNext);
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long managerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약", reservationId));
        validateManagesStore(managerId, reservation.getStore().getId());
        reservationRepository.deleteById(reservationId);
    }

    /**
     * 헬퍼메서드
     */
    private List<Reservation> fetchReservations(int page, int size, String name, List<Long> storeIds) {
        if (name == null) {
            return reservationRepository.findAllByStoreIds(storeIds, size + 1, page * size);
        }
        return reservationRepository.findAllByStoreIdsAndName(storeIds, name, size + 1, page * size);
    }

    private void validateManagesStore(Long managerId, Long storeId) {
        if (!storeRepository.existsByStoreIdAndUserId(storeId, managerId)) {
            throw new StoreManagementForbiddenException();
        }
    }
}
