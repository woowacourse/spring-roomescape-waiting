package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationResponses;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidIdException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class ReservationService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(final ReservationTimeRepository reservationTimeRepository,
                              final ReservationRepository reservationRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository,
                              final WaitingRepository waitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public ReservationResponses findEntireReservationList() {
        final List<ReservationResponse> reservationResponses = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    @Transactional
    public ReservationResponse create(final ReservationRequest reservationRequest) {
        validateDuplicatedReservation(reservationRequest);
        final ReservationTime reservationTime = getReservationTime(reservationRequest);
        final Theme theme = getTheme(reservationRequest);
        final Member member = getMember(reservationRequest);
        validatePastDate(reservationRequest, reservationTime);
        final Reservation reservation = new Reservation(member, reservationRequest.date(), reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private ReservationTime getReservationTime(final ReservationRequest reservationRequest) {
        return reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new InvalidIdException("timeId", reservationRequest.timeId()));
    }

    private Theme getTheme(final ReservationRequest reservationRequest) {
        return themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new InvalidIdException("themeId", reservationRequest.themeId()));
    }

    private Member getMember(final ReservationRequest reservationRequest) {
        return memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new InvalidIdException("memberId", reservationRequest.memberId()));
    }

    private void validateDuplicatedReservation(final ReservationRequest reservationRequest) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId())) {
            throw new ReservationFailException("이미 예약이 등록되어 있습니다.");
        }
    }

    private void validatePastDate(final ReservationRequest reservationRequest, final ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationRequest.date(), reservationTime.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ReservationFailException("지나간 날짜와 시간으로 예약할 수 없습니다.");
        }
    }

    @Transactional
    public void delete(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));
        reservationRepository.deleteById(id);

        updateWaitingToReservation(reservation);
    }

    private void updateWaitingToReservation(Reservation reservation) {
        Optional<Waiting> waiting = waitingRepository.findFirstByDateAndTimeIdAndThemeIdOrderById(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if (waiting.isPresent()) {
            Reservation createdReservation = new Reservation(reservation.getMember(), reservation.getDate(),
                    reservation.getTime(), reservation.getTheme());
            waitingRepository.delete(waiting.get());
            reservationRepository.save(createdReservation);
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponses findReservations(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                                 final LocalDate dateTo) {
        final List<ReservationResponse> reservationResponses = reservationRepository
                .findAllByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    @Transactional(readOnly = true)
    public List<ReservationsMineResponse> findReservationsByMember(final Member member) {
        return reservationRepository.findByMember(member)
                .stream()
                .map(ReservationsMineResponse::from)
                .collect(Collectors.toList());
    }
}
