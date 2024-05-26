package roomescape.service.reservation;

import static org.springframework.data.jpa.domain.Specification.where;
import static roomescape.domain.reservation.ReservationRepository.Specs.hasEndDate;
import static roomescape.domain.reservation.ReservationRepository.Specs.hasMemberId;
import static roomescape.domain.reservation.ReservationRepository.Specs.hasStartDate;
import static roomescape.domain.reservation.ReservationRepository.Specs.hasThemeId;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.domain.reservationwaiting.ReservationWaitingWithRank;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.InvalidReservationMemberException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.reservation.dto.ReservationListResponse;
import roomescape.service.reservation.dto.ReservationMineListResponse;
import roomescape.service.reservation.dto.ReservationMineResponse;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository reservationWaitingRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReservationListResponse findAllReservation(
            Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findAll(
                where(hasMemberId(memberId))
                        .and(hasThemeId(themeId))
                        .and(hasStartDate(dateFrom))
                        .and(hasEndDate(dateTo))
        );
        return new ReservationListResponse(reservations.stream()
                .map(ReservationResponse::new)
                .toList());
    }

    @Transactional(readOnly = true)
    public ReservationMineListResponse findMyReservation(Member member) {
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());
        List<ReservationWaitingWithRank> reservationWaitingWithRanks
                = reservationWaitingRepository.findAllWaitingWithRankByMemberId(member.getId());

        List<ReservationMineResponse> myReservations = Stream.concat(
                        reservations.stream().map(ReservationMineResponse::new),
                        reservationWaitingWithRanks.stream().map(ReservationMineResponse::new)
                )
                .sorted(Comparator.comparing(ReservationMineResponse::getDateTime))
                .toList();
        return new ReservationMineListResponse(myReservations);
    }

    public ReservationResponse saveReservation(ReservationRequest request, Member member) {
        ReservationTime time = findReservationTimeById(request.getTimeId());
        Theme theme = findThemeById(request.getThemeId());
        Reservation reservation = request.toReservation(time, theme, member);
        validateDuplicateReservation(reservation);
        validateDateTimeReservation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme())) {
            throw new DuplicatedReservationException();
        }
    }

    private void validateDateTimeReservation(Reservation reservation) {
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new InvalidDateTimeReservationException();
        }
    }

    public void deleteReservation(long reservationId, long memberId) {
        Reservation reservation = findReservationById(reservationId);
        validateReservationMember(reservation, memberId);

        reservationWaitingRepository.findFirstByReservation(reservation)
                .ifPresentOrElse(
                        waiting -> upgradeWaitingToReservationAndDeleteWaiting(reservation, waiting),
                        () -> reservationRepository.delete(reservation)
                );
    }

    private void validateReservationMember(Reservation reservation, long memberId) {
        if (reservation.isNotOwnedBy(memberId)) {
            throw new InvalidReservationMemberException();
        }
    }

    private void upgradeWaitingToReservationAndDeleteWaiting(Reservation reservation, ReservationWaiting waiting) {
        reservation.updateMember(waiting.getMember());
        reservationWaitingRepository.delete(waiting);
    }

    private Reservation findReservationById(long id) { // TODO: optional 다루는 함수들 전부 분리됐는지 확인하고, 순서 재배열하기
        return reservationRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime findReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
