package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationQueryService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findReservedReservations() {
        return reservationRepository.findByStatusWithAssociations(BookingStatus.RESERVED)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findWaitingReservations() {
        return reservationRepository.findByStatusWithAssociations(BookingStatus.WAITING)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTime(final Long themeId, final String date) {
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        final List<Reservation> reservations = reservationRepository.findByDateAndThemeIdWithAssociations(
                LocalDate.parse(date), themeId);

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
        return reservationRepository.findByFilteringWithAssociations(themeId, memberId, start, end,
                        BookingStatus.RESERVED)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findByMemberId(final Long memberId) {
        return reservationRepository.findByMemberIdWithAssociations(memberId)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }
}

