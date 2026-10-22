<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminUserRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "users");
	String ctx = request.getContextPath();
	List<AdminUserRow> rows = (List<AdminUserRow>) request.getAttribute("userRows");
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
	AdminUserRow selected = (AdminUserRow) request.getAttribute("selectedUser");
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String[][] tabs = {{"ALL", "All"}, {"ACTIVE", "Active"}, {"BLOCKED", "Blocked"}, {"DELETED", "Closed by user"}};
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
		if ("BLOCKED".equals(state)) return "admin-status-cancelled";
		return "admin-status-pending";
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Users | PhnetPhlyx</title>
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
						<h2>Users</h2>
						<p>Review customer accounts and block or unblock logins.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-users">
						<input type="hidden" name="status" value="<%= esc(status) %>">
						<label for="user-search">Search</label>
						<input id="user-search" class="admin-input" type="text" name="q" maxlength="100"
							placeholder="Name, username, email or phone" value="<%= esc(q) %>">
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
						<div class="admin-alert admin-alert-error">The user data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<nav class="admin-tabs">
					<%
						for (String[] tab : tabs) {
					%>
							<a class="admin-tab <%= tab[0].equals(status) ? "active" : "" %>"
								href="<%= ctx %>/admin-users?status=<%= tab[0] %>&q=<%= qEnc %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No user accounts match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>User</th>
												<th>Contact</th>
												<th>Registered</th>
												<th>Comments</th>
												<th>Reports against</th>
												<th>State</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminUserRow u : rows) {
													String state = u.getState();
											%>
													<tr>
														<td>
															<strong><%= esc(u.getFullName()) %></strong>
															<br>
															<span class="admin-muted">@<%= esc(u.getUsername()) %></span>
														</td>
														<td>
															<%= esc(u.getEmail()) %>
															<br>
															<span class="admin-muted"><%= esc(u.getPhone()) %></span>
														</td>
														<td><%= u.getCreatedAt() == null ? "--" : df.format(u.getCreatedAt()) %></td>
														<td><%= u.getCommentCount() %></td>
														<td><%= u.getReportsReceived() %></td>
														<td>
															<span class="admin-status <%= stateClass(state) %>"><%= state %></span>
														</td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-users?status=<%= esc(status) %>&q=<%= qEnc %>&id=<%= u.getUserId() %>">Details</a>
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
											<a class="admin-btn" href="<%= ctx %>/admin-users?status=<%= esc(status) %>&q=<%= qEnc %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-users?status=<%= esc(status) %>&q=<%= qEnc %>&page=<%= pageNo + 1 %>">Next</a>
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
								<p>@<%= esc(selected.getUsername()) %> · <%= esc(selected.getEmail()) %> · <%= esc(selected.getPhone()) %></p>
								<p class="admin-muted">
									Registered <%= selected.getCreatedAt() == null ? "--" : df.format(selected.getCreatedAt()) %>
									<%
										if (selected.getDeletedAt() != null) {
									%>
											· closed by the user on <%= df.format(selected.getDeletedAt()) %>
									<%
										}
									%>
								</p>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<tbody>
											<tr>
												<td>Comments posted</td>
												<td><%= selected.getCommentCount() %></td>
											</tr>
											<tr>
												<td>Bookings</td>
												<td><%= selected.getBookingCount() %></td>
											</tr>
											<tr>
												<td>Reports against their comments</td>
												<td><%= selected.getReportsReceived() %></td>
											</tr>
											<tr>
												<td>Reports they filed</td>
												<td><%= selected.getReportsFiled() %></td>
											</tr>
										</tbody>
									</table>
								</div>
								<%
									if ("DELETED".equals(state)) {
								%>
										<p class="admin-muted">This account was closed by its owner, so it cannot log in and cannot be changed here.</p>
								<%
									} else {
								%>
										<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-users">
											<input type="hidden" name="userId" value="<%= selected.getUserId() %>">
											<input type="hidden" name="status" value="<%= esc(status) %>">
											<%
												if (selected.isActive()) {
											%>
													<button class="admin-btn admin-btn-danger" name="action" value="block"
														onclick="return confirm('Block this user? They will not be able to log in. Their bookings and comments are kept.');">Block user</button>
											<%
												} else {
											%>
													<button class="admin-btn admin-btn-primary" name="action" value="unblock">Unblock user</button>
											<%
												}
											%>
										</form>
								<%
									}
								%>
							</div>
						</section>
				<%
					}
				%>
			</main>
		</section>
	</body>
</html>