<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminTheaterRequestRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/admin-theater-requests");
		return;
	}
	request.setAttribute("currentAdminPage", "theater-requests");
	String ctx = request.getContextPath();
	List<AdminTheaterRequestRow> rows = (List<AdminTheaterRequestRow>) request.getAttribute("requests");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String status = (String) request.getAttribute("status");
	Integer page_ = (Integer) request.getAttribute("page");
	Integer pages = (Integer) request.getAttribute("pages");
	Integer pendingCount = (Integer) request.getAttribute("pendingCount");
	int pageNo = page_ == null ? 1 : page_;
	int pageTotal = pages == null ? 1 : pages;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String[][] tabs = {{"PENDING", "Pending"}, {"APPROVED", "Approved"}, {"REJECTED", "Rejected"}, {"CANCELLED", "Cancelled"}, {"ALL", "All"}};
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
		if ("APPROVED".equals(state)) return "admin-status-confirmed";
		if ("PENDING".equals(state)) return "admin-status-pending";
		return "admin-status-cancelled";
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Theater Requests | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<%@ include file="/components/theme.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Theater Requests</h2>
						<p>
							Review requests from managers to be assigned to, or removed from, a theater.
							<%
								if (pendingCount != null && pendingCount > 0) {
							%>
									<strong><%= pendingCount %> pending</strong>
							<%
								}
							%>
						</p>
					</div>
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
						<div class="admin-alert admin-alert-error">The requests could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<nav class="admin-tabs">
					<%
						for (String[] tab : tabs) {
					%>
							<a class="admin-tab <%= tab[0].equals(status) ? "active" : "" %>"
								href="<%= ctx %>/admin-theater-requests?status=<%= tab[0] %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No requests match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Manager</th>
												<th>Theater</th>
												<th>Request</th>
												<th>Sent</th>
												<th>Manager's note</th>
												<th>Status</th>
												<th>Review</th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminTheaterRequestRow r : rows) {
													boolean isPending = "PENDING".equals(r.getStatus());
											%>
													<tr>
														<td>
															<strong><%= esc(r.getFullName()) %></strong>
															<br>
															<span class="admin-muted">@<%= esc(r.getUsername()) %></span>
														</td>
														<td><%= esc(r.getTheaterName()) %></td>
														<td><%= "ASSIGN".equals(r.getRequestType()) ? "Assign to theater" : "Remove from theater" %></td>
														<td><%= r.getCreatedAt() == null ? "--" : df.format(r.getCreatedAt()) %></td>
														<td><%= r.getNote() == null || r.getNote().isEmpty() ? "--" : esc(r.getNote()) %></td>
														<td>
															<span class="admin-status <%= stateClass(r.getStatus()) %>"><%= r.getStatus() %></span>
														</td>
														<td class="admin-actions">
															<%
																if (isPending) {
															%>
																	<form method="post" action="<%= ctx %>/admin-theater-requests">
																		<input type="hidden" name="requestId" value="<%= r.getRequestId() %>">
																		<input type="hidden" name="status" value="<%= esc(status) %>">
																		<input class="admin-input" type="text" name="reviewNote" maxlength="500"
																			placeholder="Reply to manager (optional)" aria-label="Reply to manager">
																		<label class="admin-muted">
																			<input type="checkbox" name="sendEmail" value="1" checked> Email the manager
																		</label>
																		<button class="admin-btn admin-btn-primary" name="action" value="approve"
																			onclick="return confirm('Approve and apply this request now?');">Approve</button>
																		<button class="admin-btn admin-btn-danger" name="action" value="reject"
																			onclick="return confirm('Reject this request?');">Reject</button>
																	</form>
															<%
																} else {
															%>
																	<span class="admin-muted">
																		<%= r.getReviewedAt() == null ? "" : df.format(r.getReviewedAt()) %>
																		<%
																			if (r.getReviewerName() != null) {
																		%>
																				by <%= esc(r.getReviewerName()) %>
																		<%
																			}
																		%>
																		<%
																			if (r.getReviewNote() != null && !r.getReviewNote().isEmpty()) {
																		%>
																				<br><%= esc(r.getReviewNote()) %>
																		<%
																			}
																		%>
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
								<div class="admin-pagination">
									<%
										if (pageNo > 1) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-theater-requests?status=<%= esc(status) %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-theater-requests?status=<%= esc(status) %>&page=<%= pageNo + 1 %>">Next</a>
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