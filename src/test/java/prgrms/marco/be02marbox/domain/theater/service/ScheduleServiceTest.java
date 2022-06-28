package prgrms.marco.be02marbox.domain.theater.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import prgrms.marco.be02marbox.domain.movie.Genre;
import prgrms.marco.be02marbox.domain.movie.LimitAge;
import prgrms.marco.be02marbox.domain.movie.Movie;
import prgrms.marco.be02marbox.domain.movie.repository.MovieRepository;
import prgrms.marco.be02marbox.domain.movie.service.utils.MovieConverter;
import prgrms.marco.be02marbox.domain.theater.Region;
import prgrms.marco.be02marbox.domain.theater.Schedule;
import prgrms.marco.be02marbox.domain.theater.Theater;
import prgrms.marco.be02marbox.domain.theater.TheaterRoom;
import prgrms.marco.be02marbox.domain.theater.dto.RequestCreateSchedule;
import prgrms.marco.be02marbox.domain.theater.repository.ScheduleRepository;
import prgrms.marco.be02marbox.domain.theater.repository.TheaterRepository;
import prgrms.marco.be02marbox.domain.theater.repository.TheaterRoomRepository;
import prgrms.marco.be02marbox.domain.theater.service.utils.ScheduleConverter;

@DataJpaTest
@Import({ScheduleService.class, ScheduleConverter.class, MovieConverter.class})
class ScheduleServiceTest {

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private TheaterRoomRepository theaterRoomRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private TheaterRepository theaterRepository;

	@Autowired
	private ScheduleService scheduleService;

	private TheaterRoom theaterRoom;

	private Movie movie;

	@BeforeEach
	void setup() {
		Theater theater = new Theater(Region.SEOUL, "강남");
		theaterRepository.save(theater);
		theaterRoom = new TheaterRoom(theater, "A관");
		theaterRoomRepository.save(theaterRoom);
		movie = new Movie("test", LimitAge.ADULT, Genre.ACTION, 100, "test");
		movieRepository.save(movie);
	}

	@Test
	@DisplayName("스케줄 생성 성공 테스트")
	void testCreateSchedule() {
		RequestCreateSchedule requestCreateSchedule = new RequestCreateSchedule(theaterRoom.getId(), movie.getId(),
			LocalDateTime.now(),
			LocalDateTime.now());

		Long id = scheduleService.createSchedule(requestCreateSchedule);

		Optional<Schedule> findSchedule = scheduleRepository.findById(id);

		assertAll(
			() -> assertThat(findSchedule).isPresent(),
			() -> assertThat(id).isEqualTo(findSchedule.get().getId()),
			() -> assertThat(movie.getId()).isEqualTo(findSchedule.get().getMovie().getId())
		);
	}

	@Test
	@DisplayName("TheaterRoom이 없을 경우 스케줄 생성 실패")
	void testCreateScheduleFail_TheaterRoom_No_Exists() {
		RequestCreateSchedule requestCreateSchedule = new RequestCreateSchedule(100L, movie.getId(),
			LocalDateTime.now(),
			LocalDateTime.now());

		assertThrows(IllegalArgumentException.class, () -> {
			scheduleService.createSchedule(requestCreateSchedule);
		});
	}

	@Test
	@DisplayName("Movie가 없을 경우 스케줄 생성 실패")
	void testCreateScheduleFail_Movie_No_Exists() {
		RequestCreateSchedule requestCreateSchedule = new RequestCreateSchedule(theaterRoom.getId(), 100L,
			LocalDateTime.now(),
			LocalDateTime.now());

		assertThrows(IllegalArgumentException.class, () -> {
			scheduleService.createSchedule(requestCreateSchedule);
		});
	}

	@Test
	@DisplayName("같은 영화에 대해서는 중복을 제거해서 리스트를 생성함")
	void testGetCurrentMovieList_No_Duplicate() {
		Theater theater2 = new Theater(Region.SEOUL, "테스트2");
		theaterRepository.save(theater2);
		TheaterRoom theaterRoom2 = new TheaterRoom(theater2, "테스트관");
		theaterRoomRepository.save(theaterRoom2);

		createAndSaveSchedule(theaterRoom, movie, LocalDateTime.now(), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom2, movie, LocalDateTime.now(), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom, movie, LocalDateTime.now().plusDays(1), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom2, movie, LocalDateTime.now().plusDays(2), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom, movie, LocalDateTime.now().plusDays(4), LocalDateTime.now());

		assertThat(scheduleService.getCurrentMovieList().size()).isEqualTo(1);
	}

	@Test
	@DisplayName("현재 상영되는 영화 정보는 현재 날짜를 기준으로 20일까지만(현재 날짜 + 19일까지만) 정보를 가져옴")
	void testGetCurrentMovieList_Only_In_19_Days() {
		Movie movie2 = new Movie("테스트2", LimitAge.CHILD, Genre.ACTION, 100, "/test/location");
		Movie movie3 = new Movie("테스트3", LimitAge.ADULT, Genre.ACTION, 120, "/test/location");
		Movie movie4 = new Movie("테스트4", LimitAge.ADULT, Genre.ANIMATION, 150, "/test/location");
		Movie movie5 = new Movie("테스트5", LimitAge.CHILD, Genre.ROMANCE, 160, "/test/location");

		movieRepository.save(movie2);
		movieRepository.save(movie3);
		movieRepository.save(movie4);
		movieRepository.save(movie5);

		createAndSaveSchedule(theaterRoom, movie, LocalDateTime.now(), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom, movie2, LocalDateTime.now(), LocalDateTime.now());
		createAndSaveSchedule(theaterRoom, movie3, LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10));
		createAndSaveSchedule(theaterRoom, movie4, LocalDateTime.now().plusDays(19), LocalDateTime.now().plusDays(19));
		createAndSaveSchedule(theaterRoom, movie5, LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(20));

		assertThat(scheduleService.getCurrentMovieList().size()).isEqualTo(4);
	}

	void createAndSaveSchedule(TheaterRoom theaterRoom, Movie movie, LocalDateTime startTime, LocalDateTime endTime) {
		Schedule schedule = Schedule.builder()
			.theaterRoom(theaterRoom)
			.movie(movie)
			.startTime(startTime)
			.endTime(endTime)
			.build();

		scheduleRepository.save(schedule);
	}

}