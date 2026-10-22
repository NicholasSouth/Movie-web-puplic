<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Genres" %>
<%@ page import="com.movieweb.model.Tags" %>
<%@ page import="com.movieweb.model.Actors" %>
<%@ page import="com.movieweb.model.Directors" %>
<%@ page import="com.movieweb.model.Authors" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Showtimes" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.Ratings" %>
<%@ page import="com.movieweb.model.Comments" %>
<%
	request.setAttribute("currentPage", "movies");
	Movies movie = (Movies) request.getAttribute("movie");
	if (movie == null) {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, "Movie not found.");
		return;
	}
	List<Genres> genres = movie.getGenres();
	List<Tags> tags = movie.getTags();
	String contextPath = request.getContextPath();
	String posterPath = movie.getPoster_path();
	String trailerPath = movie.getTrailer_path();
	String trailerLink = movie.getTrailer_link();
	List<Actors> actors = (List<Actors>) request.getAttribute("actors");
	if (actors == null) {
		actors = new java.util.ArrayList<>();
	}
	List<Directors> directors = (List<Directors>) request.getAttribute("directors");
	if (directors == null) {
		directors = new java.util.ArrayList<>();
	}
	List<Authors> authors = (List<Authors>) request.getAttribute("authors");
	if (authors == null) {
		authors = new java.util.ArrayList<>();
	}
	Map<Showtimes, Theaters> movieShowtimes = (Map<Showtimes, Theaters>) request.getAttribute("movieShowtimes");
	Users loggedInUser = (Users) request.getAttribute("loggedInUser");
	Ratings userRating = (Ratings) request.getAttribute("userRating");
	List<Comments> comments = (List<Comments>) request.getAttribute("comments");
	if (comments == null) {
		comments = new java.util.ArrayList<>();
	}
	Map<Integer, Users> commentUsers = (Map<Integer, Users>) request.getAttribute("commentUsers");
	if (commentUsers == null) {
		commentUsers = new java.util.HashMap<>();
	}
	Boolean favouriteAttribute = (Boolean) request.getAttribute("isFavourite");
	boolean isFavourite = favouriteAttribute != null && favouriteAttribute;
	String csrfToken = (String) request.getAttribute("csrfToken");
	String favouriteMessage = (String) request.getAttribute("favouriteMessage");
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	String releaseDate = movie.getAvailable_from() != null
			? dateFormat.format(movie.getAvailable_from())
			: "Not available.";
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title><%= movie.getMovie_name() %> | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= contextPath %>/styles/main.css">
		<link rel="stylesheet" href="<%= contextPath %>/styles/movie_details.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout1">
			<%@ include file="components/header.jsp" %>
			<%@ include file="components/left_sidebar.jsp" %>
			<main class="MainBody">
				<!-- Movie Information -->
				<section class="movie-info">
					<div class="movie-poster">
						<%
							if (posterPath != null && !posterPath.isEmpty()) {
						%>
								<img src="<%= contextPath %>/<%= posterPath %>" alt="<%= movie.getMovie_name() %>">
						<%
							} 
							else {
						%>
								<div class="poster-placeholder">
									Poster unavailable.
								</div>
						<%
							}
						%>
					</div>
					<%
						if (favouriteMessage != null && !favouriteMessage.isEmpty()) {
					%>
							<div class="favourite-message">
								<%= favouriteMessage %>
							</div>
					<%
						}
					%>
					<div class="movie-information">
						<h1>
							<%= movie.getMovie_name() %>
						</h1>
						<!-- Rating -->
						<div class="movie-rating">
							<img src="<%= contextPath %>/pictures/assessments/star.png" alt="Rating" class="rating-star">
							<span>
								<%= movie.getAvg_rating() %> / 10
							</span>
						</div>
						<!-- Genres -->
						<div class="movie-meta-group">
							<strong>Genres:</strong>
							<div class="movie-tags">
								<%
									if (genres != null && !genres.isEmpty()) {
										for (Genres genre : genres) {
								%>
											<a href="<%= contextPath %>/movies?genre=<%= genre.getGenre_id() %>" class="genre-tag">
												<%= genre.getGenre_name() %>
											</a>
								<%
										}
									} 
									else {
								%>
										<span class="empty-meta">
											No genres available.
										</span>
								<%
									}
								%>
							</div>
						</div>
						<!-- Tags -->
						<div class="movie-meta-group">
							<strong>Tags:</strong>
							<div class="movie-tags">
								<%
									if (tags != null && !tags.isEmpty()) {
										for (Tags tag : tags) {
								%>
											<a href="<%= contextPath %>/movies?tag=<%= tag.getTag_id() %>" class="movie-tag">
												#<%= tag.getTag_name() %>
											</a>
								<%
										}
									} 
									else {
								%>
										<span class="empty-meta">
											No tags available.
										</span>
								<%
									}
								%>
							</div>
						</div>
						<!-- Directors -->
						<div class="movie-meta-group">
							<strong>Directors:</strong>
							<%
								if (directors.isEmpty()) {
							%>
									<span class="empty-meta">Not available.</span>
							<%
								} 
								else {
							%>
									<div class="movie-people">
										<%
											for (Directors director : directors) {
										%>
												<span class="person-name">
													<%= director.getDirector_name() %>
												</span>
										<%
											}
										%>
									</div>
							<%
								}
							%>
						</div>
						<!-- Authors -->
						<div class="movie-meta-group">
							<strong>Authors:</strong>
							<%
								if (authors.isEmpty()) {
							%>
									<span class="empty-meta">Not available.</span>
							<%
								} 
								else {
							%>
									<div class="movie-people">
										<%
											for (Authors author : authors) {
										%>
												<span class="person-name">
													<%= author.getAuthor_name() %>
												</span>
										<%
											}
										%>
									</div>
							<%
								}
							%>
						</div>
						<!-- Actors -->
						<div class="movie-meta-group">
							<strong>Actors:</strong>
							<%
								if (actors.isEmpty()) {
							%>
									<span class="empty-meta">Not available.</span>
							<%
								} 
								else {
							%>
									<div class="movie-people">
										<%
											for (Actors actor : actors) {
										%>
												<span class="person-name">
													<%= actor.getActor_name() %>
												</span>
										<%
											}
										%>
									</div>
							<%
								}
							%>
						</div>
						<p>
							<strong>Duration:</strong>
							<%= movie.getDuration_minute() %> minutes
						</p>
						<p>
							<strong>Age Rating:</strong>
							<%= movie.getAge_rating() != null ? movie.getAge_rating() : "Not available." %>
						</p>
						<p>
							<strong>Release Date:</strong>
							<%= releaseDate %>
						</p>
						<!-- Actions -->
						<div class="movie-actions">
							<%
								if (loggedInUser != null) {
							%>
									<form action="<%= contextPath %>/favorite" method="post" class="favorite-form">
										<input type="hidden" name="movieId" value="<%= movie.getMovie_id() %>">
										<input type="hidden" name="action" value="<%= isFavourite ? "remove" : "add" %>">
										<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
										<button type="submit" class="favorite-button<%= isFavourite ? " active" : "" %>">
											<img src="<%= contextPath %>/pictures/assessments/heart.png" alt="Favorite" class="favorite-icon">
											<span>
												<%= isFavourite ? "Remove from Favorites" : "Add to Favorites" %>
											</span>
										</button>
									</form>
							<%
								} 
								else {
							%>
									<button type="button" class="favorite-button" onclick="window.location.href='<%= contextPath %>/log_in.jsp'">
										<img src="<%= contextPath %>/pictures/assessments/heart.png" alt="Login" class="favorite-icon">
										<span>Login to Add Favorites</span>
									</button>
							<%
								}
							%>
							<form action="<%= contextPath %>/booking-showtimes" method="get">
								<input type="hidden" name="movie_id" value="<%= movie.getMovie_id() %>">
								<button type="submit" class="booking-button">
									Book Ticket
								</button>
							</form>
						</div>
					</div>
				</section>
				<!-- Description -->
				<section class="movie-section">
					<h2>Description</h2>
					<p>
						<%= movie.getDescription() != null ? movie.getDescription() : "No description available." %>
					</p>
				</section>
				<!-- Trailer -->
				<section class="movie-section">
					<h2>Trailer</h2>
					<div class="trailer-container">
						<%
							if (trailerPath != null && !trailerPath.isEmpty()) {
						%>
								<div class="trailer-placeholder">
									<%
										if (trailerLink != null && !trailerLink.isEmpty()) {
									%>
											<a href="<%= trailerLink %>" class="trailer-link" target="_blank" rel="noopener noreferrer">
												<img src="<%= contextPath %>/<%= trailerPath %>" alt="<%= movie.getMovie_name() %> Trailer" class="trailer-image">
											</a>
									<%
										} 
										else {
									%>
											<img src="<%= contextPath %>/<%= trailerPath %>" alt="<%= movie.getMovie_name() %> Trailer" class="trailer-image">
									<%
										}
									%>
								</div>
						<%
							} 
							else {
						%>
								<p class="empty-meta">
									Trailer unavailable.
								</p>
						<%
							}
						%>
					</div>
				</section>
				<!-- Showtimes -->
				<section class="movie-section">
					<h2>Available Showtimes</h2>
					<%
						SimpleDateFormat showtimeDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						if (movieShowtimes != null && !movieShowtimes.isEmpty()) {
							for (Map.Entry<Showtimes, Theaters> entry : movieShowtimes.entrySet()) {
								Showtimes showtime = entry.getKey();
								Theaters theater = entry.getValue();
								String showtimeDate = showtime.getStart_at() != null
										? showtimeDateFormat.format(showtime.getStart_at())
										: "Date unavailable.";
								String selectedDate = showtime.getStart_at() != null
										? new SimpleDateFormat("yyyy-MM-dd").format(showtime.getStart_at())
										: "";
					%>
								<a href="<%= contextPath %>/theater-details?id=<%= theater.getTheater_id() %>&date=<%= selectedDate %>#movie-schedule" class="showtime-card">
									<div class="showtime-theater-image">
										<%
											if (theater.getTheater_image_path() != null && !theater.getTheater_image_path().isEmpty()) {
										%>
												<img src="<%= contextPath %>/<%= theater.getTheater_image_path() %>" alt="<%= theater.getTheater_name() %>">
										<%
											} 
											else {
										%>
												<div class="showtime-theater-placeholder">
													No Image
												</div>
										<%
											}
										%>
									</div>
									<div class="showtime-theater-info">
										<h3>
											<%= theater.getTheater_name() %>
										</h3>
										<p class="showtime-theater-address">
											<%= theater.getTheater_address() != null ? theater.getTheater_address() : "Address unavailable." %>
										</p>
										<p class="showtime-start">
											<strong>Starts:</strong>
											<%= showtimeDate %>
										</p>
									</div>
								</a>
					<%
							}
						} 
						else {
					%>
							<p class="empty-meta">
								No upcoming showtimes are available.
							</p>
					<%
						}
					%>
				</section>
				<!-- Ratings -->
				<section class="movie-section">
					<h2>Ratings</h2>
					<div class="movie-rating-summary">
						<img src="<%= contextPath %>/pictures/assessments/star.png" alt="Rating" class="rating-star">
						<span>
							<%= movie.getAvg_rating() %> / 10
						</span>
					</div>
					<%
						if (loggedInUser != null) {
					%>
							<div class="user-rating-section">
								<h3>Your Rating</h3>
								<%
									if (userRating != null) {
								%>
										<p>
											You rated this movie
											<strong>
												<%= userRating.getRating() %> / 10
											</strong>
										</p>
								<%
									} 
									else {
								%>
										<p>
											You have not rated this movie yet.
										</p>
								<%
									}
								%>
								<form action="<%= contextPath %>/rating" method="post" class="rating-form">
									<input type="hidden" name="movieId" value="<%= movie.getMovie_id() %>">
									<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
									<label for="rating">
										<%= userRating != null ? "Change your rating:" : "Give this movie a rating:" %>
									</label>
									<select id="rating" name="rating" required>
										<option value="">Select rating</option>
										<%
											for (int rating = 1; rating <= 10; rating++) {
										%>
												<option value="<%= rating %>" <%= userRating != null && userRating.getRating() == rating ? "selected" : "" %>>
													<%= rating %> / 10
												</option>
										<%
											}
										%>
									</select>
									<button type="submit">
										<%= userRating != null ? "Update Rating" : "Submit Rating" %>
									</button>
								</form>
							</div>
					<%
						} 
						else {
					%>
							<p>
								<a href="<%= contextPath %>/log_in.jsp">Log in</a> to rate this movie.
							</p>
					<%
						}
					%>
				</section>
				<!-- Comments -->
				<section class="movie-section">
					<h2>Comments</h2>
					<%
						if (loggedInUser != null) {
					%>
							<form action="<%= contextPath %>/comment" method="post" class="comment-form">
								<input type="hidden" name="movieId" value="<%= movie.getMovie_id() %>">
								<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
								<textarea name="commentText" rows="4" placeholder="Write a comment..." required></textarea>
								<button type="submit">Post Comment</button>
							</form>
					<%
						} 
						else {
					%>
							<p>
								<a href="<%= contextPath %>/log_in.jsp">Log in</a> to leave a comment.
							</p>
					<%
						}
					%>
					<div class="comments-list">
						<%
							if (comments.isEmpty()) {
						%>
								<p class="empty-meta">No comments yet.</p>
						<%
							} 
							else {
								for (Comments comment : comments) {
									Users commentUser = commentUsers.get(comment.getUserId());
									String commentUsername = commentUser != null ? commentUser.getUsername() : "Unknown User";
									String commentAvatar = commentUser != null ? commentUser.getAvtPath() : null;
						%>
									<div class="comment">
										<div class="comment-header">
											<div class="comment-user">
												<a href="<%= contextPath %>/user-profile?user_id=<%= comment.getUserId() %>" class="comment-user-link">
													<%
														if (commentAvatar != null && !commentAvatar.isEmpty()) {
													%>
															<img src="<%= contextPath %>/<%= commentAvatar %>" alt="<%= commentUsername %>" class="comment-avatar">
													<%
														} 
														else {
													%>
															<div class="comment-avatar comment-avatar-placeholder">
																<%= commentUsername != null && !commentUsername.isEmpty() ? commentUsername.substring(0, 1).toUpperCase() : "?" %>
															</div>
													<%
														}
													%>
													<strong><%= commentUsername %></strong>
												</a>
											</div>
											<span class="comment-date">
												<%= new SimpleDateFormat("MMM dd, yyyy HH:mm").format(comment.getCreatedAt()) %>
											</span>
										</div>
										<p class="comment-text">
											<%= comment.getCommentText() %>
										</p>
										<%
											if (loggedInUser != null) {
										%>
												<form action="<%= contextPath %>/report" method="post" class="report-form">
													<input type="hidden" name="commentId" value="<%= comment.getCommentId() %>">
												<input type="hidden" name="csrfToken" value="<%= csrfToken %>">
													<input type="hidden" name="movieId" value="<%= movie.getMovie_id() %>">
													<button type="submit" class="report-button">Report</button>
												</form>
										<%
											}
										%>
									</div>
						<%
								}
							}
						%>
					</div>
				</section>
			</main>
			<%@ include file="components/right_sidebar.jsp" %>
			<%@ include file="components/footer.jsp" %>
		</section>
	</body>
</html>