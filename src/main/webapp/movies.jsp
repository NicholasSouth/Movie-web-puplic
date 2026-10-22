<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Genres" %>
<%@ page import="com.movieweb.model.Tags" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
    // Expects 'catalog' (or data list providers) and 'filter' passed from your controller/servlet
    // If you are using your merged Movies model as the filter object:
    Movies filter = (Movies) request.getAttribute("filter");
    if (filter == null) 
   		filter = new Movies(); // Fallback default
    request.setAttribute("currentPage", "movies");
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport"
	          content="width=device-width, initial-scale=1.0">
	    <title>Movies - PhnetPhlyx</title>
	    <link rel="stylesheet"
	          href="${pageContext.request.contextPath}/styles/main.css">
	    <link rel="stylesheet"
	          href="${pageContext.request.contextPath}/styles/movies.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout1 movie-layout">
		    <!--Header-->
		    <%@ include file="components/header.jsp" %>
		
		    <!--Left Sidebar-->
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!--Main Body-->
		    <main class="MainBody">
		        <!-- Movies Header -->
		        <section class="movies-header">
		            <h1>Movies</h1>
		            <p>
		                Discover movies currently showing,
		                coming soon, and popular movies.
		            </p>
		        </section>
		
		        <!--Search & Filters Form-->
		        <form class="movie-filters" action="${pageContext.request.contextPath}/movies" method="get">
		            <!-- Search -->
		            <div class="movie-search">
		                <input
		                    type="search"
		                    id="movie-search"
		                    name="search"
		                    maxlength="200"
		                    value="<%= ViewUtils.h(filter.getSearch()) %>"
		                    placeholder="Search by movie, actor, director, or author...">
		                <button type="submit">
		                    Search
		                </button>
		            </div>
		
		            <!-- Filters -->
		            <div class="movie-filter-options">
		                <div class="movie-filter">
		                    <label for="genre">
						        Genre
						    </label>					
						    <select id="genre" name="genre">					
						        <option value="">All Genres</option>					
						        <%
						            List<Genres> genres = (List<Genres>) request.getAttribute("genres");						
						            if (genres != null) {
						                for (Genres genre : genres) {
						        %>						
								            <option
								                value="<%= genre.getGenre_id() %>"
								                <%= filter.getGenreId() != null
								                    && filter.getGenreId() == genre.getGenre_id()
								                    ? "selected"
								                    : "" %>>						
								                <%= genre.getGenre_name() %>						
								            </option>						
						        <%
						                }
						            }
						        %>						
						    </select>
		                </div>
		                <div class="movie-filter">
		                    <label for="tag">
						        Tags
						    </label>						
						    <select id="tag" name="tag">					
						        <option value="">All Tags</option>					
						        <%
						            List<Tags> tags = (List<Tags>) request.getAttribute("tags");						
						            if (tags != null) {
						                for (Tags tag : tags) {
						        %>						
								            <option
								                value="<%= tag.getTag_id() %>"
								                <%= filter.getTagId() != null
								                    && filter.getTagId() == tag.getTag_id()
								                    ? "selected"
								                    : "" %>>						
								                <%= tag.getTag_name() %>						
								            </option>						
						        <%
						                }
						            }
						        %>						
						    </select>

		                </div>
		                <div class="movie-filter">
		                    <label for="theater">
		                        Theater
		                    </label>
		                    <select id="theater" name="theater">
		                        <option value="">All Theaters</option>
		                        <%-- Iterate theaters list if provided by controller --%>
		                    </select>
		                </div>		      		                
		                <div class="movie-filter">
		                    <label for="age-rating">
							    Age Rating
							</label>
							<select id="age-rating" name="age-rating">
							    <option value=""
							        <%= (filter.getFilterAgeRating() == null
							                || filter.getFilterAgeRating().isEmpty())
							                ? "selected" : "" %>>
							        All Ratings
							    </option>
							    <option value="G"
							        <%= "G".equals(filter.getFilterAgeRating())
							                ? "selected" : "" %>>
							        G
							    </option>
							    <option value="PG"
							        <%= "PG".equals(filter.getFilterAgeRating())
							                ? "selected" : "" %>>
							        PG
							    </option>
							    <option value="PG-13"
							        <%= "PG-13".equals(filter.getFilterAgeRating())
							                ? "selected" : "" %>>
							        PG-13
							    </option>
							    <option value="R"
							        <%= "R".equals(filter.getFilterAgeRating())
							                ? "selected" : "" %>>
							        R
							    </option>
							    <option value="NC-17"
							        <%= "NC-17".equals(filter.getFilterAgeRating())
							                ? "selected" : "" %>>
							        NC-17
							    </option>
							</select>
		                </div>
		                <div class="movie-filter">
		                    <label for="rating">
		                        Minimum Rating
		                    </label>
		                    <input
		                        type="number"
		                        id="rating"
		                        name="rating"
		                        min="0"
		                        max="10"
		                        step="0.1"
		                        placeholder="Any rating"
		                        value="<%= filter.getMinRating() != null ? filter.getMinRating() : "" %>">
		                </div>
		                <div class="movie-filter">
		                    <label for="price">
		                        Max Ticket Price (VND)
		                    </label>
		                    <input
		                        type="number"
		                        id="price"
		                        name="price"
		                        min="0"
		                        step="1000"
		                        placeholder="Any price"
		                        value="<%= filter.getMaxPrice() != null ? String.valueOf(filter.getMaxPrice().longValue()) : "" %>">
		                </div>
		                <div class="movie-filter">
		                    <label for="status">
		                        Availability
		                    </label>
		                    <select id="status" name="status">
		                        <option value="all" <%= "all".equals(filter.getStatus()) ? "selected" : "" %>>All Movies</option>
		                        <option value="now-showing" <%= "now-showing".equals(filter.getStatus()) ? "selected" : "" %>>Now Showing</option>
		                        <option value="coming-soon" <%= "coming-soon".equals(filter.getStatus()) ? "selected" : "" %>>Coming Soon</option>
		                        <option value="popular" <%= "popular".equals(filter.getStatus()) ? "selected" : "" %>>Most Popular</option>
		                    </select>
		                </div>		                
		            </div>
		
		            <!-- Filter Buttons -->
		            <div class="movie-filter-actions">
		                <a class="clear-filter" href="${pageContext.request.contextPath}/movies">
		                    Clear Filters
		                </a>
		                <button type="submit" class="apply-filter">
		                    Apply Filters
		                </button>
		            </div>
		        </form>
		
		        <%-- If no filters are active, show the default categorized sections (Now Showing, Coming Soon, Popular) --%>
		        <% if (!filter.isHasFilters()) { %>
		            <!--Now Showing-->
		            <section class="movie-section">
		                <div class="section-header">
		                    <h2>Now Showing</h2>
		                    <span class="movie-count">
		                        <%
		                            List<Movies> nowShowingMovies = (List<Movies>) request.getAttribute("nowShowingMovies");
		                            out.print(nowShowingMovies != null ? nowShowingMovies.size() : 0);
		                        %>
		                        movies
		                    </span>
		                </div>
		                <%
		                    request.setAttribute("movieGridMovies", nowShowingMovies);
		                %>
		                <jsp:include page="/components/movie_grid.jsp" />
		            </section>
		
		            <!--Coming Soon-->
		            <section class="movie-section">
		                <div class="section-header">
		                    <h2>Coming Soon</h2>
		                    <span class="movie-count">
		                        <%
		                            List<Movies> comingSoonMovies = (List<Movies>) request.getAttribute("comingSoonMovies");
		                            out.print(comingSoonMovies != null ? comingSoonMovies.size() : 0);
		                        %>
		                        movies
		                    </span>
		                </div>
		                <%
		                    request.setAttribute("movieGridMovies", comingSoonMovies);
		                %>
		                <jsp:include page="/components/movie_grid.jsp" />
		            </section>
		
		            <!--Popular Movies-->
		            <section class="movie-section">
		                <div class="section-header">
		                    <h2>Popular Movies</h2>
		                    <span class="movie-count">
		                        <%
		                            List<Movies> popularMovies = (List<Movies>) request.getAttribute("popularMovies");
		                            out.print(popularMovies != null ? popularMovies.size() : 0);
		                        %>
		                        movies
		                    </span>
		                </div>
		                <%
		                    request.setAttribute("movieGridMovies", popularMovies);
		                %>
		                <jsp:include page="/components/movie_grid.jsp" />
		            </section>
		        <% } else { %>
		            <%-- If filters ARE active, display the filtered list via the grid component --%>
		            <section class="movie-section" id="catalog-results">
		                <div class="section-header">
		                    <h2>Search Results</h2>
		                    <span class="movie-count">
		                        <%
		                            List<Movies> filteredMovies = (List<Movies>) request.getAttribute("filteredMovies");
		                            out.print(filteredMovies != null ? filteredMovies.size() : 0);
		                        %>
		                        movies
		                    </span>
		                </div>
		                <%
		                    request.setAttribute("movieGridMovies", filteredMovies);
		                %>
		                <jsp:include page="/components/movie_grid.jsp" />
		            </section>
		        <% } %>
		    </main>
		    
		    <!--Right Sidebar-->
		    <%@ include file="components/right_sidebar.jsp" %>
		    
		    <!--Footer-->
		    <%@ include file="components/footer.jsp" %>
		</section>
	</body>
</html>