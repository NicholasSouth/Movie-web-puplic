<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.model.AdminManagerRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "managers");
	String ctx = request.getContextPath();
	List<AdminManagerRow> managers = (List<AdminManagerRow>) request.getAttribute("managers");
	if (managers == null) {
		managers = Collections.emptyList();
	}
	String status = (String) request.getAttribute("status");
	String q = (String) request.getAttribute("q");
	Integer page_ = (Integer) request.getAttribute("page");
	Integer pages = (Integer) request.getAttribute("pages");
	Integer pendingCount = (Integer) request.getAttribute("pendingCount");
	int pageNo = page_ == null ? 1 : page_;
	int pageTotal = pages == null ? 1 : pages;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	AdminManagerRow selected = (AdminManagerRow) request.getAttribute("selectedManager");
	List<Theaters> selectedTheaters = (List<Theaters>) request.getAttribute("selectedTheaters");
	if (selectedTheaters == null) {
		selectedTheaters = Collections.emptyList();
	}
	List<Theaters> allTheaters = (List<Theaters>) request.getAttribute("adminTheaters");
	if (allTheaters == null) {
		allTheaters = Collections.emptyList();
	}
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String[][] tabs = {{"ALL", "All"}, {"PENDING", "Pending"}, {"ACTIVE", "Active"}, {"DEACTIVATED", "Deactivated"}};
%>
<%!
	private String esc(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
	private String stateClass(String state) {
		if ("ACTIVE".equals(state)) return "admin-status-confirmed";
		if ("PENDING".equals(state)) return "admin-status-pending";
		return "admin-status-cancelled";
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Managers | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Managers</h2>
						<p>
							Approve new manager accounts and assign them to theaters.
							<%
								if (pendingCount != null && pendingCount > 0) {
							%>
									<strong><%= pendingCount %> pending</strong>
							<%
								}
							%>
						</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-managers">
						<input type="hidden" name="status" value="<%= esc(status) %>">
						<label for="manager-search">Search</label>
						<input id="manager-search" class="admin-input" type="text" name="q" maxlength="100"
							placeholder="Name, username or email" value="<%= esc(q) %>">
						<button type="submit" class="admin-btn">Search</button>
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
						<div class="admin-alert admin-alert-error">The manager list could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<nav class="admin-tabs">
					<%
						for (String[] tab : tabs) {
					%>
							<a class="admin-tab <%= tab[0].equals(status) ? "active" : "" %>"
								href="<%= ctx %>/admin-managers?status=<%= tab[0] %>&q=<%= java.net.URLEncoder.encode(q == null ? "" : q, "UTF-8") %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (managers.isEmpty()) {
						%>
								<p>No manager accounts match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Manager</th>
												<th>Contact</th>
												<th>Registered</th>
												<th>Theaters</th>
												<th>State</th>
												<th>Actions</th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminManagerRow m : managers) {
													String state = m.getState();
											%>
													<tr>
														<td>
															<strong><%= esc(m.getFullName()) %></strong>
															<br>
															<span class="admin-muted">@<%= esc(m.getUsername()) %></span>
														</td>
														<td>
															<%= esc(m.getEmail()) %>
															<br>
															<span class="admin-muted"><%= esc(m.getPhone()) %></span>
														</td>
														<td><%= m.getCreatedAt() == null ? "--" : df.format(m.getCreatedAt()) %></td>
														<td><%= m.getTheaterCount() %></td>
														<td>
															<span class="admin-status <%= stateClass(state) %>"><%= state %></span>
														</td>
														<td class="admin-actions">
															<%
																if ("PENDING".equals(state)) {
															%>
																	<form method="post" action="<%= ctx %>/admin-managers">
																		<input type="hidden" name="userId" value="<%= m.getUserId() %>">
																		<input type="hidden" name="status" value="<%= esc(status) %>">
																		<button class="admin-btn admin-btn-primary" name="action" value="approve">Approve</button>
																		<button class="admin-btn admin-btn-danger" name="action" value="reject"
																			onclick="return confirm('Reject this application? The account will be removed.');">Reject</button>
																	</form>
															<%
																} else {
															%>
																	<a class="admin-btn" href="<%= ctx %>/admin-managers?status=<%= esc(status) %>&q=<%= java.net.URLEncoder.encode(q == null ? "" : q, "UTF-8") %>&userId=<%= m.getUserId() %>">Details</a>
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
								<div class="admin-pagination">
									<%
										if (pageNo > 1) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-managers?status=<%= esc(status) %>&q=<%= java.net.URLEncoder.encode(q == null ? "" : q, "UTF-8") %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-managers?status=<%= esc(status) %>&q=<%= java.net.URLEncoder.encode(q == null ? "" : q, "UTF-8") %>&page=<%= pageNo + 1 %>">Next</a>
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
					if (selected != null) {
						String state = selected.getState();
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= esc(selected.getFullName()) %> <span class="admin-status <%= stateClass(state) %>"><%= state %></span></h3>
							</div>
							<div class="admin-panel-content">
								<p><%= esc(selected.getEmail()) %> · <%= esc(selected.getPhone()) %></p>

								<h4>Assigned theaters</h4>
								<%
									if (selectedTheaters.isEmpty()) {
								%>
										<p class="admin-muted">Not assigned to any theater.</p>
								<%
									} else {
								%>
										<div class="admin-table-wrapper">
											<table class="admin-data-table">
												<tbody>
													<%
														for (Theaters t : selectedTheaters) {
													%>
															<tr>
																<td><%= esc(t.getTheater_name()) %><%= t.isActive() ? "" : " (inactive)" %></td>
																<td class="admin-actions">
																	<form method="post" action="<%= ctx %>/admin-managers">
																		<input type="hidden" name="userId" value="<%= selected.getUserId() %>">
																		<input type="hidden" name="theaterId" value="<%= t.getTheater_id() %>">
																		<input type="hidden" name="status" value="<%= esc(status) %>">
																		<button class="admin-btn admin-btn-danger" name="action" value="unassign"
																			onclick="return confirm('Remove this theater assignment?');">Remove</button>
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

								<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-managers">
									<input type="hidden" name="userId" value="<%= selected.getUserId() %>">
									<input type="hidden" name="status" value="<%= esc(status) %>">
									<select class="admin-input" name="theaterId" required>
										<option value="">Assign a theater...</option>
										<%
											for (Theaters t : allTheaters) {
												boolean already = false;
												for (Theaters a : selectedTheaters) {
													if (a.getTheater_id() == t.getTheater_id()) {
														already = true;
													}
												}
												if (already) {
													continue;
												}
										%>
												<option value="<%= t.getTheater_id() %>"><%= esc(t.getTheater_name()) %><%= t.isActive() ? "" : " (inactive)" %></option>
										<%
											}
										%>
									</select>
									<button class="admin-btn admin-btn-primary" name="action" value="assign">Assign</button>
								</form>

								<h4>Account</h4>
								<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-managers">
									<input type="hidden" name="userId" value="<%= selected.getUserId() %>">
									<input type="hidden" name="status" value="<%= esc(status) %>">
									<%
										if (selected.isActive()) {
									%>
											<button class="admin-btn" name="action" value="deactivate"
												onclick="return confirm('Deactivate this manager? They will not be able to log in.');">Deactivate</button>
									<%
										} else {
									%>
											<button class="admin-btn admin-btn-primary" name="action" value="activate">Activate</button>
									<%
										}
									%>
									<button class="admin-btn admin-btn-danger" name="action" value="delete"
										onclick="return confirm('Delete this manager account and all of their theater assignments?');">Delete account</button>
								</form>
							</div>
						</section>
				<%
					}
				%>
			</main>
		</section>
	</body>
</html>