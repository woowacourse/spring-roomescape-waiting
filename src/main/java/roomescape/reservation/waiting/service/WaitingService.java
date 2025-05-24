package roomescape.reservation.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.dto.CreateWaitingRequest;
import roomescape.reservation.waiting.dto.WaitingResponse;
import roomescape.reservation.waiting.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

import java.util.List;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            ReservationRepository reservationRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse createWaiting(final CreateWaitingRequest request) {
        final ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new BadRequestException("예약 시간이 존재하지 않습니다."));
        final Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BadRequestException("예약자를 찾을 수 없습니다."));
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new BadRequestException("테마가 존재하지 않습니다."));
        Waiting waiting = Waiting.register(request.date(), time, member, theme);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    public ReservationResponse approveWaiting(long id) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("예약 대기가 존재하지 않습니다."));
        validateNoDuplicateReservation(waiting);
        Reservation reservation = Reservation.register(waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
        waitingRepository.deleteById(id);
        reservationRepository.save(reservation);
        return new ReservationResponse(reservation);
    }

    public List<WaitingResponse> getAllWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::new)
                .toList();
    }

    public void cancelWaiting(long id) {
        waitingRepository.deleteById(id);
    }

    private void validateNoDuplicateReservation(Waiting waiting) {
        boolean alreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeId(
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        );
        if (alreadyReserved) {
            throw new BadRequestException("해당 시간에 이미 예약이 존재합니다.");
        }
    }
}
