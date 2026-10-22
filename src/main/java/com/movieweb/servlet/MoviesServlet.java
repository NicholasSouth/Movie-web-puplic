package com.movieweb.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.movieweb.model.Movies;
import com.movieweb.service.MoviesService;
import com.movieweb.DAO.GenresDAO;
import com.movieweb.DAO.TagsDAO;
import com.movieweb.model.Genres;
import com.movieweb.model.Tags;

@WebServlet(urlPatterns = {"/movies", "/Movies"})
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(MoviesServlet.class.getName());
    private final MoviesService moviesService;
    private final GenresDAO genresDAO;
    private final TagsDAO tagsDAO;
    public MoviesServlet() {
    	this(
                new MoviesService(),
                new GenresDAO(),
                new TagsDAO()
            );
    }
    public MoviesServlet(
            MoviesService moviesService,
            GenresDAO genresDAO,
            TagsDAO tagsDAO) {
        this.moviesService = Objects.requireNonNull(moviesService);
        this.genresDAO = Objects.requireNonNull(genresDAO);
        this.tagsDAO = Objects.requireNonNull(tagsDAO);
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
    	MoviePageServlet.configure(req, resp);
        try {
            // 1. Parse search and filter parameters
            Movies filter = Movies.fromParameters(req.getParameterMap());

            // 2. Store filter for JSP
            req.setAttribute("filter", filter);
            
            //Load genres and tags for dropdowns
            List<Genres> genres = genresDAO.getAllGenres();
            List<Tags> tags = tagsDAO.getAllTags();
            req.setAttribute("genres", genres);
            req.setAttribute("tags", tags);
                      
            if (!filter.isHasFilters()) {
                // 3. Load default movie sections
                List<Movies> nowShowing = moviesService.getNowShowingMovies();
                List<Movies> comingSoon = moviesService.getComingSoonMovies();
                List<Movies> popular = moviesService.getPopularMovies();

                // 4. Send lists to JSP
                req.setAttribute("nowShowingMovies", nowShowing);
                req.setAttribute("comingSoonMovies", comingSoon);
                req.setAttribute("popularMovies", popular);

            } 
            else {
                // 5. Load filtered and paginated results
                List<Movies> filtered = moviesService.getCatalog(filter);
                req.setAttribute("filteredMovies", filtered);
            }

            // 6. Forward to your actual JSP
            req.getRequestDispatcher("/movies.jsp")
               .forward(req, resp);

        } 
        catch (IllegalArgumentException ex) {
            LOG.log(
                    Level.WARNING,
                    "Invalid movie filter parameters",
                    ex
            );
            MoviePageServlet.error(
                    req,
                    resp,
                    400,
                    "Invalid Filters",
                    ex.getMessage()
            );
        } 
        catch (SQLException ex) {
            LOG.log(
                    Level.SEVERE,
                    "Cannot load movies from database",
                    ex
            );
            MoviePageServlet.error(
                    req,
                    resp,
                    503,
                    "Unable to Load Movies",
                    "The system could not connect to the database. "
                    + "Please try again later."
            );
        }
    }
}