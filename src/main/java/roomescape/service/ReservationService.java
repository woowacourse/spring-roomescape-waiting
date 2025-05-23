package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.business.WaitingWithRank;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationStatusResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository, MemberRepository memberRepository, WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllReservationsByMember(long memberId) {
        Member savedMember = getMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMember(savedMember);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationStatusResponse findAllReservationStatusByMember(long memberId) {
        Member savedMember = getMemberById(memberId);
        List<Reservation> reservations = reservationRepository.findByMember(savedMember);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWithRankingByMember(savedMember.getId());
        return ReservationStatusResponse.createReservationStatusResponses(reservations, waitingWithRanks);
    }

    public List<ReservationResponse> findReservationsByFilter(
            long memberId, long themeId, LocalDate from, LocalDate to
    ) {
        List<Reservation> reservations = reservationRepository.findReservationsByFilter(memberId, themeId, from, to);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse addReservation(long memberId, ReservationCreationContent request) {
        Member member = getMemberById(memberId);
        Theme theme = getThemeById(request.themeId());
        ReservationTime time = getReservationTimeById(request.timeId());

        Reservation reservation = Reservation.createWithoutId(
                request.date(), time, theme, member);

        validateDuplicateReservation(theme, request.date(), time);
        validatePastReservationCreation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservationById(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservationRepository.delete(reservation);
        addReservationWithWaiting(reservation);
    }

    private void addReservationWithWaiting(Reservation deletedReservation) {
        Optional<Waiting> firstWaiting = findFirstWaitingByReservation(deletedReservation);
        if (firstWaiting.isPresent()) {
            Waiting waiting = firstWaiting.get();
            long memberId = waiting.getMember().getId();
            ReservationCreationContent creationContent = new ReservationCreationContent(waiting);
            addReservation(memberId, creationContent);
            waitingRepository.delete(waiting);
        }
    }

    private void validateDuplicateReservation(Theme theme, LocalDate date, ReservationTime time) {
        boolean isDuplicatedReservation =
                reservationRepository.existsByThemeAndDateAndReservationTime(theme, date, time);
        if (isDuplicatedReservation) {
            throw new BadRequestException("중복된 예약 입니다.");
        }
    }

    private void validatePastReservationCreation(Reservation reservation) {
        if (reservation.isPastDateTime()) {
            throw new BadRequestException("과거 예약은 생성할 수 없습니다.");
        }
    }

    private Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 회원을 찾을 수 없습니다."));
    }

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 테마을 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 예약 시간을 찾을 수 없습니다."));
    }

    private Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 예약을 찾을 수 없습니다."));
    }

    private Optional<Waiting> findFirstWaitingByReservation(Reservation deletedReservation) {
        return waitingRepository.findFirstWaiting(
                deletedReservation.getTheme().getId(),
                deletedReservation.getDate(),
                deletedReservation.getReservationTime().getId());
    }
}
