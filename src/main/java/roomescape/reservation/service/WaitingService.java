package roomescape.reservation.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.ReservationRequest;
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
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WaitingResponse addWaiting(ReservationRequest reservationRequest, MemberRequest memberRequest) {
        ReservationTime reservationTime = reservationTimeRepository.getById(reservationRequest.timeId());
        Theme theme = themeRepository.getById(reservationRequest.themeId());

        Reservation reservation = reservationRepository.findByDateAndReservationTimeAndTheme(
                        reservationRequest.date(),
                        reservationTime,
                        theme)
                .orElseThrow(() -> new NotFoundException("기존 예약이 존재하지 않아 예약 대기가 불가능합니다. 예약 신청으로 다시 시도해주세요."));

        Member member = memberRequest.toMember();
        if (reservation.isSameMember(member)) {
            throw new DuplicationException("이미 예약에 성공하셨으므로 예약 대기를 걸 수 없습니다.");
        }

        Waiting waiting = new Waiting(reservationRequest.date(), reservationTime, theme, member);
        Waiting save = waitingRepository.save(waiting);

        return new WaitingResponse(save);
    }
}
