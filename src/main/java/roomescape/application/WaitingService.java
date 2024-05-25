package roomescape.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.WaitingRequest;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.WaitingResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public WaitingResponse addWaiting(WaitingRequest request) {
        Waiting waiting = createWaiting(
                request.currentDateTime(),
                request.date(),
                request.timeId(),
                request.themeId(),
                request.memberId()
        );

        validateReservationNotExists(waiting);
        validateCurrentMemberAlreadyReserved(waiting);
        validateCurrentMemberAlreadyWaiting(waiting);

        Waiting savedWaiting = waitingRepository.save(waiting);

        return WaitingResponse.from(savedWaiting);
    }

    public List<WaitingResponse> getWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @Transactional
    public void deleteWaitingById(Long waitingId, Long memberId) {
        Waiting waiting = waitingRepository.getById(waitingId);

        if (!waiting.isOwnedBy(memberId)) {
            throw new UnauthorizedException("자신의 예약 대기만 취소할 수 있습니다.");
        }

        waitingRepository.delete(waiting);
    }

    @Transactional
    public ReservationResponse approveWaitingToReservation(LocalDateTime currentDateTime, Long waitingId) {
        Waiting waiting = waitingRepository.getById(waitingId);

        validateReservationAlreadyExists(waiting);

        Reservation reservation = Reservation.create(
                currentDateTime,
                waiting.getDetail(),
                waiting.getMember()
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        waitingRepository.delete(waiting);

        return ReservationResponse.from(savedReservation);
    }

    @Transactional
    public void rejectWaitingToReservation(Long waitingId) {
        Waiting waiting = waitingRepository.getById(waitingId);

        waitingRepository.delete(waiting);
    }

    private Waiting createWaiting(
            LocalDateTime currentDateTime,
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

        ReservationDetail detail = new ReservationDetail(date, reservationTime, theme);

        return Waiting.create(currentDateTime, detail, member);
    }

    private void validateReservationNotExists(Waiting waiting) {
        boolean reservationNotExists = !reservationRepository.existsByDetail(waiting.getDetail());

        if (reservationNotExists) {
            throw new BadRequestException("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateReservationAlreadyExists(Waiting waiting) {
        boolean reservationExists = reservationRepository.existsByDetail(waiting.getDetail());

        if (reservationExists) {
            throw new BadRequestException("이미 예약이 존재합니다.");
        }
    }

    private void validateCurrentMemberAlreadyReserved(Waiting waiting) {
        boolean currentMemberAlreadyReserved = reservationRepository.existsByDetailAndMemberId(
                waiting.getDetail(),
                waiting.getMember().getId()
        );

        if (currentMemberAlreadyReserved) {
            throw new BadRequestException("해당 회원은 이미 예약을 하였습니다.");
        }
    }

    private void validateCurrentMemberAlreadyWaiting(Waiting waiting) {
        boolean currentMemberAlreadyWaiting = waitingRepository.existsByDetailAndMemberId(
                waiting.getDetail(),
                waiting.getMember().getId()
        );

        if (currentMemberAlreadyWaiting) {
            throw new BadRequestException("해당 회원은 이미 예약 대기를 하였습니다.");
        }
    }
}
