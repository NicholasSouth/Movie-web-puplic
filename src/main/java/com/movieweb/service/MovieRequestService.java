package com.movieweb.service;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.movieweb.model.MovieRequestData;

/**
 * Checks and cleans the "Request a movie" form. No servlet and no database here,
 * so it can be tested on its own. Only the movie name is required.
 */
public class MovieRequestService {

    public static final String[] FIELDS = {
        "name", "releaseDate", "duration", "ageRating", "genres", "directors",
        "authors", "actors", "synopsis", "trailerLink", "posterLink", "note"
    };
    public static final int NAME_MAX = 100;          // Movies.movie_name is varchar(100)
    public static final int LIST_MAX = 200;
    public static final int ACTORS_MAX = 300;
    public static final int SYNOPSIS_MAX = 2000;
    public static final int URL_MAX = 500;
    public static final int NOTE_MAX = 500;
    public static final int DURATION_MAX = 600;
    private static final Set<String> RATINGS = new HashSet<>(Arrays.asList("G", "PG", "PG-13", "R", "NC-17"));

    public static class Result {
        public final List<String> errors = new ArrayList<>();
        /** The cleaned text of every field, to put back into the form after an error. */
        public final Map<String, String> form = new LinkedHashMap<>();
        public MovieRequestData data;               // only set when there are no errors

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    public Result validate(Map<String, String> raw) {
        Result r = new Result();

        String name = clean(raw.get("name"), false).replaceAll("\\s+", " ");
        String date = clean(raw.get("releaseDate"), false);
        String duration = clean(raw.get("duration"), false);
        String rating = clean(raw.get("ageRating"), false);
        String genres = clean(raw.get("genres"), false);
        String directors = clean(raw.get("directors"), false);
        String authors = clean(raw.get("authors"), false);
        String actors = clean(raw.get("actors"), false);
        String synopsis = clean(raw.get("synopsis"), true);
        String trailer = clean(raw.get("trailerLink"), false);
        String poster = clean(raw.get("posterLink"), false);
        String note = clean(raw.get("note"), true);

        r.form.put("name", name);
        r.form.put("releaseDate", date);
        r.form.put("duration", duration);
        r.form.put("ageRating", rating);
        r.form.put("genres", genres);
        r.form.put("directors", directors);
        r.form.put("authors", authors);
        r.form.put("actors", actors);
        r.form.put("synopsis", synopsis);
        r.form.put("trailerLink", trailer);
        r.form.put("posterLink", poster);
        r.form.put("note", note);

        if (name.isEmpty()) {
            r.errors.add("The movie name is required.");
        }
        tooLong(r, "The movie name", name, NAME_MAX);
        tooLong(r, "Genres", genres, LIST_MAX);
        tooLong(r, "Directors", directors, LIST_MAX);
        tooLong(r, "Authors", authors, LIST_MAX);
        tooLong(r, "Actors", actors, ACTORS_MAX);
        tooLong(r, "The synopsis", synopsis, SYNOPSIS_MAX);
        tooLong(r, "The note", note, NOTE_MAX);

        LocalDate releaseDate = null;
        if (!date.isEmpty()) {
            try {
                releaseDate = LocalDate.parse(date);
                if (releaseDate.getYear() < 1888 || releaseDate.getYear() > 2100) {
                    r.errors.add("The release date is out of range.");
                    releaseDate = null;
                }
            } catch (RuntimeException e) {
                r.errors.add("The release date is not a valid date.");
            }
        }

        Integer minutes = null;
        if (!duration.isEmpty()) {
            try {
                minutes = Integer.valueOf(duration);
                if (minutes < 1 || minutes > DURATION_MAX) {
                    r.errors.add("The duration must be between 1 and " + DURATION_MAX + " minutes.");
                    minutes = null;
                }
            } catch (NumberFormatException e) {
                r.errors.add("The duration must be a whole number of minutes.");
            }
        }

        if (!rating.isEmpty() && !RATINGS.contains(rating)) {
            r.errors.add("The age rating must be G, PG, PG-13, R or NC-17.");
        }
        checkUrl(r, "The trailer link", trailer);
        checkUrl(r, "The poster link", poster);

        if (r.errors.isEmpty()) {
            MovieRequestData d = new MovieRequestData();
            d.setName(name);
            d.setReleaseDate(releaseDate);
            d.setDurationMinutes(minutes);
            d.setAgeRating(blankToNull(rating));
            d.setGenres(blankToNull(genres));
            d.setDirectors(blankToNull(directors));
            d.setAuthors(blankToNull(authors));
            d.setActors(blankToNull(actors));
            d.setSynopsis(blankToNull(synopsis));
            d.setTrailerLink(blankToNull(trailer));
            d.setPosterLink(blankToNull(poster));
            d.setNote(blankToNull(note));
            r.data = d;
        }
        return r;
    }

    // ------------------------------------------------------------- helpers

    /** Trims, unifies line breaks and drops control characters (they could break an e-mail header). */
    static String clean(String value, boolean multiline) {
        if (value == null) {
            return "";
        }
        String s = value.replace("\r\n", "\n").replace('\r', '\n');
        s = s.replace('\t', ' ');
        s = s.replaceAll("[\\p{Cntrl}&&[^\\n]]", "");
        if (!multiline) {
            s = s.replace('\n', ' ');
        }
        return s.trim();
    }

    private void tooLong(Result r, String label, String value, int max) {
        if (value.length() > max) {
            r.errors.add(label + " is too long (max " + max + " characters).");
        }
    }

    private void checkUrl(Result r, String label, String value) {
        if (value.isEmpty()) {
            return;
        }
        if (value.length() > URL_MAX) {
            r.errors.add(label + " is too long (max " + URL_MAX + " characters).");
            return;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            boolean web = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!web || uri.getHost() == null || value.indexOf(' ') >= 0) {
                r.errors.add(label + " must be a full web address starting with http:// or https://.");
            }
        } catch (Exception e) {
            r.errors.add(label + " must be a full web address starting with http:// or https://.");
        }
    }

    private String blankToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
