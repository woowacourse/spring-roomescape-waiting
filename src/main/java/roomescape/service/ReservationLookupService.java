package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.service.dto.ReservationStatus;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ReservationLookupService {
    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationLookupService(ReservationService reservationService,
                                    ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    public List<ReservationStatus> findByName(String name) {
        return Stream.concat(
                        reservationService.findByName(name).stream()
                                .map(ReservationStatus::reserved),
                        reservationWaitingService.findByName(name).stream()
                                .map(ReservationStatus::waiting))
                .sorted(Comparator
                        .comparing(ReservationStatus::date, Comparator.reverseOrder())
                        .thenComparing(status -> status.time().getStartAt(), Comparator.reverseOrder()))
                .toList();
    }

    public List<ReservationStatus> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new RoomescapeException(ErrorCode.INVALID_INPUT, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        return Stream.concat(
                        reservationService.findByDateRange(startDate, endDate).stream()
                                .map(ReservationStatus::reserved),
                        reservationWaitingService.findByDateRange(startDate, endDate).stream()
                                .map(ReservationStatus::waiting))
                .sorted(Comparator
                        .comparing(ReservationStatus::date)
                        .thenComparing(status -> status.time().getStartAt())
                        .thenComparing(status -> status.theme().getName())
                        .thenComparing(ReservationStatus::status)
                        .thenComparing(ReservationStatus::turn, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
    }
}
