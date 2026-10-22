<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminPromotionRow" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "promotions");
	String ctx = request.getContextPath();
	List<AdminPromotionRow> rows = (List<AdminPromotionRow>) request.getAttribute("promotionRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String status = (String) request.getAttribute("status");
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	Integer totalAttr = (Integer) request.getAttribute("totalPromotions");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	int total = totalAttr == null ? 0 : totalAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	Map<String, String> form = (Map<String, String>) request.getAttribute("form");
	String formMode = (String) request.getAttribute("formMode");
	Integer formId = (Integer) request.getAttribute("formId");
	Integer formUsed = (Integer) request.getAttribute("formUsedCount");
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	String[][] tabs = {{"ALL", "All"}, {"ACTIVE", "Active"}, {"INACTIVE", "Inactive"}};
	String listQuery = "status=" + status + "&q=" + qEnc;
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
	private String stateClass(String state) {
		if ("LIVE".equals(state)) return "admin-status-success";
		if ("SCHEDULED".equals(state)) return "admin-status-scheduled";
		if ("EXPIRED".equals(state) || "EXHAUSTED".equals(state) || "NO LIMIT".equals(state)) return "admin-status-pending";
		return ""; // DISABLED: neutral grey
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Promotions | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_theaters.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_promotions.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Promotions</h2>
						<p>Promotion codes apply to every theater. <%= total %> code<%= total == 1 ? "" : "s" %> in this view.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-promotions">
						<input type="hidden" name="status" value="<%= esc(status) %>">
						<label for="promo-search">Search</label>
						<input id="promo-search" class="admin-input" type="text" name="q" maxlength="20"
							placeholder="Promotion code" value="<%= esc(q) %>">
						<button type="submit" class="admin-btn">Search</button>
						<a class="admin-btn admin-btn-primary" href="<%= ctx %>/admin-promotions?new=1&<%= listQuery %>">New promotion</a>
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
						<div class="admin-alert admin-alert-error">The promotion data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<%
					if (form != null) {
						boolean creating = "create".equals(formMode);
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= creating ? "New promotion" : "Edit promotion" %></h3>
							</div>
							<div class="admin-panel-content">
								<form class="admin-form" method="post" action="<%= ctx %>/admin-promotions">
									<input type="hidden" name="action" value="<%= creating ? "create" : "update" %>">
									<%
										if (!creating) {
									%>
											<input type="hidden" name="id" value="<%= formId == null ? 0 : formId %>">
									<%
										}
									%>
									<input type="hidden" name="status" value="<%= esc(status) %>">
									<input type="hidden" name="q" value="<%= esc(q) %>">
									<input type="hidden" name="page" value="<%= pageNo %>">
									<div class="admin-form-row">
										<label>
											Code 
											<input class="admin-input" type="text" name="code" maxlength="20" required value="<%= fv(form, "code") %>">
										</label>
										<label>
											Price modifier 
											<input class="admin-input" type="text" name="priceModify" maxlength="5" required
												placeholder="-10% or -20" value="<%= fv(form, "priceModify") %>">
										</label>
										<label>
											Usage limit 
											<input class="admin-input" type="number" name="usageLimit" min="1" step="1" required value="<%= fv(form, "usageLimit") %>">
										</label>
									</div>
									<span class="admin-form-hint">
										Price modifier: <strong>-10%</strong> takes 10% off the ticket price; <strong>-20</strong> (no %) takes a fixed
										20,000 VND off (the number is in thousands). Use negative numbers to discount. Whole numbers only, at most 5 characters.
									</span>
									<label>
										Description 
										<textarea class="admin-input" name="description" rows="2" maxlength="255" required><%= fv(form, "description") %></textarea>
									</label>
									<div class="admin-form-row">
										<label>
											Starts 
											<input class="admin-input" type="datetime-local" name="startAt" required value="<%= fv(form, "startAt") %>">
										</label>
										<label>
											Ends 
											<input class="admin-input" type="datetime-local" name="endAt" required value="<%= fv(form, "endAt") %>">
										</label>
									</div>
									<%
										if (creating) {
									%>
											<label class="admin-check">
												<input type="checkbox" name="isActive" value="true" <%= "true".equals(form.get("isActive")) ? "checked" : "" %>> Active immediately
											</label>
									<%
										} else if (formUsed != null && formUsed > 0) {
									%>
											<span class="admin-form-hint">This code is used by <%= formUsed %> booking<%= formUsed == 1 ? "" : "s" %> (refunded bookings count until the refund is approved). Changes only affect future bookings.</span>
									<%
										}
									%>
									<div class="admin-inline-form">
										<button type="submit" class="admin-btn admin-btn-primary"><%= creating ? "Create promotion" : "Save changes" %></button>
										<a class="admin-btn" href="<%= ctx %>/admin-promotions?<%= listQuery %>&page=<%= pageNo %>">Cancel</a>
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
								href="<%= ctx %>/admin-promotions?status=<%= tab[0] %>&q=<%= qEnc %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No promotions match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Code</th>
												<th>Description</th>
												<th>Modifier</th>
												<th>Valid period</th>
												<th>Used</th>
												<th>State</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminPromotionRow p : rows) {
													String state = p.getState();
											%>
													<tr>
														<td><span class="admin-promo-code"><%= esc(p.getCode()) %></span></td>
														<td class="admin-promo-description"><%= esc(p.getDescription()) %></td>
														<td><%= esc(p.getPriceModify()) %></td>
														<td>
															<%= p.getStartAt() == null ? "--" : df.format(p.getStartAt()) %>
															<br>
															<span class="admin-muted">to <%= p.getEndAt() == null ? "--" : df.format(p.getEndAt()) %></span>
														</td>
														<td class="admin-promo-usage"><%= p.getUsedCount() %> / <%= p.getUsageLimit() > 0 ? String.valueOf(p.getUsageLimit()) : "no limit set" %></td>
														<td><span class="admin-status <%= stateClass(state) %>"><%= state %></span></td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-promotions?edit=<%= p.getPromotionId() %>&<%= listQuery %>&page=<%= pageNo %>">Edit</a>
															<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-promotions">
																<input type="hidden" name="id" value="<%= p.getPromotionId() %>">
																<input type="hidden" name="status" value="<%= esc(status) %>">
																<input type="hidden" name="q" value="<%= esc(q) %>">
																<input type="hidden" name="page" value="<%= pageNo %>">
																<%
																	if (p.isActive()) {
																%>
																		<button class="admin-btn admin-btn-danger" name="action" value="deactivate"
																			onclick="return confirm('Deactivate this promotion? Customers will no longer be able to apply it.');">Deactivate</button>
																<%
																	} else {
																%>
																		<button class="admin-btn admin-btn-primary" name="action" value="activate">Activate</button>
																<%
																	}
																%>
															</form>
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
											<a class="admin-btn" href="<%= ctx %>/admin-promotions?<%= listQuery %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-promotions?<%= listQuery %>&page=<%= pageNo + 1 %>">Next</a>
									<%
										}
									%>
								</div>
						<%
							}
						%>
						<p class="admin-muted">Promotions are never deleted, because bookings reference them. Deactivate a code to stop it being used.</p>
					</div>
				</section>
			</main>
		</section>
	</body>
</html>