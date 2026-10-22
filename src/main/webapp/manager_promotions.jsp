<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.movieweb.model.Promotions" %>
<%
	request.setAttribute("currentManagerPage", "promotions");
	List<Promotions> promotions = (List<Promotions>) request.getAttribute("promotions");
	Integer currentPageObject = (Integer) request.getAttribute("currentPage");
	Integer totalPagesObject = (Integer) request.getAttribute("totalPages");
	Integer totalPromotionsObject = (Integer) request.getAttribute("totalPromotions");
	int currentPage = currentPageObject == null ? 1 : currentPageObject;
	int totalPages = totalPagesObject == null ? 1 : totalPagesObject;
	int totalPromotions = totalPromotionsObject == null ? 0 : totalPromotionsObject;
	String successMessage = (String) request.getAttribute("successMessage");
	String errorMessage = (String) request.getAttribute("errorMessage");
	Timestamp now = new Timestamp(System.currentTimeMillis());
%>
<%!
	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
	private String formatPromotionDate(Timestamp timestamp) {
		if (timestamp == null) {
			return "-";
		}
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp);
	}
	private String formatDateTimeLocal(Timestamp timestamp) {
		if (timestamp == null) {
			return "";
		}
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(timestamp);
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Manage Promotions | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= request.getContextPath() %>/styles/main_manager.css">
		<link rel="stylesheet" href="<%= request.getContextPath() %>/styles/manager_promotions.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<div class="Layout2">
			<%@ include file="components/header_manager.jsp" %>
			<%@ include file="components/manager_navigation.jsp" %>
			<main class="ManagerMain ManagerPromotions">
				<div class="PromotionPage">
					<div class="PromotionPageHeader">
						<div>
							<span class="PromotionBreadcrumb">
								Manage Theaters / Promotion Management
							</span>
							<h1>Promotion Management</h1>
							<p>Create and manage promotion codes for PhnetPhlyx.</p>
						</div>
					</div>
					<%
						if (successMessage != null && !successMessage.isEmpty()) {
					%>
							<div class="PromotionNotice PromotionNoticeSuccess">
								<%= escapeHtml(successMessage) %>
							</div>
					<%
						}
					%>
					<%
						if (errorMessage != null && !errorMessage.isEmpty()) {
					%>
							<div class="PromotionNotice PromotionNoticeError">
								<%= escapeHtml(errorMessage) %>
							</div>
					<%
						}
					%>
					<!-- CREATE PROMOTION -->
					<section class="PromotionPanel">
						<div class="PromotionPanelHeader">
							<div>
								<h2>Create Promotion</h2>
								<p>Configure a new promotion code and its validity period.</p>
							</div>
						</div>
						<form action="<%= request.getContextPath() %>/manager-promotions" method="post" class="PromotionForm">
							<input type="hidden" name="action" value="create">
							<div class="PromotionFormGrid">
								<div class="PromotionFormGroup">
									<label for="promotion_code">Promotion Code *</label>
									<input 
										type="text" 
										id="promotion_code" 
										name="promotion_code" 
										maxlength="20" 
										placeholder="e.g. MOVIE10" 
										autocomplete="off" 
										required>
									<small>Maximum 20 characters.</small>
								</div>
								<div class="PromotionFormGroup">
									<label for="price_modify">Price Modifier *</label>
									<input 
										type="text" 
										id="price_modify" 
										name="price_modify" 
										maxlength="5" 
										placeholder="e.g. -10% or -20" 
										required>
									<small>
										Percentage: -10% means 10% off. 
										Fixed amount: -20 means 20,000 VND off.
									</small>
								</div>
								<div class="PromotionFormGroup PromotionFormFull">
									<label for="description">Description *</label>
									<textarea 
										id="description" 
										name="description" 
										rows="3" 
										placeholder="Describe this promotion..." 
										required></textarea>
								</div>
								<div class="PromotionFormGroup">
									<label for="start_at">Start Date &amp; Time *</label>
									<input 
										type="datetime-local" 
										id="start_at" 
										name="start_at" 
										required>
								</div>
								<div class="PromotionFormGroup">
									<label for="end_at">End Date &amp; Time *</label>
									<input 
										type="datetime-local" 
										id="end_at" 
										name="end_at" 
										required>
								</div>
								<div class="PromotionFormGroup">
									<label for="usage_limit">Usage Limit *</label>
									<input 
										type="number" 
										id="usage_limit" 
										name="usage_limit" 
										min="1" 
										step="1" 
										placeholder="e.g. 100" 
										required>
									<small>Maximum number of eligible bookings.</small>
								</div>
								<div class="PromotionFormGroup PromotionActiveGroup">
									<label for="isActive">Initial Status</label>
									<label class="PromotionCheckboxLabel">
										<input 
											type="checkbox" 
											id="isActive" 
											name="isActive" 
											value="true" 
											checked>
										<span>Activate immediately</span>
									</label>
									<small>
										Uncheck to create the promotion as inactive.
									</small>
								</div>
							</div>
							<div class="PromotionFormActions">
								<button type="reset" class="PromotionButton PromotionButtonSecondary">
									Clear Form
								</button>
								<button type="submit" class="PromotionButton PromotionButtonPrimary">
									Create Promotion
								</button>
							</div>
						</form>
					</section>
					<!-- PROMOTION LIST -->
					<section class="PromotionPanel">
						<div class="PromotionPanelHeader PromotionListHeader">
							<div>
								<h2>Promotion Codes</h2>
								<p>Showing up to 10 promotions per page, newest first.</p>
							</div>
							<div class="PromotionTotal">
								<span>Total Promotions</span>
								<strong><%= totalPromotions %></strong>
							</div>
						</div>
						<%
							if (promotions == null || promotions.isEmpty()) {
						%>
								<div class="PromotionEmptyState">
									<h3>No promotions found</h3>
									<p>Create your first promotion using the form above.</p>
								</div>
						<%
							} else {
						%>
								<div class="PromotionTableWrapper">
									<table class="PromotionTable">
										<thead>
											<tr>
												<th>ID</th>
												<th>Promotion</th>
												<th>Modifier</th>
												<th>Validity</th>
												<th>Usage Limit</th>
												<th>Status</th>
											</tr>
										</thead>
										<tbody>
											<%
												for (Promotions promotion : promotions) {
													String status;
													String statusClass;
													if (!promotion.isActive()) {
														status = "Inactive";
														statusClass = "Inactive";
													} else if (promotion.getEnd_at() != null && promotion.getEnd_at().before(now)) {
														status = "Expired";
														statusClass = "Expired";
													} else if (promotion.getStart_at() != null && promotion.getStart_at().after(now)) {
														status = "Scheduled";
														statusClass = "Scheduled";
													} else {
														status = "Active";
														statusClass = "Active";
													}
											%>
													<tr>
														<td class="PromotionId">
															#<%= promotion.getPromotion_id() %>
														</td>
														<td class="PromotionDetails">
															<strong>
																<%= escapeHtml(promotion.getPromotion_code()) %>
															</strong>
															<span>
																<%= escapeHtml(promotion.getDescription()) %>
															</span>
														</td>
														<td class="PromotionModifier">
															<%= escapeHtml(promotion.getPrice_modify()) %>
														</td>
														<td class="PromotionValidity">
															<%= formatPromotionDate(promotion.getStart_at()) %>
															-
															<%= formatPromotionDate(promotion.getEnd_at()) %>
														</td>
														<td>
															<%= promotion.getUsage_limit() %>
														</td>
														<td>
															<span class="PromotionStatus PromotionStatus<%= statusClass %>">
																<%= status %>
															</span>
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
						<!-- PAGINATION -->
						<%
							if (totalPages > 1) {
						%>
								<nav class="PromotionPagination" aria-label="Promotion pages">
									<%
										if (currentPage > 1) {
									%>
											<a class="PromotionPageLink" href="<%= request.getContextPath() %>/manager-promotions?page=1" aria-label="First page">
												First
											</a>
											<a class="PromotionPageLink" href="<%= request.getContextPath() %>/manager-promotions?page=<%= currentPage - 1 %>" aria-label="Previous page">
												Previous
											</a>
									<%
										}
										int startPage = Math.max(1, currentPage - 2);
										int endPage = Math.min(totalPages, currentPage + 2);
										for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
									%>
											<a class="PromotionPageLink <%= pageNumber == currentPage ? "PromotionPageActive" : "" %>" href="<%= request.getContextPath() %>/manager-promotions?page=<%= pageNumber %>" <%= pageNumber == currentPage ? "aria-current=\"page\"" : "" %>>
												<%= pageNumber %>
											</a>
									<%
										}
										if (currentPage < totalPages) {
									%>
											<a class="PromotionPageLink" href="<%= request.getContextPath() %>/manager-promotions?page=<%= currentPage + 1 %>" aria-label="Next page">
												Next
											</a>
											<a class="PromotionPageLink" href="<%= request.getContextPath() %>/manager-promotions?page=<%= totalPages %>" aria-label="Last page">
												Last
											</a>
									<%
										}
									%>
								</nav>
						<%
							}
						%>
					</section>
				</div>
			</main>
		</div>
	</body>
</html>