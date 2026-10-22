<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminAdRow" %>
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
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/admin-ads");
		return;
	}
	request.setAttribute("currentAdminPage", "ads");
	String ctx = request.getContextPath();
	List<AdminAdRow> rows = (List<AdminAdRow>) request.getAttribute("adRows");
	if (rows == null) {
		rows = Collections.emptyList();
	}
	String status = (String) request.getAttribute("status");
	String q = (String) request.getAttribute("q");
	String qEnc = URLEncoder.encode(q == null ? "" : q, "UTF-8");
	Integer pageAttr = (Integer) request.getAttribute("page");
	Integer pagesAttr = (Integer) request.getAttribute("pages");
	Integer totalAttr = (Integer) request.getAttribute("totalAds");
	int pageNo = pageAttr == null ? 1 : pageAttr;
	int pageTotal = pagesAttr == null ? 1 : pagesAttr;
	int total = totalAttr == null ? 0 : totalAttr;
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	Map<String, String> form = (Map<String, String>) request.getAttribute("form");
	String formMode = (String) request.getAttribute("formMode");
	Integer formId = (Integer) request.getAttribute("formId");
	String formImagePath = (String) request.getAttribute("formImagePath");
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
		if ("EXPIRED".equals(state)) return "admin-status-pending";
		return ""; // DISABLED: neutral grey
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Advertisements | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_theaters.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_ads.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="admin-header">
					<div class="admin-header-info">
						<h2>Advertisements</h2>
						<p>Ads appear in the right sidebar of the customer pages while they are active and inside their period. <%= total %> ad<%= total == 1 ? "" : "s" %> in this view.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-ads">
						<input type="hidden" name="status" value="<%= esc(status) %>">
						<label for="ad-search">Search</label>
						<input id="ad-search" class="admin-input" type="text" name="q" maxlength="100"
							placeholder="Advertisement name" value="<%= esc(q) %>">
						<button type="submit" class="admin-btn">Search</button>
						<a class="admin-btn admin-btn-primary" href="<%= ctx %>/admin-ads?new=1&<%= listQuery %>">New advertisement</a>
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
						<div class="admin-alert admin-alert-error">The advertisement data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<%
					if (form != null) {
						boolean creating = "create".equals(formMode);
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= creating ? "New advertisement" : "Edit advertisement" %></h3>
							</div>
							<div class="admin-panel-content">
								<form class="admin-form" method="post" action="<%= ctx %>/admin-ads" enctype="multipart/form-data">
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
											Name 
											<input class="admin-input" type="text" name="name" maxlength="100" required value="<%= fv(form, "name") %>">
										</label>
										<label>
											Link 
											<input class="admin-input" type="url" name="link" maxlength="2000" required
												placeholder="https://example.com/offer" value="<%= fv(form, "link") %>">
										</label>
									</div>
									<%
										if (!creating && formImagePath != null && !formImagePath.isEmpty()) {
									%>
											<div class="admin-ad-current">
												<img class="admin-ad-thumb" src="<%= ctx %>/<%= esc(formImagePath) %>" alt="Current advertisement image">
												<span class="admin-form-hint">Current image. Choose a file below only if you want to replace it.</span>
											</div>
									<%
										}
									%>
									<label>
										<%= creating ? "Image" : "New image (optional)" %>
										<input class="admin-input" type="file" name="image" accept=".jpg,.jpeg,.png,.webp" <%= creating ? "required" : "" %>>
									</label>
									<span class="admin-form-hint">JPG, PNG or WebP, up to 5 MB. Ads are shown about 150 px tall and cropped to fit the sidebar, so a wide banner (for example 600 x 300) works best.</span>
									<div class="admin-form-row">
										<label>
											Starts (optional) 
											<input class="admin-input" type="datetime-local" name="startAt" value="<%= fv(form, "startAt") %>">
										</label>
										<label>
											Ends (optional) 
											<input class="admin-input" type="datetime-local" name="endAt" value="<%= fv(form, "endAt") %>">
										</label>
									</div>
									<span class="admin-form-hint">Leave a date empty for no limit. An ad is shown only while it is active and the current time is inside this period.</span>
									<%
										if (creating) {
									%>
											<label class="admin-check">
												<input type="checkbox" name="isActive" value="true" <%= "true".equals(form.get("isActive")) ? "checked" : "" %>> Active immediately
											</label>
									<%
										}
									%>
									<div class="admin-inline-form">
										<button type="submit" class="admin-btn admin-btn-primary"><%= creating ? "Create advertisement" : "Save changes" %></button>
										<a class="admin-btn" href="<%= ctx %>/admin-ads?<%= listQuery %>&page=<%= pageNo %>">Cancel</a>
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
								href="<%= ctx %>/admin-ads?status=<%= tab[0] %>&q=<%= qEnc %>"><%= tab[1] %></a>
					<%
						}
					%>
				</nav>

				<section class="admin-panel">
					<div class="admin-panel-content">
						<%
							if (rows.isEmpty()) {
						%>
								<p>No advertisements match this filter.</p>
						<%
							} else {
						%>
								<div class="admin-table-wrapper">
									<table class="admin-data-table">
										<thead>
											<tr>
												<th>Image</th>
												<th>Advertisement</th>
												<th>Period</th>
												<th>State</th>
												<th></th>
											</tr>
										</thead>
										<tbody>
											<%
												for (AdminAdRow a : rows) {
													String state = a.getState();
											%>
													<tr>
														<td>
															<img class="admin-ad-thumb" src="<%= ctx %>/<%= esc(a.getImagePath()) %>" alt="<%= esc(a.getName()) %>">
														</td>
														<td>
															<div class="admin-ad-name"><%= esc(a.getName()) %></div>
															<div class="admin-ad-link">
																<a href="<%= esc(a.getLink()) %>" target="_blank" rel="noopener noreferrer"><%= esc(a.getLink()) %></a>
															</div>
														</td>
														<td>
															<%= a.getStartAt() == null ? "No start limit" : df.format(a.getStartAt()) %>
															<br>
															<span class="admin-muted">to <%= a.getEndAt() == null ? "no end limit" : df.format(a.getEndAt()) %></span>
														</td>
														<td>
															<span class="admin-status <%= stateClass(state) %>"><%= state %></span>
														</td>
														<td class="admin-actions">
															<a class="admin-btn" href="<%= ctx %>/admin-ads?edit=<%= a.getAdId() %>&<%= listQuery %>&page=<%= pageNo %>">Edit</a>
															<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-ads">
																<input type="hidden" name="id" value="<%= a.getAdId() %>">
																<input type="hidden" name="status" value="<%= esc(status) %>">
																<input type="hidden" name="q" value="<%= esc(q) %>">
																<input type="hidden" name="page" value="<%= pageNo %>">
																<%
																	if (a.isActive()) {
																%>
																		<button class="admin-btn admin-btn-danger" name="action" value="deactivate"
																			onclick="return confirm('Deactivate this advertisement? It will no longer be shown to customers.');">Deactivate</button>
																<%
																	} else {
																%>
																		<button class="admin-btn admin-btn-primary" name="action" value="activate">Activate</button>
																<%
																	}
																%>
																<button class="admin-btn admin-btn-danger" name="action" value="delete"
																	onclick="return confirm('Delete this advertisement? This cannot be undone from the admin pages.');">Delete</button>
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
											<a class="admin-btn" href="<%= ctx %>/admin-ads?<%= listQuery %>&page=<%= pageNo - 1 %>">Previous</a>
									<%
										}
									%>
									<span class="admin-muted">Page <%= pageNo %> of <%= pageTotal %></span>
									<%
										if (pageNo < pageTotal) {
									%>
											<a class="admin-btn" href="<%= ctx %>/admin-ads?<%= listQuery %>&page=<%= pageNo + 1 %>">Next</a>
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