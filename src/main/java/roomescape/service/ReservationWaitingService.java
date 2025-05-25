package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

@Service
@Transactional
public class ReservationWaitingService {

    private final JpaReservationWaitingTicketRepository reservationWaitingTicketRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;

    public ReservationWaitingService(JpaReservationWaitingTicketRepository reservationWaitingTicketRepository,
                                     JpaReservationRepository reservationRepository,
                                     JpaReservationTimeRepository reservationTimeRepository,
                                     JpaThemeRepository themeRepository, JpaMemberRepository memberRepository) {
        this.reservationWaitingTicketRepository = reservationWaitingTicketRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }


    public ReservationResponseDto createReservationWaiting(ReservationCreateDto createDto) {
        //todo 같은 멤버 검증
        // 1. 요청한 시간, 테마, 멤버 반환
        ReservationTime reservationTime = reservationTimeRepository.findById(createDto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + createDto.timeId()));

        Reservation.validateReservableTime(createDto.date(), reservationTime.getStartAt());

        Theme theme = themeRepository.findById(createDto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + createDto.themeId()));

        Member member = memberRepository.findById(createDto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + createDto.memberId()));

        // 2. 예약 대기 등록
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                createDto.date(),
                createDto.timeId(),
                createDto.themeId())
        ) {
            throw new IllegalArgumentException("[ERROR] 현재 예약이 존재하지 않습니다. 예약하기 기능을 이용해주세요.");
        }

        Reservation requestReservation = Reservation.createWaitingWithoutId(member, createDto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);
        reservationWaitingTicketRepository.save(new ReservationWaitingTicket(newReservation));
        return ReservationResponseDto.of(newReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findAllReservationWaitings() {
        return reservationRepository.findReservationsByStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public void deleteReservationWaiting(Long id) {
        Optional<Reservation> reservationWaiting = reservationRepository.findById(id);
        if (reservationWaiting.isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);
    }
}
