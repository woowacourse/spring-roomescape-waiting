package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
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
        ReservationTime time = findReservationTime(reservationDto);
        Theme theme = findTheme(reservationDto);
        Member member = findMember(reservationDto);

        LocalDate date = reservationDto.getDate();
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), reservationDto.getThemeId());

        // TODO: 이렇게 모든 객체를 다 찾아서 Reservation 객체를 만드는 게 맞을까?
        Reservation reservation = Reservation.of(reservationDto, time, theme, member);
        return reservationRepository.save(reservation);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Theme findTheme(ReservationDto reservationDto) {
        long themeId = reservationDto.getThemeId();
        Optional<Theme> theme = themeRepository.findById(themeId);
        return theme.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Member findMember(ReservationDto reservationDto) {
        long memberId = reservationDto.getMemberId();
        Optional<Member> member = memberRepository.findById(memberId);
        return member.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    public void deleteReservation(long id) {
        // TODO: 본인의 예약인지 검증
        validateExistence(id);
        Reservation reservation = reservationRepository.findById(id) //
                .orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
        LocalDate reservationDate = reservation.getDate();
        ReservationTime reservationTime = reservation.getTime();
        Theme reservationTheme = reservation.getTheme();
        Optional<Waiting> rawWaiting = waitingRepository.findOneByDateAndTimeAndTheme(
                reservationDate, reservationTime, reservationTheme);
        if (rawWaiting.isPresent()) {
            Waiting waiting = rawWaiting.get();
            LocalDate date = waiting.getDate();
            ReservationTime time = waiting.getTime();
            Theme theme = waiting.getTheme();
            Member member = waiting.getMember();
            Waiting deletedWaiting = waitingRepository.findFirstByDateAndTimeAndTheme(date, time, theme); //
            waitingRepository.delete(deletedWaiting);

            Reservation approvedReservation = new Reservation(date, time, theme, member);
            reservationRepository.save(approvedReservation);
        }
        reservationRepository.deleteById(id);
    }

    public ReservationTimeInfoDto findReservationTimesInformation(LocalDate date, long themeId) {
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeBooked(date, themeId);
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return new ReservationTimeInfoDto(bookedTimes, allTimes);
    }

    public List<Reservation> findReservationsByConditions(long memberId, long themeId, LocalDate from, LocalDate to) {
        return reservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
    }

    public List<Reservation> findReservationsByMember(LoginMember member) {
        return reservationRepository.findByMemberId(member.getId());
    }

    private void validateIsFuture(LocalDate date, LocalTime time) {
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId) {
        boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
        }
    }

    private void validateExistence(long id) {
        boolean isNotExist = !reservationRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약입니다.");
        }
    }
}
