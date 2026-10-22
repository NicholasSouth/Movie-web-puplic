package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Movies;
import com.movieweb.model.Users;
import com.movieweb.model.Actors;
import com.movieweb.model.Directors;
import com.movieweb.model.Authors;
import com.movieweb.service.FavouriteMoviesService;
import com.movieweb.service.MoviesService;
import com.movieweb.service.MovieCastService;
import com.movieweb.DAO.ShowtimesDAO;
import com.movieweb.model.Showtimes;
import com.movieweb.model.Theaters;
import com.movieweb.model.Ratings;
import com.movieweb.model.Comments;
import com.movieweb.service.RatingsService;
import com.movieweb.service.CommentsService;

@WebServlet(urlPatterns = {
        "/movie-details",
        "/movie_details",
        "/Movie_details"
})
public class MovieDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MovieDetailsServlet.class.getName());
    private final MoviesService moviesService;
    private final FavouriteMoviesService favouriteMoviesService;
    private final MovieCastService movieCastService;
    //for showing nearest showtimes
    private final ShowtimesDAO showtimesDAO;
    //for rating and comments
    private final RatingsService ratingsService;
    private final CommentsService commentsService;
    public MovieDetailsServlet() {
        this(
            new MoviesService(),
            new FavouriteMoviesService(),
            new MovieCastService(),
            new ShowtimesDAO(),
            new RatingsService(),
            new CommentsService()
        );
    }
    public MovieDetailsServlet(
            MoviesService moviesService,
            FavouriteMoviesService favouriteMoviesService,
            MovieCastService movieCastService,
            ShowtimesDAO showtimesDAO,
            RatingsService ratingsService,
            CommentsService commentsService) {
        this.moviesService = Objects.requireNonNull(moviesService);
        this.favouriteMoviesService = Objects.requireNonNull(favouriteMoviesService);
        this.movieCastService = Objects.requireNonNull(movieCastService);
        this.showtimesDAO = Objects.requireNonNull(showtimesDAO);
        this.ratingsService = Objects.requireNonNull(ratingsService);
        this.commentsService = Objects.requireNonNull(commentsService);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        MoviePageServlet.configure(request, response);
        try {
            int movieId = MoviePageServlet.movieId(request);
            Users user = MoviePageServlet.user(request);
            String favourite = request.getParameter("favourite");
            Movies movie = moviesService.getDetails(
                            movieId,
                            user == null
                                    ? null
                                    : user.getUserId()
                    );            
            if (movie == null) {
                MoviePageServlet.error(
                        request,
                        response,
                        404,
                        "Movie Not Found",
                        "The movie does not exist or is not currently public."
                );
                return;
            }
            // Load movie cast
            List<Actors> actors = movieCastService.getActorsByMovieId(movieId);
            List<Directors> directors = movieCastService.getDirectorsByMovieId(movieId);
            List<Authors> authors = movieCastService.getAuthorsByMovieId(movieId);
            
            //Load showtimes
            Map<Showtimes, Theaters> movieShowtimes = showtimesDAO.getTop5ShowtimesByMovieId(movieId);
            
            //Set favourite status
            boolean isFavourite = false;
            if (user != null) {
                isFavourite = favouriteMoviesService.isFavourite(
                                user.getUserId(),
                                movieId
                        );
            }
            
            //Load comments and ratings
            // Load user's rating
            Ratings userRating = null;
            if (user != null) {
                userRating = ratingsService.getRating(user.getUserId(), movieId);
            }

            // Load movie comments
            List<Comments> comments = commentsService.getCommentsByMovieId(movieId);
            Map<Integer, Users> commentUsers = commentsService.getCommentUsersByMovieId(movieId);

            // Pass data to JSP
            request.setAttribute("movie", movie);
            request.setAttribute("actors", actors);
            request.setAttribute("directors", directors);
            request.setAttribute("authors", authors);
            request.setAttribute("loggedInUser", user);
            request.setAttribute("isFavourite", isFavourite);
            request.setAttribute("csrfToken", MoviePageServlet.csrfToken(request));
            request.setAttribute("movieShowtimes", movieShowtimes);
            request.setAttribute("userRating", userRating);
            request.setAttribute("comments", comments);
            request.setAttribute("commentUsers", commentUsers);
            if ("added".equals(favourite)) {
                request.setAttribute(
                        "favouriteMessage",
                        "Movie added to your favorites."
                );
            } 
            else if ("removed".equals(favourite)) {
                request.setAttribute(
                        "favouriteMessage",
                        "Movie removed from your favorites."
                );
            } 
            else if ("error".equals(favourite)) {
                request.setAttribute(
                        "favouriteMessage",
                        "Unable to update your favorites."
                );
            }

            // JSP is directly inside webapp
            request.getRequestDispatcher("/movie_details.jsp").forward(request, response);
        } 
        catch (IllegalArgumentException ex) {
            MoviePageServlet.error(
                    request,
                    response,
                    400,
                    "Invalid Movie ID",
                    ex.getMessage()
            );
        } 
        catch (SQLException ex) {
            LOG.log(
                    Level.SEVERE,
                    "Cannot load movie details from database",
                    ex
            );
            MoviePageServlet.error(
                    request,
                    response,
                    503,
                    "Unable to Load Movie Details",
                    "The database is currently unavailable. Please try again later."
            );
        }
    }
}