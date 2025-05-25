package roomescape.service.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.ReservationCreateDto;

@Service
@Transactional
public class ReservationCommandService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaReservationWaitingTicketRepository waitingTicketRepository;

    public ReservationCommandService(JpaReservationRepository reservationRepository,
                                     JpaReservationTimeRepository reservationTimeRepository,
                                     JpaThemeRepository themeRepository, JpaMemberRepository memberRepository,
                                     JpaReservationWaitingTicketRepository waitingTicketRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingTicketRepository = waitingTicketRepository;
    }

    public ReservationResponseDto bookReservation(ReservationCreateDto request) {
        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("예약 시간을 찾을 수 없습니다. id : " + request.timeId()));

        validateDuplicate(request.date(), request.timeId(), request.themeId());
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다. id : " + request.themeId()));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다. id : " + request.memberId()));
        Reservation requestReservation = new Reservation(member, request.date(), reservationTime, theme, ReservationStatus.RESERVED);
        requestReservation.validateReservableTime(LocalDateTime.now());

        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponseDto.of(newReservation);
    }

    public void cancelReservationBy(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new NotFoundException("등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);

        updateReservationWaitingToReservation(reservation.get());
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndTimeIdAndThemeIdAndStatus(
                date, timeId, themeId, ReservationStatus.RESERVED);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("이미 예약이 존재합니다. 예약 대기 기능을 사용해주세요.");
        }
    }

    private void updateReservationWaitingToReservation(Reservation deletedReservation) {
        List<Reservation> reservationWaitings = reservationRepository.findReservationsByDateAndTimeIdAndThemeIdAndStatus(
                deletedReservation.getDate(),
                deletedReservation.getTime().getId(),
                deletedReservation.getTheme().getId(),
                ReservationStatus.WAITING
        );

        if (reservationWaitings.isEmpty()) {
            return;
        }

        Reservation earliestWaiting = reservationWaitings.stream()
                .map(waiting -> waitingTicketRepository.findByReservationId(waiting.getId())
                        .orElseThrow(() -> new NotFoundException("예약 대기 티켓을 찾을 수 없습니다. 예약 ID: " + waiting.getId())))
                .min(Comparator.comparing(ReservationWaitingTicket::getCreatedAt))
                .orElseThrow(() -> new NotFoundException("가장 오래된 예약 대기 티켓을 찾을 수 없습니다."))
                .getReservation();

        waitingTicketRepository.deleteByReservationId(earliestWaiting.getId());
        earliestWaiting.setStatus(ReservationStatus.RESERVED);
    }
}
