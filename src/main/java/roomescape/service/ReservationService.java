package roomescape.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.domain.WaitingWithRank;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.ReservationMineResponse;
import roomescape.service.dto.response.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findAllReservation(
            Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId,
                themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationMineResponse> findMyReservation(Member member) {
        List<Reservation> reservations = reservationRepository.findByMemberId(member.getId());
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(member.getId());
        List<ReservationMineResponse> responses = new ArrayList<>();

        for (Reservation reservation : reservations) {
            responses.add(new ReservationMineResponse(reservation));
        }

        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            Long rank = waitingWithRank.rank();
            responses.add(new ReservationMineResponse(waitingWithRank.waiting(), rank + 1));
        }

        responses.sort(Comparator.comparing(ReservationMineResponse::date).thenComparing(o -> o.time().getStartAt()));

        return responses;
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationRequest request, Member member) {
        ReservationTime time = findReservationTimeById(request.timeId());
        Theme theme = findThemeById(request.themeId());

        validateDuplicateReservation(request);

        Reservation reservation = request.toReservation(member, time, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())) {
            throw new DuplicatedReservationException();
        }
    }


    @Transactional
    public void deleteReservation(long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.delete(reservation);
        makeWaitingToReservation(reservation);
    }

    private void makeWaitingToReservation(Reservation reservation) {
        Optional<Waiting> opWaiting = waitingRepository.findFirstByDateAndTimeIdAndThemeIdAndStatus(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.WAITING
        );

        opWaiting.ifPresent(waiting -> {
            waitingRepository.delete(waiting);
            reservationRepository.save(
                    new Reservation(waiting.getDate(), waiting.getMember(), waiting.getTime(), waiting.getTheme()));
        });
    }

    private Reservation findReservationById(long id) {
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
