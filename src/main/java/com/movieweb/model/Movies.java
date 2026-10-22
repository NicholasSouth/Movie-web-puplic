package com.movieweb.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class Movies {
	//How much movies should be shown at once
	public static final int PAGE_SIZE = 20;
	
	//Basic SQL table variables
	private int movie_id;
	private String movie_name;
	private String description;
	private String age_rating;
	private double avg_rating;
	private int duration_minute;
	private Timestamp available_from;
	private Timestamp available_until;
	private String poster_path;
	private String trailer_path;
	private String trailer_link;
	private boolean isActive;
	private Timestamp deleted_at;
	
	//For filtering and paging
	private String search = "";
    private Integer genreId;
    private Integer tagId;
    private Integer theaterId;
    private Double maxPrice;
    private String filterAgeRating = "";
    private Double minRating;
    private String status = "all";
    private int page = 1;
	   
    // Related movie information
    private List<Genres> genres = new ArrayList<>();
    private List<Tags> tags = new ArrayList<>();
    
    //
	public Movies(){
	}
	//Adding the parameter into movies.jsp
	/** Parses request parameters directly into filter fields. */
    public static Movies fromParameters(Map<String, String[]> parameters) {
        Movies filter = new Movies();      
        filter.search = value(parameters, "search");
        if (filter.search.length() > 200) throw new IllegalArgumentException("Maximum 200 characters search.");       
        filter.genreId = optionalId(value(parameters, "genre"), "Genre");
        filter.tagId = optionalId(value(parameters, "tag"), "Tag");
        filter.theaterId = optionalId(value(parameters, "theater"), "Theater");
        
        // Ticket Price filter
        String priceText = value(parameters, "price");
        if (!priceText.isEmpty()) {
            try {
                filter.maxPrice = Double.valueOf(priceText);
                if (!Double.isFinite(filter.maxPrice) || filter.maxPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Price invalid.");
            }
        }        
        filter.filterAgeRating = value(parameters, "age-rating");
        if (filter.filterAgeRating.length() > 100 || filter.filterAgeRating.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Age rating invalid.");
        String ratingText = value(parameters, "rating");
        if (!ratingText.isEmpty()) {
            try {
                if (!ratingText.matches("\\d+(\\.\\d{1,2})?")) throw new NumberFormatException();
                filter.minRating = Double.valueOf(ratingText);
                if (!Double.isFinite(filter.minRating) || filter.minRating < 0 || filter.minRating > 10) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Rating must be in range from 0 to 10.");
            }
        }        
        filter.status = value(parameters, "status");
        if (filter.status.isEmpty()) filter.status = "all";
        if (!Set.of("all", "now-showing", "coming-soon", "popular").contains(filter.status))
            throw new IllegalArgumentException("Movie status invalid.");
        Integer pageNum = optionalId(value(parameters, "page"), "Page");
        if (pageNum != null && pageNum > 100000) throw new IllegalArgumentException("Too large page number.");
        filter.page = pageNum == null ? 1 : pageNum;
        return filter;
    }
    public static String value(Map<String, String[]> parameters, String key) {
        String[] values = parameters.get(key);
        if (values == null || values.length == 0) return "";
        if (values.length != 1) throw new IllegalArgumentException("Variable " + key + " can only appears once.");
        return values[0] == null ? "" : values[0].trim();
    }   
    private static Integer optionalId(String text, String label) {
        if (text == null || text.isEmpty()) return null;
        try {
            if (!text.matches("[0-9]{1,10}")) throw new NumberFormatException();
            int id = Integer.parseInt(text);
            if (id < 1) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be valid positive integer.");
        }
    }
		
	//For fetching and adjusting movie_details
	public int getMovie_id() {
        return movie_id;
    }
    public void setMovie_id(int movie_id) {
        this.movie_id = movie_id;
    }
    public String getMovie_name() {
        return movie_name;
    }
    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getAge_rating() {
        return age_rating;
    }
    public void setAge_rating(String age_rating) {
        this.age_rating = age_rating;
    }
    public double getAvg_rating() {
        return avg_rating;
    }
    public void setAvg_rating(double avg_rating) {
        this.avg_rating = avg_rating;
    }
    public int getDuration_minute() {
        return duration_minute;
    }
    public void setDuration_minute(int duration_minute) {
        this.duration_minute = duration_minute;
    }
    public Timestamp getAvailable_from() {
        return available_from;
    }
    public void setAvailable_from(Timestamp available_from) {
        this.available_from = available_from;
    }
    public Timestamp getAvailable_until() {
        return available_until;
    }
    public void setAvailable_until(Timestamp available_until) {
        this.available_until = available_until;
    }
    public String getPoster_path() {
        return poster_path;
    }
    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }
    public String getTrailer_path() {
        return trailer_path;
    }
    public void setTrailer_path(String trailer_path) {
        this.trailer_path = trailer_path;
    }
    public String getTrailer_link() {
        return trailer_link;
    }
    public void setTrailer_link(String trailer_link) {
        this.trailer_link = trailer_link;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public Timestamp getDeleted_at() {
        return deleted_at; 
    }
    public void setDeleted_at(Timestamp deleted_at) {
        this.deleted_at = deleted_at;
    }

    //For fetching filter details
    public String getSearch() { return search; }
    public Integer getGenreId() { return genreId; }
    public Integer getTagId() { return tagId; }
    public Integer getTheaterId() { return theaterId; }
    public Double getMaxPrice() { return maxPrice; }
    public String getFilterAgeRating() { return filterAgeRating; }
    public Double getMinRating() { return minRating; }
    public String getStatus() { return status; }
    public int getPage() { return page; }
    public int getOffset() { return (page - 1) * PAGE_SIZE; }
    public boolean isHasFilters() {
        return (search != null && !search.isEmpty())
                || genreId != null
                || tagId != null
                || theaterId != null
                || maxPrice != null
                || (filterAgeRating != null
                    && !filterAgeRating.isEmpty())
                || minRating != null
                || (status != null
                    && !"all".equals(status));
    }
    
 // For displaying movie genres and tags
    public List<Genres> getGenres() {
        return genres;
    }
    public void setGenres(List<Genres> genres) {
        this.genres = genres;
    }
    public List<Tags> getTags() {
        return tags;
    }
    public void setTags(List<Tags> tags) {
        this.tags = tags;
    }
}
