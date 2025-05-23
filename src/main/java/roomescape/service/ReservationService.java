package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationWaitingRank;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
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
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaReservationWaitingTicketRepository waitingTicketRepository;

    public ReservationService(final JpaReservationRepository reservationRepository,
                              final JpaReservationTimeRepository reservationTimeRepository,
                              final JpaThemeRepository themeRepository,
                              final JpaMemberRepository memberRepository,
                              JpaReservationWaitingTicketRepository waitingTicketRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingTicketRepository = waitingTicketRepository;
    }

    public ReservationResponseDto createReservation(ReservationCreateDto dto) {
        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        validateDuplicate(dto.date(), dto.timeId(), dto.themeId());
        Reservation.validateReservableTime(dto.date(), reservationTime.getStartAt());

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Reservation requestReservation = Reservation.createWithoutId(member, dto.date(), reservationTime, theme);
        Reservation newReservation = reservationRepository.save(requestReservation);

        return ReservationResponseDto.of(
                newReservation,
                newReservation.getTime(),
                theme
        );
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId) {
        List<Reservation> reservations = reservationRepository.findReservationByDateAndTimeIdAndThemeId(date, timeId,
                themeId);
        if (!reservations.isEmpty()) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다. 예약 대기 기능을 사용해주세요.");
        }
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
        waitingTicketRepository.save(new ReservationWaitingTicket(newReservation));
        return ReservationResponseDto.of(
                newReservation,
                reservationTime,
                theme
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from,
                                                               LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponseDto> findMyReservations(LoginInfo loginInfo) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(loginInfo.id());
        return reservations.stream().map(reservation -> {
            if (reservation.isReservationWaiting()) {
                ReservationWaitingTicket reservationWaitingTicket = waitingTicketRepository.findByReservationId(
                        reservation.getId());
                ReservationWaitingRank rank = waitingTicketRepository.countReservationWaitingsByThemeIdAndDateAndTimeIdAndCreatedAt(
                        reservation.getTheme().getId(),
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservationWaitingTicket.getCreatedAt()
                );
                return new MyReservationResponseDto(
                        reservation, rank
                );
            }
            return new MyReservationResponseDto(reservation);
        }).toList();
    }

    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        reservationRepository.deleteById(id);

   //todo findById()를 바로 해오면 쿼리가 3번 -> 2번이 됨
    }

    public void deleteReservationWaiting(Long id) {
        Optional<ReservationWaitingTicket> reservationWaitingTicket = waitingTicketRepository.findById(id);
        if (reservationWaitingTicket.isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        waitingTicketRepository.deleteById(id);
    }
}
