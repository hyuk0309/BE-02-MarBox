package prgrms.marco.be02marbox.domain.movie.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prgrms.marco.be02marbox.domain.movie.Movie;
import prgrms.marco.be02marbox.domain.movie.dto.RequestCreateMovie;
import prgrms.marco.be02marbox.domain.movie.repository.MovieRepository;
import prgrms.marco.be02marbox.domain.movie.service.utils.MovieConverter;

@Service
public class MovieService {

	private final MovieRepository movieRepository;

	private final MovieConverter movieConverter;

	public MovieService(MovieRepository movieRepository,
		MovieConverter movieConverter) {
		this.movieRepository = movieRepository;
		this.movieConverter = movieConverter;
	}

	@Transactional
	public Long createMovie(RequestCreateMovie requestCreateMovie) {
		Movie newMovie = movieConverter.convertFromRequestCreateMovieToMovie(requestCreateMovie);
		return movieRepository
			.save(newMovie)
			.getId();
	}

	@Transactional(readOnly = true)
	public List<Movie> getMovies(int page, int size) {
		return movieRepository
			.findAll(PageRequest.of(page, size))
			.getContent();
	}
}