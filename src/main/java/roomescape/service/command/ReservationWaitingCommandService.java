package roomescape.service.command;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizationException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

@Service
@Transactional
public class ReservationWaitingCommandService {

    private final JpaReservationWaitingTicketRepository reservationWaitingTicketRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final Clock clock;

    public ReservationWaitingCommandService(JpaReservationWaitingTicketRepository reservationWaitingTicketRepository,
                                            JpaReservationRepository reservationRepository,
                                            JpaReservationTimeRepository reservationTimeRepository,
                                            JpaThemeRepository themeRepository, JpaMemberRepository memberRepository,
                                            Clock clock) {
        this.reservationWaitingTicketRepository = reservationWaitingTicketRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }


    public ReservationResponseDto createReservationWaiting(ReservationCreateDto request) {
        //todo 같은 멤버 검증
        // 1. 요청한 시간, 테마, 멤버 반환
        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("예약 시간을 찾을 수 없습니다. id : " + request.timeId()));

        Reservation.validateReservableTime(request.date(), reservationTime.getStartAt(), LocalDateTime.now(clock));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다. id : " + request.themeId()));

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다. id : " + request.memberId()));

        // 2. 예약 대기 등록
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                request.timeId(),
                request.themeId())
        ) {
            throw new IllegalArgumentException("현재 예약이 존재하지 않습니다. 예약하기 기능을 이용해주세요.");
        }

        Reservation requestReservation = new Reservation(member, request.date(), reservationTime, theme, ReservationStatus.WAITING);
        Reservation newReservation = reservationRepository.save(requestReservation);
        reservationWaitingTicketRepository.save(new ReservationWaitingTicket(newReservation));
        return ReservationResponseDto.of(newReservation);
    }

    public void deleteReservationWaiting(Long id, LoginInfo loginInfo) {
        Reservation reservationWaiting = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다."));

        if (reservationWaiting.getMember().getId() != loginInfo.id()) {
            throw new UnauthorizationException("예약자만 예약 대기 취소가 가능합니다.");
        }

        //todo cascade delete 개선
        reservationWaitingTicketRepository.delete(reservationWaitingTicketRepository.findByReservationId(id).get());
        reservationRepository.delete(reservationWaiting);
    }
}
