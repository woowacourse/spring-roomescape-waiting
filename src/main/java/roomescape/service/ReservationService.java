package roomescape.service;

import static roomescape.model.Reservation.createAcceptReservation;
import static roomescape.model.Reservation.createWaiting;
import static roomescape.model.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.ReservationRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.MemberReservation;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> filterReservation(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        return reservationRepository.findByThemeAndMemberAndDateBetween(theme, member, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationRequest request, Member member) {
        validateDuplicatedReservation(request.date(), request.timeId(), request.themeId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Reservation reservation = createAcceptReservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public Reservation addWaitingReservation(ReservationRequest request, Member member) {
        validateDuplicatedWaitingReservation(request.date(), request.timeId(), request.themeId(), member.getId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Reservation reservation = createWaiting(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public Reservation addReservation(AdminReservationRequest request) {
        validateDuplicatedReservation(request.date(), request.timeId(), request.themeId());
        ReservationTime reservationTime = findReservationTime(request.date(), request.timeId());
        Theme theme = findThemeById(request.themeId());
        Member member = findMemberById(request.memberId());
        Reservation reservation = createAcceptReservation(request.date(), reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public List<MemberReservation> findMemberReservations(Member member) {
        return reservationRepository.findMemberReservation(member.getId());
    }

    public List<Reservation> findWaitingReservations() {
        return reservationRepository.findAllReservationByStatus(WAITING);
    }

    private void validateReservationDateTimeBeforeNow(LocalDate date, LocalTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("현재(%s) 이전 시간으로 예약할 수 없습니다.".formatted(now));
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long timeId, Long themeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간이 존재하지 않습니다."));
        Theme theme = findThemeById(themeId);
        long countReservation = reservationRepository.countByDateAndTimeAndTheme(date, reservationTime, theme);
        if (countReservation > 0) {
            throw new DuplicatedException("이미 해당 시간에 예약이 존재합니다.");
        }
    }

    private void validateDuplicatedWaitingReservation(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchElementException("예약 시간이 존재하지 않습니다."));
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        long countReservation = reservationRepository
                .countByDateAndTimeAndThemeAndMember(date, reservationTime, theme, member);
        if (countReservation > 0) {
            throw new DuplicatedException("이미 예약을 했거나 예약 대기를 걸어놓았습니다.");
        }
    }

    @Transactional
    public void deleteReservation(long id) {
        validateExistReservation(id);
        Reservation reservation = findReservationById(id);
        if (reservation.isAcceptReservation()) {
            List<Reservation> waitingReservations = findWaitingReservation(reservation);
            confirmReservation(waitingReservations);
        }
        reservationRepository.deleteById(id);
    }

    private Reservation findReservationById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 예약은 존재하지 않습니다.".formatted(id)));
    }

    private List<Reservation> findWaitingReservation(Reservation reservation) {
        return reservationRepository.findWaitingReservationsByReservation(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());
    }

    private void confirmReservation(List<Reservation> waitingReservations) {
        waitingReservations.stream()
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .ifPresent(Reservation::confirmReservation);
    }

    private void validateExistReservation(long id) {
        long count = reservationRepository.countById(id);
        if (count <= 0) {
            throw new NotFoundException("해당 id:[%s] 값으로 예약된 내역이 존재하지 않습니다.".formatted(id));
        }
    }

    private ReservationTime findReservationTime(LocalDate date, long timeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 예약 시간이 존재하지 않습니다.".formatted(timeId)));
        validateReservationDateTimeBeforeNow(date, reservationTime.getStartAt());
        return reservationTime;
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 테마가 존재하지 않습니다.".formatted(themeId)));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("아이디가 %s인 사용자가 존재하지 않습니다.".formatted(memberId)));
    }
}
