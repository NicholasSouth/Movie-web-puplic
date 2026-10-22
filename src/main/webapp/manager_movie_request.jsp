<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.service.MovieRequestService" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
	/* Always load the page through the servlet. */
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/manager-movie-request");
		return;
	}
	Users loggedInUser = (Users) session.getAttribute("user");
	if (loggedInUser == null || !"MANAGER".equalsIgnoreCase(loggedInUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	Map<String, String> form = (Map<String, String>) request.getAttribute("form");
	List<String> errors = (List<String>) request.getAttribute("errors");
	String flash = (String) request.getAttribute("flash");
	String csrfToken = (String) request.getAttribute("csrfToken");
	String managerEmail = (String) request.getAttribute("managerEmail");
	String contextPath = request.getContextPath();
	String[] ratings = { "G", "PG", "PG-13", "R", "NC-17" };
%>
<%!
	private static String val(Map<String, String> form, String key) {
		String v = form == null ? null : form.get(key);
		return v == null ? "" : v;
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Request a Movie - Manager</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_showtimes.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_movie_request.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout2">
			<jsp:include page="/components/header_manager.jsp" />
			<%@ include file="components/manager_navigation.jsp" %>
			<main class="ManagerMain">
				<div class="showtimes-container">
					<div class="showtimes-header">
						<div>
							<span class="showtimes-breadcrumb">Manage Theaters / Request a Movie</span>
							<h2>Request a Movie</h2>
							<p>
								Is a movie missing from PhnetPhlyx? Tell the administrator about it. Only the
								<strong>movie name</strong> is required; the more you fill in, the faster it can be added.
							</p>
						</div>
					</div>

					<%
						if (flash != null) {
					%>
							<div class="showtimes-message success-message" role="status"><%= ViewUtils.h(flash) %></div>
					<%
						}
					%>
					<%
						if (errors != null && !errors.isEmpty()) {
					%>
							<div class="showtimes-message error-message" role="alert">
								<%
									if (errors.size() == 1) {
								%>
										<%= ViewUtils.h(errors.get(0)) %>
								<%
									} else {
								%>
										Please fix the following:
										<ul class="mr-errors">
											<%
												for (String e : errors) {
											%>
													<li><%= ViewUtils.h(e) %></li>
											<%
												}
											%>
										</ul>
								<%
									}
								%>
							</div>
					<%
						}
					%>

					<section class="showtimes-section">
						<form class="showtimes-create-form" id="movieRequestForm" method="post"
							action="<%= contextPath %>/manager-movie-request"
							onsubmit="var b=this.querySelector('button[type=submit]'); if(b){b.disabled=true; b.textContent='Sending...';}">
							<input type="hidden" name="csrfToken" value="<%= ViewUtils.h(csrfToken) %>">

							<div class="showtimes-form-grid">
								<div class="showtimes-input-group mr-wide">
									<label for="name">
										Movie name 
										<span class="mr-required">*</span>
									</label>
									<input type="text" id="name" name="name" required maxlength="<%= MovieRequestService.NAME_MAX %>"
										value="<%= ViewUtils.h(val(form, "name")) %>" placeholder="e.g. Dune: Part Two" autofocus>
								</div>

								<div class="showtimes-input-group">
									<label for="releaseDate">
										Release date 
										<span class="mr-optional">optional</span>
									</label>
									<input type="date" id="releaseDate" name="releaseDate" value="<%= ViewUtils.h(val(form, "releaseDate")) %>">
								</div>
								<div class="showtimes-input-group">
									<label for="duration">
										Duration, minutes 
										<span class="mr-optional">optional</span>
									</label>
									<input type="number" id="duration" name="duration" min="1" max="<%= MovieRequestService.DURATION_MAX %>" step="1"
										value="<%= ViewUtils.h(val(form, "duration")) %>" placeholder="e.g. 166">
								</div>

								<div class="showtimes-input-group">
									<label for="ageRating">
										Age rating 
										<span class="mr-optional">optional</span>
									</label>
									<select id="ageRating" name="ageRating">
										<option value="">Not sure</option>
										<%
											for (String r : ratings) {
										%>
												<option value="<%= r %>" <%= r.equals(val(form, "ageRating")) ? "selected" : "" %>><%= r %></option>
										<%
											}
										%>
									</select>
								</div>
								<div class="showtimes-input-group">
									<label for="genres">
										Genres 
										<span class="mr-optional">optional</span>
									</label>
									<input type="text" id="genres" name="genres" maxlength="<%= MovieRequestService.LIST_MAX %>"
										value="<%= ViewUtils.h(val(form, "genres")) %>" placeholder="Separate with commas: Sci-Fi, Adventure">
								</div>

								<div class="showtimes-input-group">
									<label for="directors">
										Director(s) 
										<span class="mr-optional">optional</span>
									</label>
									<input type="text" id="directors" name="directors" maxlength="<%= MovieRequestService.LIST_MAX %>"
										value="<%= ViewUtils.h(val(form, "directors")) %>">
								</div>
								<div class="showtimes-input-group">
									<label for="authors">
										Author(s) / writer(s) 
										<span class="mr-optional">optional</span>
									</label>
									<input type="text" id="authors" name="authors" maxlength="<%= MovieRequestService.LIST_MAX %>"
										value="<%= ViewUtils.h(val(form, "authors")) %>">
								</div>

								<div class="showtimes-input-group mr-wide">
									<label for="actors">
										Main actors 
										<span class="mr-optional">optional</span>
									</label>
									<input type="text" id="actors" name="actors" maxlength="<%= MovieRequestService.ACTORS_MAX %>"
										value="<%= ViewUtils.h(val(form, "actors")) %>" placeholder="Separate with commas">
								</div>

								<div class="showtimes-input-group mr-wide">
									<label for="synopsis">
										Synopsis 
										<span class="mr-optional">optional</span>
									</label>
									<textarea id="synopsis" name="synopsis" rows="5" maxlength="<%= MovieRequestService.SYNOPSIS_MAX %>"
										placeholder="A short description of the story"><%= ViewUtils.h(val(form, "synopsis")) %></textarea>
								</div>

								<div class="showtimes-input-group">
									<label for="trailerLink">
										Trailer link 
										<span class="mr-optional">optional</span>
									</label>
									<input type="url" id="trailerLink" name="trailerLink" maxlength="<%= MovieRequestService.URL_MAX %>"
										value="<%= ViewUtils.h(val(form, "trailerLink")) %>" placeholder="https://...">
								</div>
								<div class="showtimes-input-group">
									<label for="posterLink">
										Poster link 
										<span class="mr-optional">optional</span>
									</label>
									<input type="url" id="posterLink" name="posterLink" maxlength="<%= MovieRequestService.URL_MAX %>"
										value="<%= ViewUtils.h(val(form, "posterLink")) %>" placeholder="https://...">
								</div>

								<div class="showtimes-input-group mr-wide">
									<label for="note">
										Note for the administrator 
										<span class="mr-optional">optional</span>
									</label>
									<textarea id="note" name="note" rows="3" maxlength="<%= MovieRequestService.NOTE_MAX %>"
										placeholder="Why this movie, deadlines, who asked for it..."><%= ViewUtils.h(val(form, "note")) %></textarea>
								</div>
							</div>

							<p class="mr-reply-note">
								<%
									if (managerEmail != null && !managerEmail.trim().isEmpty()) {
								%>
										The administrator will reply to <strong><%= ViewUtils.h(managerEmail) %></strong>.
								<%
									} else {
								%>
										Your account has no email address, so the administrator cannot reply by email.
										Add one in your profile first if you expect an answer.
								<%
									}
								%>
							</p>

							<div class="showtimes-form-actions">
								<a class="showtimes-button" href="<%= contextPath %>/main_manager.jsp">Cancel</a>
								<button type="submit" class="showtimes-button showtimes-button-primary">Send request</button>
							</div>
						</form>
					</section>
				</div>
			</main>
		</section>
	</body>
</html>