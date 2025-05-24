package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MyHistoryResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.WaitingResponse;
import roomescape.reservation.application.dto.WaitingWithRank;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.WaitingStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationQueryService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findReservedReservations() {
        return reservationRepository.findAllWithAssociations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<WaitingResponse> findWaitingReservations() {
        return waitingRepository.findByWaitingWithAssociations(WaitingStatus.PENDING)
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTime(
            final Long themeId,
            final LocalDate date
    ) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findByDateAndThemeIdWithAssociations(
                date, themeId);

        return times.stream()
                .map(time -> {
                    boolean isBooked = reservations.stream().anyMatch(r -> r.hasConflictWith(time, theme));
                    return AvailableReservationTimeResponse.from(time, isBooked);
                })
                .toList();
    }

    public List<ReservationResponse> findReservationByThemeIdAndMemberIdInDuration(
            final Long themeId,
            final Long memberId,
            final LocalDate start,
            final LocalDate end
    ) {
        return reservationRepository.findByFilteringWithAssociations(themeId, memberId, start, end)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyHistoryResponse> findMyReservation(final Long memberId) {
        final List<MyHistoryResponse> responses = new ArrayList<>();

        final List<Reservation> reservations = reservationRepository.findByMemberIdWithAssociations(memberId);
        reservations.forEach(r -> responses.add(MyHistoryResponse.ofReservation(r)));

        final List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(memberId);
        waitingWithRanks.forEach(w -> responses.add(MyHistoryResponse.ofWaiting(w.waiting(), w.rank())));

        return responses;
    }
}

