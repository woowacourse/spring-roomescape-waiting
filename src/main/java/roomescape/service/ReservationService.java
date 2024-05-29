package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.model.Reservation;
import roomescape.model.ReservationInfo;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.*;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              MemberRepository memberRepository,
                              ThemeRepository themeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation saveReservation(ReservationDto reservationDto) {
        LocalDate date = reservationDto.getDate();
        ReservationTime time = findReservationTime(reservationDto);
        Theme theme = findTheme(reservationDto);
        Member member = findMember(reservationDto);

        validateIsFuture(date, time);
        validateDuplication(date, time, theme);

        Reservation reservation = new Reservation(date, time, theme, member);
        return reservationRepository.save(reservation);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 시간에 대한 예약입니다."));
    }

    private Theme findTheme(ReservationDto reservationDto) {
        long themeId = reservationDto.getThemeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 테마에 대한 예약입니다."));
    }

    private Member findMember(ReservationDto reservationDto) {
        long memberId = reservationDto.getMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 사용자에 대한 예약입니다."));
    }

    public void deleteReservation(long id) {
        Reservation reservation = findReservation(id);
        findFirstWaiting(reservation).ifPresent(this::approveWaiting);
        reservationRepository.deleteById(id);
    }

    private Reservation findReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 예약입니다."));
    }

    private Optional<Waiting> findFirstWaiting(Reservation reservation) {
        ReservationInfo reservationInfo = reservation.getReservationInfo();
        return waitingRepository.findFirstByReservationInfo(reservationInfo);
    }

    private void approveWaiting(Waiting waiting) {
        Reservation reservation = waiting.toReservation();
        reservationRepository.save(reservation);
        waitingRepository.delete(waiting);
    }

    public ReservationTimeInfoDto findReservationTimesInformation(LocalDate date, long themeId) {
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeByDateAndThemeId(date, themeId);
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return new ReservationTimeInfoDto(bookedTimes, allTimes);
    }

    public List<Reservation> searchReservationsByConditions(long memberId, long themeId, LocalDate from, LocalDate to) {
        return reservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
    }

    public List<Reservation> findReservationsByMember(LoginMember member) {
        return reservationRepository.findByMemberId(member.getId());
    }

    private void validateIsFuture(LocalDate date, ReservationTime reservationTime) {
        LocalTime time = reservationTime.getStartAt();
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, ReservationTime time, Theme theme) {
        ReservationInfo reservationInfo = new ReservationInfo(date, time, theme);
        boolean isExist = reservationRepository.existsByReservationInfo(reservationInfo);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
        }
    }
}
