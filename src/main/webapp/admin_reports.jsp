<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminReportRow" %>
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
	request.setAttribute("currentAdminPage", "reports");
	String ctx = request.getContextPath();
	List<AdminReportRow> rows = (List<AdminReportRow>) request.getAttribute("reportRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	AdminReportRow selected = (AdminReportRow) request.getAttribute("selectedReport");
	List<String[]> reporters = (List<String[]>) request.getAttribute("reporters");
	if (reporters == null) {
		reporters = Collections.emptyList();
	}
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<%!
	private String esc(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
	private String shorten(String value, int max) {
		if (value == null) {
			return "";
		}
		return value.length() <= max ? value : value.substring(0, max) + "...";
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Reported Comments | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_reports.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Reported Comments</h2>
						<p>Comments that users reported. Dismiss the reports, or remove the comment.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-reports">
						<label for="report-search">Search</label>
						<input id="report-search" class="admin-input" type="text" name="q" maxlength="100"
							placeholder="Comment, author or movie" value="<%= esc(q) %>">
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
						<div class="admin-alert admin-alert-error">The reports could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No reported comments<%= q != null && !q.isEmpty() ? " match this search" : " are waiting for review" %>.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Comment</th>
												<th>Author</th>
												<th>Movie</th>
												<th>Reports</th>
												<th>Last reported</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminReportRow r : rows) {
											%>
													<tr>
														<td class="admin-comment-text"><%= esc(shorten(r.getCommentText(), 140)) %></td>
														<td>@<%= esc(r.getAuthorUsername() == null ? "unknown" : r.getAuthorUsername()) %></td>
														<td><%= esc(r.getMovieName()) %></td>
														<td><%= r.getReportCount() %></td>
														<td><%= r.getLastReportedAt() == null ? "--" : df.format(r.getLastReportedAt()) %></td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-reports?q=<%= qEnc %>&id=<%= r.getCommentId() %>">Review</a>
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
											<a class="admin-btn" href="<%= ctx %>/admin-reports?q=<%= qEnc %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-reports?q=<%= qEnc %>&page=<%= pageNo + 1 %>">Next</a>
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
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3>Comment #<%= selected.getCommentId() %> on <%= esc(selected.getMovieName()) %></h3>
								<a href="<%= ctx %>/movie-details?id=<%= selected.getMovieId() %>">Open movie page</a>
							</div>
							<div class="admin-panel-content">
								<p class="admin-muted">
									By @<%= esc(selected.getAuthorUsername() == null ? "unknown" : selected.getAuthorUsername()) %>
									<%
										if (selected.isAuthorDeleted()) {
									%>
											(account closed)
									<%
										} else if (!selected.isAuthorActive()) {
									%>
											(account blocked)
									<%
										}
									%>
									· posted <%= selected.getCommentCreatedAt() == null ? "--" : df.format(selected.getCommentCreatedAt()) %>
								</p>
								<p class="admin-comment-text admin-comment-full"><%= esc(selected.getCommentText()) %></p>

								<h4><%= selected.getReportCount() %> report(s)</h4>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<tbody>
											<%
												for (String[] rep : reporters) {
											%>
													<tr>
														<td>@<%= esc(rep[0] == null ? "unknown" : rep[0]) %> <span class="admin-muted"><%= esc(rep[1]) %></span></td>
														<td><%= esc(rep[2]) %></td>
													</tr>
											<%
												}
											%>
										</tbody>
									</table>
								</div>

								<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-reports">
									<input type="hidden" name="commentId" value="<%= selected.getCommentId() %>">
									<button class="admin-btn" name="action" value="dismiss"
										onclick="return confirm('Dismiss all reports? The comment stays visible and the report history is cleared.');">Dismiss reports</button>
									<button class="admin-btn admin-btn-danger" name="action" value="remove"
										onclick="return confirm('Permanently remove this comment and its reports? This cannot be undone.');">Remove comment</button>
									<%
										if (!selected.isAuthorDeleted()) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-users?id=<%= selected.getAuthorId() %>">View author</a>
									<%
										}
									%>
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