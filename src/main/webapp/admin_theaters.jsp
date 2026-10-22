<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.AdminTheaterRow" %>
<%@ page import="com.movieweb.model.AdminManagerRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "theaters");
	String ctx = request.getContextPath();
	List<AdminTheaterRow> rows = (List<AdminTheaterRow>) request.getAttribute("theaterRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String status = (String) request.getAttribute("status");
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	AdminTheaterRow selected = (AdminTheaterRow) request.getAttribute("selectedTheater");
	Theaters selectedFull = (Theaters) request.getAttribute("selectedTheaterFull");
	List<AdminManagerRow> assigned = (List<AdminManagerRow>) request.getAttribute("assignedManagers");
	List<AdminManagerRow> assignable = (List<AdminManagerRow>) request.getAttribute("assignableManagers");
	if (assigned == null) assigned = Collections.emptyList();
	if (assignable == null) assignable = Collections.emptyList();
	Map<String, String> form = (Map<String, String>) request.getAttribute("form");
	String formMode = (String) request.getAttribute("formMode");
	Integer formId = (Integer) request.getAttribute("formId");
	SimpleDateFormat hm = new SimpleDateFormat("HH:mm");
	String[][] tabs = {{"ALL", "All"}, {"ACTIVE", "Active"}, {"INACTIVE", "Inactive"}};
%>
<%!
	private String esc(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
	private String fv(Map<String, String> form, String key) {
		return form == null ? "" : esc(form.get(key));
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Theaters | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_theaters.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Theaters</h2>
						<p>Create and edit theaters, switch them on or off, and assign managers.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-theaters">
						<input type="hidden" name="status" value="<%= esc(status) %>">
						<label for="theater-search">Search</label>
						<input id="theater-search" class="admin-input" type="text" name="q" maxlength="100"
							placeholder="Name or address" value="<%= esc(q) %>">
						<button type="submit" class="admin-btn">Search</button>
						<a class="admin-btn admin-btn-primary" href="<%= ctx %>/admin-theaters?new=1&status=<%= esc(status) %>">New theater</a>
					</form>
				</div>

				<%
					if (flash != null) {
				%>
						<div class="admin-alert admin-alert-success"><%= esc(flash) %></div>
				<%
					}
				%>
				<%
					if (flashError != null) {
				%>
						<div class="admin-alert admin-alert-error"><%= esc(flashError) %></div>
				<%
					}
				%>
				<%
					if (loadError) {
				%>
						<div class="admin-alert admin-alert-error">The theater data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<%
					if (form != null) {
						boolean creating = "create".equals(formMode);
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= creating ? "New theater" : "Edit theater" %></h3>
							</div>
							<div class="admin-panel-content">
								<form class="admin-form" method="post" action="<%= ctx %>/admin-theaters">
									<input type="hidden" name="action" value="<%= creating ? "create" : "update" %>">
									<input type="hidden" name="status" value="<%= esc(status) %>">
									<%
										if (!creating) {
									%>
											<input type="hidden" name="id" value="<%= formId == null ? 0 : formId %>">
									<%
										}
									%>
									<label>
										Name 
										<input class="admin-input" type="text" name="name" maxlength="100" required value="<%= fv(form, "name") %>">
									</label>
									<label>
										Address 
										<input class="admin-input" type="text" name="address" maxlength="255" required value="<%= fv(form, "address") %>">
									</label>
									<label>
										Image path 
										<input class="admin-input" type="text" name="imagePath" maxlength="255" required
											placeholder="pictures/theaters/example.jpg" value="<%= fv(form, "imagePath") %>">
									</label>
									<p class="admin-muted">Path of an image already placed in the project's pictures folder (uploading is not part of this page).</p>
									<label>
										Description 
										<textarea class="admin-input" name="description" rows="4" maxlength="1000"><%= fv(form, "description") %></textarea>
									</label>
									<div class="admin-form-row">
										<label>
											Latitude 
											<input class="admin-input" type="number" name="latitude" step="any" min="-90" max="90" required value="<%= fv(form, "latitude") %>">
										</label>
										<label>
											Longitude 
											<input class="admin-input" type="number" name="longitude" step="any" min="-180" max="180" required value="<%= fv(form, "longitude") %>">
										</label>
										<label>
											Opens 
											<input class="admin-input" type="time" name="openTime" required value="<%= fv(form, "openTime") %>">
										</label>
										<label>
											Closes 
											<input class="admin-input" type="time" name="closingTime" required value="<%= fv(form, "closingTime") %>">
										</label>
									</div>
									<div class="admin-inline-form">
										<button class="admin-btn admin-btn-primary" type="submit"><%= creating ? "Create theater" : "Save changes" %></button>
										<a class="admin-btn" href="<%= ctx %>/admin-theaters?status=<%= esc(status) %><%= !creating && formId != null ? "&id=" + formId : "" %>">Cancel</a>
									</div>
								</form>
							</div>
						</section>
				<%
					}
				%>

				<nav class="admin-tabs">
					<%
						for (String[] tab : tabs) {
					%>
							<a class="admin-tab <%= tab[0].equals(status) ? "active" : "" %>"
								href="<%= ctx %>/admin-theaters?status=<%= tab[0] %>&q=<%= qEnc %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No theaters match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Theater</th>
												<th>Hours</th>
												<th>Rooms</th>
												<th>Managers</th>
												<th>Upcoming showtimes</th>
												<th>Status</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminTheaterRow r : rows) {
											%>
													<tr>
														<td>
															<strong><%= esc(r.getTheaterName()) %></strong>
															<br>
															<span class="admin-muted"><%= esc(r.getAddress()) %></span>
														</td>
														<td>
															<%= r.getOpenTime() == null ? "--" : hm.format(r.getOpenTime()) %> - <%= r.getClosingTime() == null ? "--" : hm.format(r.getClosingTime()) %>
														</td>
														<td><%= r.getActiveRoomCount() %> active / <%= r.getRoomCount() %></td>
														<td><%= r.getManagerCount() %></td>
														<td><%= r.getUpcomingShowtimes() %></td>
														<td>
															<span class="admin-status <%= r.isActive() ? "admin-status-confirmed" : "admin-status-cancelled" %>">
																<%= r.isActive() ? "Active" : "Inactive" %>
															</span>
														</td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-theaters?status=<%= esc(status) %>&q=<%= qEnc %>&id=<%= r.getTheaterId() %>">Details</a>
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
											<a class="admin-btn" href="<%= ctx %>/admin-theaters?status=<%= esc(status) %>&q=<%= qEnc %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-theaters?status=<%= esc(status) %>&q=<%= qEnc %>&page=<%= pageNo + 1 %>">Next</a>
									<%
										}
									%>
								</div>
						<%
							}
						%>
					</div>
				</section>

				<%
					if (selected != null && selectedFull != null) {
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3>
									<%= esc(selected.getTheaterName()) %>
									<span class="admin-status <%= selected.isActive() ? "admin-status-confirmed" : "admin-status-cancelled" %>">
										<%= selected.isActive() ? "Active" : "Inactive" %>
									</span>
								</h3>
								<a href="<%= ctx %>/admin-dashboard?theaterId=<%= selected.getTheaterId() %>">View dashboard for this theater</a>
							</div>
							<div class="admin-panel-content">
								<p><%= esc(selected.getAddress()) %></p>
								<%
									if (selectedFull.getDescription() != null && !selectedFull.getDescription().isEmpty()) {
								%>
										<p class="admin-muted"><%= esc(selectedFull.getDescription()) %></p>
								<%
									}
								%>
								<p class="admin-muted">
									Opens <%= selected.getOpenTime() == null ? "--" : hm.format(selected.getOpenTime()) %>,
									closes <%= selected.getClosingTime() == null ? "--" : hm.format(selected.getClosingTime()) %> ·
									<%= selected.getActiveRoomCount() %> active of <%= selected.getRoomCount() %> rooms ·
									<%= selected.getUpcomingShowtimes() %> upcoming showtimes ·
									<%= selectedFull.getLatitude() %>, <%= selectedFull.getLongtitude() %>
								</p>

								<div class="admin-inline-form">
									<a class="admin-btn" href="<%= ctx %>/admin-theaters?edit=<%= selected.getTheaterId() %>&status=<%= esc(status) %>">Edit details</a>
									<form method="post" action="<%= ctx %>/admin-theaters">
										<input type="hidden" name="id" value="<%= selected.getTheaterId() %>">
										<input type="hidden" name="status" value="<%= esc(status) %>">
										<%
											if (selected.isActive()) {
										%>
												<button class="admin-btn admin-btn-danger" name="action" value="deactivate"
													onclick="return confirm('Deactivate this theater? Rooms, showtimes, bookings and payments are NOT changed<%= selected.getUpcomingShowtimes() > 0 ? ", and " + selected.getUpcomingShowtimes() + " upcoming showtime(s) remain scheduled" : "" %>.');">Deactivate</button>
										<%
											} else {
										%>
												<button class="admin-btn admin-btn-primary" name="action" value="activate">Activate</button>
										<%
											}
										%>
									</form>
								</div>

								<h4>Managers</h4>
								<%
									if (assigned.isEmpty()) {
								%>
										<p class="admin-muted">No manager is assigned to this theater.</p>
								<%
									} else {
								%>
										<div class="admin-table-wrapper">
											<table class="admin-data-table">
												<tbody>
													<%
														for (AdminManagerRow m : assigned) {
													%>
															<tr>
																<td>
																	<strong><%= esc(m.getFullName()) %></strong> 
																	<span class="admin-muted">@<%= esc(m.getUsername()) %></span>
																</td>
																<td><%= esc(m.getEmail()) %></td>
																<td>
																	<span class="admin-status <%= m.isActive() ? "admin-status-confirmed" : "admin-status-cancelled" %>">
																		<%= m.isActive() ? "Active" : "Deactivated" %>
																	</span>
																</td>
																<td class="admin-actions">
																	<form method="post" action="<%= ctx %>/admin-theaters">
																		<input type="hidden" name="id" value="<%= selected.getTheaterId() %>">
																		<input type="hidden" name="userId" value="<%= m.getUserId() %>">
																		<input type="hidden" name="status" value="<%= esc(status) %>">
																		<button class="admin-btn admin-btn-danger" name="action" value="unassignManager"
																			onclick="return confirm('Remove this manager from the theater?');">Remove</button>
																	</form>
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
								<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-theaters">
									<input type="hidden" name="id" value="<%= selected.getTheaterId() %>">
									<input type="hidden" name="status" value="<%= esc(status) %>">
									<select class="admin-input" name="userId" required>
										<option value="">Assign an approved manager...</option>
										<%
											for (AdminManagerRow m : assignable) {
										%>
												<option value="<%= m.getUserId() %>"><%= esc(m.getFullName()) %> (@<%= esc(m.getUsername()) %>)</option>
										<%
											}
										%>
									</select>
									<button class="admin-btn admin-btn-primary" name="action" value="assignManager">Assign</button>
								</form>
								<p class="admin-muted">Rooms, seats and showtimes are managed by the theater's managers for now; admin pages for them are not built yet.</p>
							</div>
						</section>
				<%
					}
				%>
			</main>
		</section>
	</body>
</html>