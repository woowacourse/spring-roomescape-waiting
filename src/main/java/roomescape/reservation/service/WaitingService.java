package roomescape.reservation.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.CurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.dto.WaitingAddCommand;
import roomescape.reservation.service.dto.WaitingInfo;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                          MemberRepository memberRepository, CurrentDateTime currentDateTime) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.currentDateTime = currentDateTime;
    }

    /**
     * TODO
     * 락이 필요하다.
     */
    public WaitingInfo addWaiting(WaitingAddCommand command) {
        ReservationTime reservationTime = getReservationTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        Member member = getMember(command.memberId());
        ReservationTime time = getTime(command.timeId());
        validatePastTime(command.date(), reservationTime);
        validateNoReservation(command);
        validateAlreadyWaiting(command);
        validateAlreadyReserved(command);
        long order = calculateWaitingOrder(command);
        Waiting waiting = new Waiting(null, command.date(), time, theme, member, order);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingInfo(savedWaiting);
    }

    private void validateAlreadyWaiting(WaitingAddCommand command) {
        boolean waitingExists = waitingRepository.existsByDateAndThemeIdAndTimeIdAndMemberId(
                command.date(),
                command.themeId(),
                command.timeId(),
                command.memberId()
        );
        if (waitingExists) {
            throw new IllegalArgumentException("이미 대기 중입니다.");
        }
    }

    private void validateAlreadyReserved(WaitingAddCommand command) {
        boolean reservationExists = reservationRepository.existsByDateAndThemeIdAndTimeIdAndMemberId(
                command.date(),
                command.themeId(),
                command.timeId(),
                command.memberId()
        );
        if (reservationExists) {
            throw new IllegalArgumentException("이미 예약되었습니다.");
        }
    }

    private void validatePastTime(LocalDate date, ReservationTime reservationTime) {
        if (date.isBefore(currentDateTime.getDate()) ||
                (date.isEqual(currentDateTime.getDate()) && reservationTime.isBefore(currentDateTime.getTime()))) {
            throw new IllegalArgumentException("지난 날짜 및 시간에는 대기할 수 없습니다.");
        }
    }

    private void validateNoReservation(WaitingAddCommand command) {
        boolean isReservationExists = reservationRepository.existsByDateAndTimeIdAndThemeId(command.date(),
                command.timeId(), command.themeId());
        if (!isReservationExists) {
            throw new IllegalArgumentException("해당 시간에 예약이 존재하지 않아 대기할 수 없습니다.");
        }
    }

    private long calculateWaitingOrder(WaitingAddCommand command) {
        return waitingRepository.countByDateAndTimeIdAndThemeId(command.date(), command.timeId(), command.themeId()) + 1;
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }
}
