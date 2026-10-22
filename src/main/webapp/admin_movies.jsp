<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminMovieRow" %>
<%@ page import="com.movieweb.model.Genres" %>
<%@ page import="com.movieweb.model.Tags" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	/* Always load the page through the servlet. */
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/admin-movies");
		return;
	}
	request.setAttribute("currentAdminPage", "movies");
	String ctx = request.getContextPath();
	List<AdminMovieRow> rows = (List<AdminMovieRow>) request.getAttribute("movieRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	Integer totalAttr = (Integer) request.getAttribute("totalMovies");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	int total = totalAttr == null ? 0 : totalAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	Map<String, String> form = (Map<String, String>) request.getAttribute("form");
	List<Genres> allGenres = (List<Genres>) request.getAttribute("allGenres");
	List<Tags> allTags = (List<Tags>) request.getAttribute("allTags");
	Set<Integer> selectedGenres = (Set<Integer>) request.getAttribute("selectedGenres");
	Set<Integer> selectedTags = (Set<Integer>) request.getAttribute("selectedTags");
	String formMode = (String) request.getAttribute("formMode");
	Integer formId = (Integer) request.getAttribute("formId");
	String formPosterPath = (String) request.getAttribute("formPosterPath");
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
%>
<%!
	private String fv(Map<String, String> form, String key) {
		return form == null ? "" : ViewUtils.h(form.get(key));
	}
	private String stateClass(String state) {
		if ("NOW SHOWING".equals(state)) return "admin-status-success";
		if ("COMING SOON".equals(state)) return "admin-status-scheduled";
		if ("ENDED".equals(state)) return "admin-status-pending";
		return ""; // HIDDEN: neutral grey
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Movies | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_theaters.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_movies.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Movies</h2>
						<p>A movie shows to customers while it is active and inside its availability dates. <%= total %> movie<%= total == 1 ? "" : "s" %> in this view.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-movies">
						<label for="movie-search">Search</label>
						<input id="movie-search" class="admin-input" type="text" name="q" maxlength="150"
							placeholder="Movie name" value="<%= ViewUtils.h(q) %>">
						<button type="submit" class="admin-btn">Search</button>
						<a class="admin-btn admin-btn-primary" href="<%= ctx %>/admin-movies?new=1&q=<%= qEnc %>">Add movie</a>
					</form>
				</div>

				<%
					if (flash != null) {
				%>
						<div class="admin-alert admin-alert-success"><%= ViewUtils.h(flash) %></div>
				<%
					}
				%>
				<%
					if (flashError != null) {
				%>
						<div class="admin-alert admin-alert-error"><%= ViewUtils.h(flashError) %></div>
				<%
					}
				%>
				<%
					if (loadError) {
				%>
						<div class="admin-alert admin-alert-error">The movie data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<%
					if (form != null) {
						boolean creating = !"edit".equals(formMode);
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= creating ? "Add movie" : "Edit movie" %></h3>
							</div>
							<div class="admin-panel-content">
								<form class="admin-form" method="post" action="<%= ctx %>/admin-movies" enctype="multipart/form-data">
									<input type="hidden" name="action" value="<%= creating ? "create" : "update" %>">
									<%
										if (!creating) {
									%>
											<input type="hidden" name="id" value="<%= formId == null ? 0 : formId %>">
									<%
										}
									%>
									<input type="hidden" name="q" value="<%= ViewUtils.h(q) %>">
									<input type="hidden" name="page" value="<%= pageNo %>">
									<div class="admin-form-row">
										<label>
											Movie name
											<input class="admin-input" type="text" name="name" maxlength="150" required value="<%= fv(form, "name") %>">
										</label>
										<label>
											Age rating (optional)
											<input class="admin-input" type="text" name="ageRating" maxlength="20" placeholder="For example T13" value="<%= fv(form, "ageRating") %>">
										</label>
										<label>
											Duration (minutes)
											<input class="admin-input" type="number" name="duration" min="1" max="600" required value="<%= fv(form, "duration") %>">
										</label>
									</div>
									<label>
										Description (optional)
										<textarea class="admin-input" name="description" maxlength="4000"><%= fv(form, "description") %></textarea>
									</label>
									<%
										if (!creating && formPosterPath != null && !formPosterPath.isEmpty()) {
									%>
											<div class="admin-movie-current">
												<img class="admin-movie-thumb" src="<%= ViewUtils.h(ViewUtils.asset(ctx, formPosterPath)) %>" alt="Current poster">
												<span class="admin-form-hint">Current poster. Choose a file below only if you want to replace it.</span>
											</div>
									<%
										}
									%>
									<label>
										<%= creating ? "Poster" : "New poster (optional)" %>
										<input class="admin-input" type="file" name="poster" accept=".jpg,.jpeg,.png,.webp" <%= creating ? "required" : "" %>>
									</label>
									<span class="admin-form-hint">JPG, PNG or WebP, up to 5 MB. A portrait poster (for example 2 : 3) looks best. New files are stored in pictures/movies.</span>
									<div class="admin-form-row">
										<label>
											Available from
											<input class="admin-input" type="date" name="availableFrom" required value="<%= fv(form, "availableFrom") %>">
										</label>
										<label>
											Available until (optional)
											<input class="admin-input" type="date" name="availableUntil" value="<%= fv(form, "availableUntil") %>">
										</label>
									</div>
									<span class="admin-form-hint">A movie dated in the future shows as "coming soon". Leave "until" empty if there is no end date.<%= creating ? "" : " Changing the duration or dates does not change showtimes that already exist." %></span>
									<label>
										Trailer link (optional)
										<input class="admin-input" type="url" name="trailerLink" maxlength="500"
											placeholder="https://www.youtube.com/watch?v=..." value="<%= fv(form, "trailerLink") %>">
									</label>

									<div>
										<div class="admin-form-hint">Genres</div>
										<div class="admin-check-group">
											<%
												if (allGenres == null || allGenres.isEmpty()) {
											%>
													<span class="admin-form-hint">No genres exist yet.</span>
											<%
												} else {
													for (Genres g : allGenres) {
											%>
														<label class="admin-check">
															<input type="checkbox" name="genreIds" value="<%= g.getGenre_id() %>"
																<%= selectedGenres != null && selectedGenres.contains(g.getGenre_id()) ? "checked" : "" %>>
															<%= ViewUtils.h(g.getGenre_name()) %>
														</label>
											<%
													}
												}
											%>
										</div>
									</div>
									<div>
										<div class="admin-form-hint">Tags</div>
										<div class="admin-check-group">
											<%
												if (allTags == null || allTags.isEmpty()) {
											%>
													<span class="admin-form-hint">No tags exist yet.</span>
											<%
												} else {
													for (Tags t : allTags) {
											%>
														<label class="admin-check">
															<input type="checkbox" name="tagIds" value="<%= t.getTag_id() %>"
																<%= selectedTags != null && selectedTags.contains(t.getTag_id()) ? "checked" : "" %>>
															<%= ViewUtils.h(t.getTag_name()) %>
														</label>
											<%
													}
												}
											%>
										</div>
									</div>

									<label>
										Actors (optional)
										<input class="admin-input" type="text" name="actors" placeholder="Separate names with commas" value="<%= fv(form, "actors") %>">
									</label>
									<label>
										Directors (optional)
										<input class="admin-input" type="text" name="directors" placeholder="Separate names with commas" value="<%= fv(form, "directors") %>">
									</label>
									<label>
										Authors (optional)
										<input class="admin-input" type="text" name="authors" placeholder="Separate names with commas" value="<%= fv(form, "authors") %>">
									</label>
									<span class="admin-form-hint">A name that already exists is reused; a new name is created automatically.</span>

									<label class="admin-check">
										<input type="checkbox" name="isActive" value="true" <%= "true".equals(form.get("isActive")) ? "checked" : "" %>> <%= creating ? "Show to customers immediately" : "Show to customers" %>
									</label>
									<div class="admin-inline-form">
										<button type="submit" class="admin-btn admin-btn-primary"><%= creating ? "Add movie" : "Save changes" %></button>
										<a class="admin-btn" href="<%= ctx %>/admin-movies?q=<%= qEnc %>&page=<%= pageNo %>">Cancel</a>
									</div>
								</form>
							</div>
						</section>
				<%
					}
				%>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No movies match this search.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Poster</th>
												<th>Movie</th>
												<th>Available</th>
												<th>State</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminMovieRow m : rows) {
													String state = m.getState();
											%>
													<tr>
														<td>
															<img class="admin-movie-thumb" src="<%= ViewUtils.h(ViewUtils.asset(ctx, m.getPosterPath())) %>" alt="<%= ViewUtils.h(m.getName()) %>">
														</td>
														<td>
															<div class="admin-movie-name"><%= ViewUtils.h(m.getName()) %></div>
															<span class="admin-muted">
																<%= m.getDurationMinute() %> min<%= ViewUtils.hasText(m.getAgeRating()) ? " · " + ViewUtils.h(m.getAgeRating()) : "" %>
															</span>
														</td>
														<td>
															<%= m.getAvailableFrom() == null ? "-" : df.format(m.getAvailableFrom()) %>
															<br>
															<span class="admin-muted">to <%= m.getAvailableUntil() == null ? "no end date" : df.format(m.getAvailableUntil()) %></span>
														</td>
														<td>
															<span class="admin-status <%= stateClass(state) %>"><%= state %></span>
														</td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-movies?edit=<%= m.getMovieId() %>&q=<%= qEnc %>&page=<%= pageNo %>">Edit</a>
														</td>
													</tr>
											<%
												}
											%>
										</tbody>
									</table>
								</div>
								<div class="admin-pagination">
									<%
										if (pageNo > 1) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-movies?q=<%= qEnc %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-movies?q=<%= qEnc %>&page=<%= pageNo + 1 %>">Next</a>
									<%
										}
									%>
								</div>
						<%
							}
						%>
					</div>
				</section>
			</main>
		</section>
	</body>
</html>
