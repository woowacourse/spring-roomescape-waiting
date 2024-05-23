package roomescape.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.request.ReservationWaitingRequest;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.domain.reservation.dto.ReservationWithRankDto;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse addReservation(ReservationRequest request) {
        Reservation reservation = createReservation(
                request.date(),
                request.timeId(),
                request.themeId(),
                request.memberId(),
                ReservationStatus.RESERVED
        );

        validateReservationAlreadyExists(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    @Transactional
    public ReservationResponse addReservationWaiting(ReservationWaitingRequest request) {
        Reservation reservation = createReservation(
                request.date(),
                request.timeId(),
                request.themeId(),
                request.memberId(),
                ReservationStatus.WAITING
        );

        validateReservationNotExists(reservation);
        validateCurrentMemberAlreadyReserved(reservation);
        validateReservationWaitingExists(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getReservationsByConditions(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findAllByConditions(memberId, themeId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservationWithRanks(long memberId) {
        List<ReservationWithRankDto> reservations = reservationRepository.findReservationWithRanksByMemberId(memberId);

        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationWaitings() {
        List<Reservation> reservations = reservationRepository.findAllByStatus(ReservationStatus.WAITING);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new DomainNotFoundException(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", id));
        }

        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteReservationWaitingById(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.getByIdAndStatus(reservationId, ReservationStatus.WAITING);

        if (!reservation.isOwnedBy(memberId)) {
            throw new UnauthorizedException("자신의 예약 대기만 취소할 수 있습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public ReservationResponse approveReservationWaiting(Long id) {
        Reservation reservation = reservationRepository.getById(id);

        validateReservationNotWaiting(reservation);
        validateReservationAlreadyExists(reservation);

        reservation.updateToReserved();

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void rejectReservationWaiting(Long id) {
        Reservation reservation = reservationRepository.getById(id);

        validateReservationNotWaiting(reservation);

        reservationRepository.deleteById(id);
    }

    private Reservation createReservation(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId,
            ReservationStatus status
    ) {
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

        return Reservation.create(LocalDateTime.now(clock), date, member, reservationTime, theme, status);
    }

    private void validateReservationNotExists(Reservation reservation) {
        boolean reservationNotExists = !reservationRepository.existsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.RESERVED
        );

        if (reservationNotExists) {
            throw new BadRequestException("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateCurrentMemberAlreadyReserved(Reservation reservation) {
        boolean currentMemberAlreadyReserved = reservationRepository.existsByReservationWithMemberId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId(),
                ReservationStatus.RESERVED
        );

        if (currentMemberAlreadyReserved) {
            throw new BadRequestException("해당 회원은 이미 예약을 하였습니다.");
        }
    }

    private void validateReservationWaitingExists(Reservation reservation) {
        boolean reservationWaitingExists = reservationRepository.existsByReservationWithMemberId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId(),
                ReservationStatus.WAITING
        );

        if (reservationWaitingExists) {
            throw new BadRequestException("해당 회원은 이미 예약 대기를 하였습니다.");
        }
    }

    private void validateReservationAlreadyExists(Reservation reservation) {
        boolean reservationExists = reservationRepository.existsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                ReservationStatus.RESERVED
        );

        if (reservationExists) {
            throw new BadRequestException("이미 예약이 존재합니다.");
        }
    }

    private void validateReservationNotWaiting(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new BadRequestException("예약 대기 상태가 아닙니다.");
        }
    }
}
