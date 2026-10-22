<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.Rooms" %>
<%@ page import="com.movieweb.model.Movies" %>
<%@ page import="com.movieweb.model.Showtimes" %>
<%
	request.setAttribute("currentManagerPage", "showtimes");
	List<Theaters> theaters = (List<Theaters>) request.getAttribute("theaters");
	Integer currentTheaterId = (Integer) request.getAttribute("currentTheaterId");
	List<Rooms> rooms = (List<Rooms>) request.getAttribute("rooms");
	List<Movies> movies = (List<Movies>) request.getAttribute("movies");
	List<Showtimes> showtimes = (List<Showtimes>) request.getAttribute("showtimes");
	String error = (String) request.getAttribute("errorMessage");
	String success = (String) request.getAttribute("successMessage");
	String selectedDate = (String) request.getAttribute("selectedDate");
	if (selectedDate == null || selectedDate.trim().isEmpty()) {
		selectedDate = LocalDate.now().toString();
	}
	String currentTheaterName = null;
	if (theaters != null && currentTheaterId != null) {
		for (Theaters theater : theaters) {
			if (theater.getTheater_id() == currentTheaterId.intValue()) {
				currentTheaterName = theater.getTheater_name();
				break;
			}
		}
	}
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Showtimes - PhnetPhlyx</title>
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/manager_showtimes.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout2">
			<!-- Manager Header -->
			<jsp:include page="/components/header_manager.jsp" />
			<!-- Manager Navigation -->
			<%@ include file="components/manager_navigation.jsp" %>
			<!-- Main Content -->
			<main class="ManagerMain">
				<div class="showtimes-container">
					<!-- Page Header -->
					<div class="showtimes-header">
						<div>
							<span class="showtimes-breadcrumb">
								Manage Theaters / Showtimes Management
							</span>
							<h2>Manage Showtimes</h2>
							<p>
								Schedule movies and manage showtimes 
								for your currently selected theater.
							</p>
							<%
								if (currentTheaterId != null) {
							%>
									<div class="showtimes-current-theater">
										Current Theater: 
										<strong>
											<%= currentTheaterName == null
													? "Theater ID " + currentTheaterId
													: currentTheaterName %>
										</strong>
									</div>
							<%
								}
							%>
						</div>
					</div>
					<!-- Error Message -->
					<%
						if (error != null && !error.trim().isEmpty()) {
					%>
							<div class="showtimes-message error-message">
								<%= error %>
							</div>
					<%
						}
					%>
					<!-- Success Message -->
					<%
						if (success != null && !success.trim().isEmpty()) {
					%>
							<div class="showtimes-message success-message">
								<%= success %>
							</div>
					<%
						}
					%>
					<%
						if (currentTheaterId == null) {
					%>
							<div class="showtimes-empty-state">
								<h3>No Theater Available</h3>
								<p>
									You do not currently have an active assigned 
									theater. Please contact an administrator.
								</p>
							</div>
					<%
						} 
						else {
					%>				
							<!-- Create Showtime -->
							<section class="showtimes-section">
								<div class="showtimes-section-heading">
									<div>
										<h3 class="showtimes-section-title">
											Add Showtime
										</h3>
										<p class="showtimes-section-description">
											Schedule a movie in a room belonging 
											to the currently selected theater.
										</p>
									</div>
								</div>
								<form class="showtimes-create-form" action="${pageContext.request.contextPath}/manager-showtimes" method="post">
									<input type="hidden" name="action" value="create">
									<input type="hidden" name="date" value="<%= selectedDate %>">
									<div class="showtimes-form-grid">
										<!-- Room -->
										<div class="showtimes-input-group">
											<label for="roomId">Room</label>
											<select id="roomId" name="roomId" required>
												<option value="">
													Select a room
												</option>
												<%
													if (rooms != null) {
														for (Rooms room : rooms) {
												%>
															<option value="<%= room.getRoom_id() %>">
																<%= room.getRoom_name() %>
															</option>
												<%
														}
													}
												%>
											</select>
										</div>
										<!-- Movie -->
										<div class="showtimes-input-group">
											<label for="movieId">Movie</label>
											<select id="movieId" name="movieId" required>
												<option value="">
													Select a movie
												</option>
												<%
													if (movies != null) {
														for (Movies movie : movies) {
												%>
															<option 
																value="<%= movie.getMovie_id() %>" 
																data-duration="<%= movie.getDuration_minute() %>">
															    <%= movie.getMovie_name() %>
															    <%= movie.getAvailable_from() == null ? "" :
															        " (from " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(movie.getAvailable_from()) + ")" %>
															</option>
												<%
														}
													}
												%>
											</select>
										</div>
										<!-- Start Time -->
										<div class="showtimes-input-group">
											<label for="startAt">Start Time</label>
											<input 
												type="datetime-local" 
												id="startAt" 
												name="startAt" 
												step="60" 
												required>
										</div>
										<div class="showtimes-input-group"> 
											<label for="endAt">End Time</label> 
											<input 
												type="datetime-local" 
												id="endAt" 
												name="endAt" 
												readonly placeholder="Automatically calculated" > 
											<small>Automatically calculated from the movie duration.</small> 
										</div>								
										<!-- Base Ticket Price -->
										<div class="showtimes-input-group">
											<label for="price">
												Base Ticket Price (VND)
											</label>
											<input 
												type="number" 
												id="price" 
												name="price" 
												min="1" 
												step="1" 
												placeholder="e.g. 75000" 
												required>
										</div>
									</div>
									<div class="showtimes-form-actions">
										<button type="submit" class="showtimes-button showtimes-button-primary">
											Create Showtime
										</button>
									</div>
								</form>
							</section>
							<!-- Showtime List -->
							<section class="showtimes-section">
								<div class="showtimes-section-heading">
									<div>
							            <h3 class="showtimes-section-title">View Showtimes</h3>
							            <p class="showtimes-section-description">
							                Showing schedule for <%= selectedDate %>.
							            </p>
							        </div>
							        <span class="showtimes-count">
							            <%= showtimes == null ? 0 : showtimes.size() %> showtime(s)
							        </span>
								</div>
								
								<!-- Date toolbar -->
							    <form class="showtimes-filter-form" action="${pageContext.request.contextPath}/manager-showtimes" method="get">
							        <div class="showtimes-filter-fields">
							            <div class="showtimes-input-group">
							                <label for="date">Date</label>
							                <input
							                    type="date"
							                    id="date"
							                    name="date"
							                    value="<%= selectedDate %>"
							                    onchange="this.form.submit()"
							                    required>
							            </div>
							            <noscript>
							                <div class="showtimes-filter-action">
							                    <button type="submit" class="showtimes-button showtimes-button-primary">
							                        View
							                    </button>
							                </div>
							            </noscript>
							        </div>
							    </form>
								<%
									if (showtimes == null || showtimes.isEmpty()) {
								%>
										<div class="showtimes-empty-state">
											<p>
												No showtimes were found for this date.
											</p>
										</div>
								<%
									} 
									else {
								%>
										<div class="showtimes-table-wrapper">
											<table class="showtimes-table">
												<thead>
													<tr>
														<th>ID</th>
														<th>Movie ID</th>
														<th>Room ID</th>
														<th>Start Time</th>
														<th>End Time</th>
														<th>Status</th>
														<th>Action</th>
													</tr>
												</thead>
												<tbody>
													<%
														for (Showtimes showtime : showtimes) {
															String status = showtime.getStatus() == null
																	? "UNKNOWN"
																	: showtime.getStatus().toUpperCase();
															String statusClass = status.toLowerCase()
																	.replaceAll("[^a-z0-9_-]", "");
													%>
															<tr>
																<td>
																	<%= showtime.getShowtime_id() %>
																</td>
																<td>
																	<%= showtime.getMovie_id() %>
																</td>
																<td>
																	<%= showtime.getRoom_id() %>
																</td>
																<td>
																	<%= showtime.getStart_at() == null
																			? "-"
																			: dateTimeFormat.format(showtime.getStart_at()) %>
																</td>
																<td>
																	<%= showtime.getEnd_at() == null
																			? "-"
																			: dateTimeFormat.format(showtime.getEnd_at()) %>
																</td>
																<td>
																	<span class="showtimes-status status-<%= statusClass %>">
																		<%= status %>
																	</span>
																</td>
																<td>
																	<%
																		if (showtime.getDeleted_at() == null && !"CANCELED".equals(status) && !"COMPLETED".equals(status)) {
																	%>
																			<form class="showtimes-delete-form" action="${pageContext.request.contextPath}/manager-showtimes" method="post" onsubmit="return confirm('Remove this showtime?');">
																				<input type="hidden" name="action" value="delete">
																				<input type="hidden" name="date" value="<%= selectedDate %>">
																				<input type="hidden" name="showtimeId" value="<%= showtime.getShowtime_id() %>">
																				<button type="submit" class="showtimes-button showtimes-button-danger">
																					Remove
																				</button>
																			</form>
																	<%
																		} 
																		else {
																	%>
																			<span class="showtimes-no-action">
																				—
																			</span>
																	<%
																		}
																	%>
																</td>
															</tr>
													<%
														}
													%>
												</tbody>
											</table>
										</div>
								<%
									}
								%>
							</section>
					<%
						}
					%>
				</div>
			</main>
		</section>
		<script 
			src="<%= request.getContextPath() %>/scripts/manager_showtimes.js">
		</script>
	</body>
</html>