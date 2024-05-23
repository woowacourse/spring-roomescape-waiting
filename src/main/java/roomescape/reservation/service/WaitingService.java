package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exceptions.AuthException;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.ReservationOrWaitingResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public WaitingResponse addWaiting(WaitingRequest waitingRequest, MemberRequest memberRequest) {
        Reservation reservation = getReservationByWaiting(waitingRequest);
        Member member = memberRequest.toMember();

        validateIsNotDuplicatedMember(reservation, member);

        Waiting waiting = waitingRepository.save(new Waiting(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme(),
                member)
        );
        return new WaitingResponse(waiting);
    }

    private Reservation getReservationByWaiting(WaitingRequest waitingRequest) {
        ReservationTime reservationTime = reservationTimeRepository.getById(waitingRequest.timeId());
        Theme theme = themeRepository.getById(waitingRequest.themeId());

        return reservationRepository.findByDateAndReservationTimeAndTheme(
                waitingRequest.date(),
                reservationTime,
                theme
        ).orElseThrow(() -> new NotFoundException("기존 예약이 존재하지 않아 예약 대기가 불가능합니다. 예약 신청으로 다시 시도해주세요."));
    }

    private void validateIsNotDuplicatedMember(Reservation reservation, Member member) {
        validateDoesNotAlreadyReservateMember(reservation, member);
        validateIsNotAlreadyWaitingMember(reservation, member);
    }

    private void validateDoesNotAlreadyReservateMember(Reservation reservation, Member member) {
        if (reservation.isSameMember(member)) {
            throw new DuplicationException("이미 예약에 성공하셨습니다.");
        }
    }

    private void validateIsNotAlreadyWaitingMember(Reservation reservation, Member member) {
        boolean isAlreadyWaiting = waitingRepository.existsByDateAndReservationTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme(),
                member
        );

        if (isAlreadyWaiting) {
            throw new DuplicationException("이미 예약 대기를 거셨습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationOrWaitingResponse> findWaitingsByMember(MemberRequest memberRequest) {
        return waitingRepository.findWaitingsWithRankByMemberId(memberRequest.id())
                .stream()
                .map(ReservationOrWaitingResponse::new)
                .toList();
    }

    public List<WaitingResponse> findAll() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public void deleteWaiting(Long id, MemberRequest memberRequest) {
        waitingRepository.findById(id)
                .ifPresent(waiting -> {
                            validateDeleteAuth(memberRequest.toMember(), waiting);
                            waitingRepository.deleteById(id);
                        }
                );
    }

    private void validateDeleteAuth(Member member, Waiting waiting) {
        if (waiting.doesNotHaveDeleteAuth(member)) {
            throw new AuthException("예약 대기를 삭제할 권한이 없습니다.");
        }
    }
}
