package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.dto.ReservationIdWithSequenceResponse;
import roomescape.reservation.application.dto.ReservationSearchFilterRequest;
import roomescape.reservation.application.dto.SlotSequenceResponse;
import roomescape.reservation.application.dto.ThemeToBookCountResponse;
import roomescape.reservation.domain.BookedCount;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public boolean existsBySlot(final ReservationSlot slot) {
        return reservationRepository.existsBySlot(slot);
    }

    public boolean existsBySlotAndUserId(final ReservationSlot slot, final UserId userId) {
        return reservationRepository.existsBySlotAndUserId(slot, userId);
    }

    public Reservation getById(final ReservationId id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.RESERVATION, id));
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getAllByUserId(final UserId userId) {
        return reservationRepository.findAllByUserId(userId);
    }

    public List<Reservation> getAllBySearchFilter(final ReservationSearchFilterRequest request) {
        return reservationRepository.findAllByParams(
                request.userId(),
                request.themeId(),
                request.dateFrom(),
                request.dateTo()
        );
    }

    public List<SlotSequenceResponse> getAllSlotSequenceResponseByUserId(final UserId userId) {
        final Map<ReservationId, Reservation> reservations = getAllByUserId(userId).stream()
                .collect(Collectors.toMap(Reservation::getId, reservation -> reservation));

        final List<ReservationIdWithSequenceResponse> reservationSequences =
                reservationRepository.findAllReservationSequencesByIds(
                        reservations.keySet().stream().toList());

        return reservationSequences.stream()
                .map(reservationIdWithSequence -> new SlotSequenceResponse(
                        reservations.get(reservationIdWithSequence.reservationId()).getSlot(),
                        reservationIdWithSequence.sequence()))
                .toList();
    }

    public List<ThemeToBookCountResponse> getRanking(final ReservationDate startDate,
                                                     final ReservationDate endDate,
                                                     final int bookCount) {

        return reservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, bookCount)
                .entrySet().stream()
                .map(entry -> new ThemeToBookCountResponse(entry.getKey(), BookedCount.from(entry.getValue())))
                .toList();
    }
}
