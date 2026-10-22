package com.movieweb.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.movieweb.DAO.AdminMoviesDAO;
import com.movieweb.DAO.GenresDAO;
import com.movieweb.DAO.TagsDAO;
import com.movieweb.model.AdminMovieInput;
import com.movieweb.service.MovieImageService;

/* Admin movie catalogue: list/search, Add Movie and Edit Movie (poster upload, genres, tags and cast).
 * Authorization is enforced by AdminFilter. Movies are global, so the admin theater selector is not used.
 * A movie shows on the customer pages when it is active and not deleted (MoviesDAO.VISIBLE); whether it is
 * "now showing" or "coming soon" follows from its availability dates.
 * Flow: GET shows the list (and the form for ?new=1 or ?edit=ID) -> POST action=create or update validates,
 * saves a new poster file if one was chosen, calls AdminMoviesDAO.create / update (one transaction)
 * -> redirect back with a one-time message (PRG).
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
@WebServlet("/admin-movies")
public class AdminMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;
    /* Keep these at or below the real column sizes of your Movies table. */
    private static final int NAME_MAX = 150;
    private static final int DESCRIPTION_MAX = 4000;
    private static final int AGE_MAX = 20;
    private static final int TRAILER_MAX = 500;
    private static final int PERSON_MAX = 100;
    private static final int PEOPLE_MAX = 30;
    private static final int DURATION_MAX = 600;

    private final AdminMoviesDAO dao = new AdminMoviesDAO();
    private final GenresDAO genresDAO = new GenresDAO();
    private final TagsDAO tagsDAO = new TagsDAO();
    private MovieImageService images;

    @Override
    public void init() throws ServletException {
        images = new MovieImageService(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String search = request.getParameter("q");
        search = search == null ? "" : search.trim();
        if (search.length() > NAME_MAX) {
            search = search.substring(0, NAME_MAX);
        }
        int page = parseInt(request.getParameter("page"), 1);
        try {
            int total = dao.count(search);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(Math.max(page, 1), pages);
            request.setAttribute("movieRows", dao.list(search, (page - 1) * PAGE_SIZE, PAGE_SIZE));
            request.setAttribute("totalMovies", total);
            request.setAttribute("page", page);
            request.setAttribute("pages", pages);
        } catch (SQLException e) {
            getServletContext().log("Failed to load admin movies page", e);
            request.setAttribute("loadError", Boolean.TRUE);
        }
        int editId = parseInt(request.getParameter("edit"), 0);
        if (request.getAttribute("form") != null) {
            // Re-displaying a form after failed validation (see doPost): keep it as is.
        } else if (editId > 0) {
            try {
                AdminMovieInput movie = dao.getById(editId);
                if (movie == null) {
                    request.setAttribute("flashError", "That movie no longer exists.");
                } else {
                    request.setAttribute("form", toForm(movie));
                    request.setAttribute("selectedGenres", new HashSet<Integer>(movie.genreIds));
                    request.setAttribute("selectedTags", new HashSet<Integer>(movie.tagIds));
                    request.setAttribute("formMode", "edit");
                    request.setAttribute("formId", editId);
                    request.setAttribute("formPosterPath", movie.posterPath);
                }
            } catch (SQLException e) {
                getServletContext().log("Failed to load movie " + editId + " for editing", e);
                request.setAttribute("flashError", "The movie could not be loaded for editing.");
            }
        } else if ("1".equals(request.getParameter("new"))) {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("isActive", "true");
            request.setAttribute("form", form);
            request.setAttribute("formMode", "create");
            request.setAttribute("selectedGenres", new HashSet<Integer>());
            request.setAttribute("selectedTags", new HashSet<Integer>());
        }
        if (request.getAttribute("form") != null) { // the form needs the genre and tag checkboxes
            request.setAttribute("allGenres", genresDAO.getAllGenres());
            request.setAttribute("allTags", tagsDAO.getAllTags());
        }
        request.setAttribute("q", search);
        HttpSession session = request.getSession(false);
        if (session != null) { // one-time messages from the previous POST (PRG)
            Object ok = session.getAttribute("adminFlash");
            Object err = session.getAttribute("adminFlashError");
            if (ok != null) request.setAttribute("flash", ok);
            if (err != null && request.getAttribute("flashError") == null) request.setAttribute("flashError", err);
            session.removeAttribute("adminFlash");
            session.removeAttribute("adminFlashError");
        }
        request.getRequestDispatcher("/admin_movies.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        int id = parseInt(request.getParameter("id"), 0);
        String message = "Invalid request.";
        boolean error = true;
        boolean creating = "create".equals(action);
        if (creating || ("update".equals(action) && id > 0)) {
            Map<String, String> form = readForm(request);
            Set<Integer> genres = readIds(request.getParameterValues("genreIds"));
            Set<Integer> tags = readIds(request.getParameterValues("tagIds"));
            AdminMovieInput input = new AdminMovieInput();
            String problem = validate(form, input);
            Part poster = null;
            if (problem == null) {
                try {
                    poster = request.getPart("poster");
                } catch (IllegalStateException e) {
                    problem = "The poster is too large. Maximum size is 5 MB.";
                }
                if (poster != null && poster.getSize() == 0) {
                    poster = null;
                }
                if (problem == null && creating && poster == null) {
                    problem = "Please choose a poster image.";
                }
            }
            String newPoster = null;
            AdminMovieInput existing = null;
            try {
                if (!creating) {
                    existing = dao.getById(id);
                    if (problem == null && existing == null) {
                        problem = "That movie no longer exists.";
                    }
                }
                if (problem == null && dao.nameExists(input.name, creating ? 0 : id)) {
                    problem = "Another movie already has this name.";
                }
                if (problem == null && poster != null) {
                    try {
                        newPoster = images.save(poster);
                    } catch (IllegalArgumentException e) {
                        problem = e.getMessage();
                    }
                }
                if (problem != null) {
                    // Validation failed: redisplay the form with what was typed (no redirect).
                    // The chosen file cannot be kept by the browser, so the admin picks the poster again.
                    request.setAttribute("form", form);
                    request.setAttribute("selectedGenres", genres);
                    request.setAttribute("selectedTags", tags);
                    request.setAttribute("formMode", creating ? "create" : "edit");
                    request.setAttribute("formId", id);
                    if (existing != null) {
                        request.setAttribute("formPosterPath", existing.posterPath);
                    }
                    request.setAttribute("flashError", problem);
                    doGet(request, response);
                    return;
                }
                input.movieId = id;
                input.posterPath = newPoster; // null on update = keep the current poster
                input.genreIds = new ArrayList<>(genres);
                input.tagIds = new ArrayList<>(tags);
                boolean saved = false;
                try {
                    if (creating) {
                        dao.create(input);
                        saved = true;
                        message = "Movie \"" + input.name + "\" was added."
                                + (input.active ? "" : " It is hidden from customers until it is activated.");
                    } else {
                        saved = dao.update(input);
                        message = saved ? "Movie \"" + input.name + "\" was updated."
                                        : "That movie no longer exists. Nothing was changed.";
                    }
                } finally {
                    if (!saved) {
                        images.deleteQuietly(newPoster); // do not leave an unused upload behind
                    }
                }
                if (saved) {
                    error = false;
                    if (!creating && newPoster != null && existing != null) {
                        images.deleteQuietly(existing.posterPath); // replaced poster (hand-placed files are left alone)
                    }
                }
            } catch (SQLException e) {
                getServletContext().log("Admin " + action + " movie failed, id=" + id, e);
                message = "The movie could not be saved because of a database error. Nothing was changed.";
                error = true;
            }
        }
        request.getSession().setAttribute(error ? "adminFlashError" : "adminFlash", message);
        String q = request.getParameter("q");
        response.sendRedirect(request.getContextPath() + "/admin-movies?q="
                + java.net.URLEncoder.encode(q == null ? "" : q.trim(), "UTF-8")
                + "&page=" + Math.max(parseInt(request.getParameter("page"), 1), 1));
    }

    /* Form values for the edit form (dates as yyyy-MM-dd for <input type="date">, names separated by commas). */
    private Map<String, String> toForm(AdminMovieInput m) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("name", nz(m.name));
        form.put("description", nz(m.description));
        form.put("ageRating", nz(m.ageRating));
        form.put("duration", String.valueOf(m.durationMinute));
        form.put("availableFrom", m.availableFrom == null ? "" : m.availableFrom.toLocalDateTime().toLocalDate().toString());
        form.put("availableUntil", m.availableUntil == null ? "" : m.availableUntil.toLocalDateTime().toLocalDate().toString());
        form.put("trailerLink", nz(m.trailerLink));
        form.put("actors", String.join(", ", m.actors));
        form.put("directors", String.join(", ", m.directors));
        form.put("authors", String.join(", ", m.authors));
        form.put("isActive", m.active ? "true" : "false");
        return form;
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private Map<String, String> readForm(HttpServletRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        for (String key : new String[] {"name", "description", "ageRating", "duration", "availableFrom",
                "availableUntil", "trailerLink", "actors", "directors", "authors"}) {
            String v = request.getParameter(key);
            form.put(key, v == null ? "" : v.trim());
        }
        form.put("isActive", request.getParameter("isActive") != null ? "true" : "false");
        return form;
    }

    private Set<Integer> readIds(String[] values) {
        Set<Integer> ids = new HashSet<>();
        if (values != null) {
            for (String v : values) {
                int id = parseInt(v, 0);
                if (id > 0) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    /* Returns a problem message, or null when valid (then the matching fields of input are filled). */
    private String validate(Map<String, String> form, AdminMovieInput input) {
        String name = form.get("name");
        if (name.isEmpty() || name.length() > NAME_MAX) {
            return "The movie name is required and must not exceed " + NAME_MAX + " characters.";
        }
        if (form.get("description").length() > DESCRIPTION_MAX) {
            return "The description must not exceed " + DESCRIPTION_MAX + " characters.";
        }
        if (form.get("ageRating").length() > AGE_MAX) {
            return "The age rating must not exceed " + AGE_MAX + " characters.";
        }
        int duration = parseInt(form.get("duration"), 0);
        if (duration < 1 || duration > DURATION_MAX) {
            return "The duration must be a whole number of minutes between 1 and " + DURATION_MAX + ".";
        }
        LocalDate from;
        LocalDate until = null;
        try {
            from = LocalDate.parse(form.get("availableFrom"));
            if (!form.get("availableUntil").isEmpty()) {
                until = LocalDate.parse(form.get("availableUntil"));
            }
        } catch (DateTimeParseException e) {
            return "Please enter a valid \"Available from\" date. \"Available until\" can be left empty.";
        }
        if (until != null && until.isBefore(from)) {
            return "\"Available until\" must not be before \"Available from\".";
        }
        String trailer = form.get("trailerLink");
        if (!trailer.isEmpty() && (trailer.length() > TRAILER_MAX || !isHttpUrl(trailer))) {
            return "The trailer link must be a full web address starting with http:// or https:// (up to "
                    + TRAILER_MAX + " characters).";
        }
        List<String> actors = splitNames(form.get("actors"));
        List<String> directors = splitNames(form.get("directors"));
        List<String> authors = splitNames(form.get("authors"));
        for (List<String> people : java.util.Arrays.asList(actors, directors, authors)) {
            if (people.size() > PEOPLE_MAX) {
                return "Please list at most " + PEOPLE_MAX + " names in each of actors, directors and authors.";
            }
            for (String person : people) {
                if (person.length() > PERSON_MAX) {
                    return "Each actor, director or author name must not exceed " + PERSON_MAX + " characters.";
                }
            }
        }
        input.name = name;
        input.description = form.get("description");
        input.ageRating = form.get("ageRating");
        input.durationMinute = duration;
        input.availableFrom = Timestamp.valueOf(from.atStartOfDay());
        input.availableUntil = until == null ? null : Timestamp.valueOf(until.atStartOfDay());
        input.trailerLink = trailer;
        input.active = "true".equals(form.get("isActive"));
        input.actors = actors;
        input.directors = directors;
        input.authors = authors;
        return null;
    }

    /* "Tom Hardy, Zendaya" (commas or new lines) -> ["Tom Hardy", "Zendaya"], without empties or repeats. */
    private List<String> splitNames(String text) {
        List<String> names = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String part : text.split("[,\\r\\n]+")) {
            String n = part.trim().replaceAll("\\s+", " ");
            if (!n.isEmpty() && seen.add(n.toLowerCase(Locale.ROOT))) {
                names.add(n);
            }
        }
        return names;
    }

    /* Only http(s) links are accepted, so a link can never run script when it is shown in an href. */
    private boolean isHttpUrl(String link) {
        try {
            URI uri = new URI(link);
            String scheme = uri.getScheme();
            return scheme != null
                && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
