<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.AdminLabelRow" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.net.URLEncoder" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	/* Always load the page through the servlet. */
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD) {
		response.sendRedirect(request.getContextPath() + "/admin-genres-tags");
		return;
	}
	request.setAttribute("currentAdminPage", "genres-tags");
	String ctx = request.getContextPath();
	String q = (String) request.getAttribute("q");
	if (q == null) q = "";
	String qEnc = URLEncoder.encode(q, "UTF-8");
	List<AdminLabelRow> genres = (List<AdminLabelRow>) request.getAttribute("genreRows");
	List<AdminLabelRow> tags = (List<AdminLabelRow>) request.getAttribute("tagRows");
	if (genres == null) genres = Collections.emptyList();
	if (tags == null) tags = Collections.emptyList();
	boolean loadError = Boolean.TRUE.equals(request.getAttribute("loadError"));
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	/* The same markup is used twice: once for genres, once for tags. */
	String[] kinds = {"genre", "tag"};
	String[] titles = {"Genres", "Tags"};
	List<List<AdminLabelRow>> lists = java.util.Arrays.asList(genres, tags);
	/* Paging: genres and tags are paged independently (?genrePage=N&tagPage=N). */
	int[] pageNo = new int[2];
	int[] pageTotal = new int[2];
	int[] total = new int[2];
	for (int k = 0; k < 2; k++) {
		Integer pg = (Integer) request.getAttribute(kinds[k] + "Page");
		Integer pgs = (Integer) request.getAttribute(kinds[k] + "Pages");
		Integer tot = (Integer) request.getAttribute(kinds[k] + "Total");
		pageNo[k] = pg == null ? 1 : pg;
		pageTotal[k] = pgs == null ? 1 : pgs;
		total[k] = tot == null ? 0 : tot;
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Genres and Tags | PhnetPhlyx</title>
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
						<h2>Genres and Tags</h2>
						<p>These are the choices offered when adding or editing a movie and in the Movies page filters. Deleting one removes it from every movie that uses it.</p>
					</div>
					<form class="admin-theater-filter" method="get" action="<%= ctx %>/admin-genres-tags">
						<label for="label-search">Search</label>
						<input id="label-search" class="admin-input" type="text" name="q" maxlength="50"
							placeholder="Genre or tag name" value="<%= ViewUtils.h(q) %>">
						<button type="submit" class="admin-btn">Search</button>
						<%
							if (!q.isEmpty()) {
						%>
								<a class="admin-btn" href="<%= ctx %>/admin-genres-tags">Clear</a>
						<%
							}
						%>
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
						<div class="admin-alert admin-alert-error">The data could not be loaded. The error has been logged.</div>
				<%
					}
				%>

				<%
					for (int k = 0; k < kinds.length; k++) {
						String kind = kinds[k];
						List<AdminLabelRow> rows = lists.get(k);
				%>
						<section class="admin-panel">
							<div class="admin-panel-header">
								<h3><%= titles[k] %> (<%= total[k] %>)</h3>
							</div>
							<div class="admin-panel-content">
								<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-genres-tags">
									<input type="hidden" name="action" value="add">
									<input type="hidden" name="genrePage" value="<%= pageNo[0] %>">
									<input type="hidden" name="tagPage" value="<%= pageNo[1] %>">
									<input type="hidden" name="q" value="<%= ViewUtils.h(q) %>">
									<input type="hidden" name="kind" value="<%= kind %>">
									<input class="admin-input" type="text" name="name" maxlength="50" required
										placeholder="New <%= kind %> name" aria-label="New <%= kind %> name">
									<button type="submit" class="admin-btn admin-btn-primary">Add <%= kind %></button>
								</form>
								<%
									if (rows.isEmpty()) {
								%>
										<p><%= q.isEmpty() ? "No " + kind + "s yet." : "No " + kind + "s match \"" + ViewUtils.h(q) + "\"." %></p>
								<%
									} else {
								%>
										<div class="admin-table-wrapper">
											<table class="admin-data-table">
												<thead>
													<tr>
														<th>Name</th>
														<th>Movies using it</th>
														<th></th>
													</tr>
												</thead>
												<tbody>
													<%
														for (AdminLabelRow r : rows) {
															String warning = r.getMovieCount() > 0
																? "It is used by " + r.getMovieCount() + " movie" + (r.getMovieCount() == 1 ? "" : "s") + " and will be removed from them. "
																: "";
													%>
															<tr>
																<td><%= ViewUtils.h(r.getName()) %></td>
																<td><%= r.getMovieCount() %></td>
																<td class="admin-actions">
																	<form class="admin-inline-form" method="post" action="<%= ctx %>/admin-genres-tags">
																		<input type="hidden" name="action" value="delete">
																		<input type="hidden" name="genrePage" value="<%= pageNo[0] %>">
																		<input type="hidden" name="tagPage" value="<%= pageNo[1] %>">
									<input type="hidden" name="q" value="<%= ViewUtils.h(q) %>">
																		<input type="hidden" name="kind" value="<%= kind %>">
																		<input type="hidden" name="id" value="<%= r.getId() %>">
																		<button class="admin-btn admin-btn-danger" type="submit"
																			data-confirm="Delete the <%= kind %> &quot;<%= ViewUtils.h(r.getName()) %>&quot;? <%= ViewUtils.h(warning) %>This cannot be undone."
																			onclick="return confirm(this.getAttribute('data-confirm'));">Delete</button>
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
												if (pageNo[k] > 1) {
											%>
													<a class="admin-btn" href="<%= ctx %>/admin-genres-tags?q=<%= qEnc %>&genrePage=<%= k == 0 ? pageNo[k] - 1 : pageNo[0] %>&tagPage=<%= k == 1 ? pageNo[k] - 1 : pageNo[1] %>">Previous</a>
											<%
												}
											%>
											<span class="admin-muted">Page <%= pageNo[k] %> of <%= pageTotal[k] %></span>
											<%
												if (pageNo[k] < pageTotal[k]) {
											%>
													<a class="admin-btn" href="<%= ctx %>/admin-genres-tags?q=<%= qEnc %>&genrePage=<%= k == 0 ? pageNo[k] + 1 : pageNo[0] %>&tagPage=<%= k == 1 ? pageNo[k] + 1 : pageNo[1] %>">Next</a>
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
					}
				%>
			</main>
		</section>
	</body>
</html>
