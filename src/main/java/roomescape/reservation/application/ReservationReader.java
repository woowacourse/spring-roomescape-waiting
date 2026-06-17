package roomescape.reservation.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import roomescape.reservation.application.dto.ReservationIntegrationInfo;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.ActiveReservationRepository;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.domain.PendingReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationReader {

    private final ActiveReservationRepository activeRepository;
    private final PendingReservationRepository pendingRepository;

    public ReservationIntegrationInfo read(Long reservationId) {
        // 1. 먼저 Active 레포지토리 조회
        Optional<ActiveReservation> active = activeRepository.findById(reservationId);
        if (active.isPresent()) {
            ActiveReservation res = active.get();
            return new ReservationIntegrationInfo(
                    res.getId(),
                    res.getName(),
                    res.themeName(),
                    "ACTIVE"
            );
        }
        // 2. 없으면 Pending 레포지토리 조회
        PendingReservation pending = pendingRepository.getById(reservationId);
        return new ReservationIntegrationInfo(
                pending.getId(),
                pending.getName(),
                pending.getSlot().getTheme().getName(),
                "PENDING"
        );
    }

    public Map<Long, ReservationIntegrationInfo> readAll(List<Long> reservationIds) {
        log.info("조회 요청된 예약 ID들: {}", reservationIds);
        if (reservationIds == null || reservationIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, ReservationIntegrationInfo> resultMap = new HashMap<>();

        List<ActiveReservation> actives = activeRepository.findAllByIdIn(reservationIds);
        log.info("조회된 Active 예약 개수: {}", actives.size());
        actives.forEach(active -> resultMap.put(
                active.getId(),
                new ReservationIntegrationInfo(active.getId(), active.getName(), active.themeName(), "ACTIVE")
        ));

        List<Long> remainingIds = reservationIds.stream()
                .filter(id -> !resultMap.containsKey(id))
                .toList();

        if (!remainingIds.isEmpty()) {
            List<PendingReservation> pendingReservations = pendingRepository.findAllByIdIn(remainingIds);
            pendingReservations.forEach(pending -> resultMap.put(
                    pending.getId(),
                    new ReservationIntegrationInfo(pending.getId(), pending.getName(), pending.getSlot().getTheme().getName(), "PENDING")
            ));
        }
        return resultMap;
    }
}
