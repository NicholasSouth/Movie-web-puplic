<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Genres" %>
<%@ page import="com.movieweb.model.Tags" %>

<div class="movie-grid">
	<%
	    List<Movies> movies = (List<Movies>) request.getAttribute("movieGridMovies");
	    if (movies != null && !movies.isEmpty()) {
	        for (Movies movie : movies) {
	%>
	    <div class="movie-card">
	        <!-- Poster links to movie details -->
	        <a href="<%= request.getContextPath() %>/movie-details?id=<%= movie.getMovie_id() %>"
	           class="movie-poster-link">
	            <img
	                src="<%= request.getContextPath() %>/<%= movie.getPoster_path() %>"
	                alt="<%= movie.getMovie_name() %>">
	        </a>
	        <div class="movie-card-content">
	            <!-- Movie title links to movie details -->
	            <h3>
	                <a href="<%= request.getContextPath() %>/movie-details?id=<%= movie.getMovie_id() %>">
	                    <%= movie.getMovie_name() %>
	                </a>
	            </h3>
	
	            <!-- Clickable genres -->
	            <% if (movie.getGenres() != null && !movie.getGenres().isEmpty()) { %>
	                <div class="movie-genres">
	                    <% for (Genres genre : movie.getGenres()) { %>
	                        <a class="movie-genre"
	                           href="<%= request.getContextPath() %>/movies?genre=<%= genre.getGenre_id() %>">
	                            <%= genre.getGenre_name() %>
	                        </a>
	                    <% } %>
	                </div>
	            <% } %>
	
	            <!-- Clickable tags -->
	            <% if (movie.getTags() != null && !movie.getTags().isEmpty()) { %>
	                <div class="movie-tags">
	                    <% for (Tags tag : movie.getTags()) { %>
	                        <a class="movie-tag"
	                           href="<%= request.getContextPath() %>/movies?tag=<%= tag.getTag_id() %>">
	                            #<%= tag.getTag_name() %>
	                        </a>
	                    <% } %>
	                </div>
	            <% } %>
	        </div>
	    </div>
	<%
	        }
	    } 
	    else {
	%>
	    <p class="empty-movies">
	        No movies available.
	    </p>
	<%
	    }
	%>
</div>