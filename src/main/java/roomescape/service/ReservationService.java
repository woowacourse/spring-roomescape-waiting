package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidIdException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class ReservationService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationTimeRepository reservationTimeRepository,
                              final ReservationRepository reservationRepository,
                              final ThemeRepository themeRepository, final MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationResponse> findEntireReservations() {
        final List<ReservationResponse> reservationResponses = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservationResponses);
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationWaitingResponse> findEntireWaitingReservations() {
        final List<ReservationWaitingResponse> reservations = reservationRepository.findByStatus(ReservationStatus.WAITING)
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservations);
    }

    public ReservationResponse register(final ReservationRequest reservationRequest) {
        validateDuplicatedReservation(reservationRequest);
        final ReservationTime reservationTime = getReservationTime(reservationRequest);
        final Theme theme = getTheme(reservationRequest);
        final Member member = getMember(reservationRequest);
        validatePastDate(reservationRequest, reservationTime);
        final Reservation reservation = createReservation(reservationRequest, member, reservationTime, theme);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private Reservation createReservation(final ReservationRequest reservationRequest, final Member member, final ReservationTime reservationTime, final Theme theme) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservationRequest.date(), reservationRequest.timeId(), reservationRequest.themeId())) {
            return new Reservation(member, reservationRequest.date(), reservationTime, theme, ReservationStatus.WAITING);
        }
        return new Reservation(member, reservationRequest.date(), reservationTime, theme, ReservationStatus.RESERVED);
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
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                reservationRequest.memberId())) {
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
        final Optional<Reservation> reservationResult = reservationRepository.findById(id);
        if (reservationResult.isPresent() && reservationResult.get().getStatus() == ReservationStatus.RESERVED) {
            updateWaitReservation(reservationResult.get());
            reservationRepository.deleteById(id);
        }
    }

    private void updateWaitReservation(final Reservation reservation) {
        final LocalDate date = reservation.getDate();
        final Long timeId = reservation.getTime().getId();
        final Long themeId = reservation.getTheme().getId();
        final Optional<Reservation> waitReservationResult = reservationRepository.findFirstByDateAndTimeIdAndThemeIdAndStatus(date, timeId, themeId, ReservationStatus.WAITING);
        if (!waitReservationResult.isEmpty()) {
            final Reservation waitReservation = waitReservationResult.get();
            waitReservation.setStatus(ReservationStatus.RESERVED);
        }
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationResponse> findReservations(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                                                  final LocalDate dateTo) {
        final List<ReservationResponse> reservationResponses = reservationRepository
                .findAllByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservationResponses);
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationsMineResponse> findMemberReservations(final Member member) {
        final List<ReservationsMineResponse> reservationsMineResponses = reservationRepository.findByMember(member)
                .stream()
                .map(reservation -> buildReservationMineResponse(reservation, member))
                .toList();
        return new ResponsesWrapper<>(reservationsMineResponses);
    }

    private ReservationsMineResponse buildReservationMineResponse(final Reservation reservation, final Member member) {
        if (reservation.getStatus() == ReservationStatus.WAITING) {
            return ReservationsMineResponse.from(reservation, calculateWaitingNumber(reservation, member));
        }
        return ReservationsMineResponse.from(reservation, 0);
    }

    private Integer calculateWaitingNumber(final Reservation reservation, final Member member) {
        final List<Reservation> reservations = reservationRepository.findByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        return IntStream.range(0, reservations.size())
                .filter(index -> reservations.get(index).getMember().getId().equals(member.getId()))
                .findFirst()
                .orElseThrow(() -> new ReservationFailException("대기중인 예약을 찾을 수 없습니다."));
    }

    public void deleteByIdWithWaiting(final Long id, final Member member) {
        if (reservationRepository.existsByIdAndMemberId(id, member.getId())) {
            deleteWaitingById(id);
        }
    }

    public void deleteWaitingById(final Long id) {
        final Optional<Reservation> reservationResult = reservationRepository.findById(id);
        if (reservationResult.isPresent() && reservationResult.get().getStatus() == ReservationStatus.WAITING) {
            reservationRepository.deleteById(id);
        }
    }
}
