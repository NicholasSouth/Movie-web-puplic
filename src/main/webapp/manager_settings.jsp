<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%
	Users currentUser = (Users) session.getAttribute("user");
	if (currentUser == null) {
		response.sendRedirect(request.getContextPath() + "/log_in.jsp");
		return;
	}
	if (!"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	String success = (String) request.getAttribute("success");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Settings | PhnetPhlyx Manager</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_settings.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<div class="Layout2">
			<!-- Header -->
			<jsp:include page="/components/header_manager.jsp" />
			
			<!-- Left Sidebar -->
			<%@ include file="components/manager_navigation.jsp" %>
			
			<!-- Main Content -->
			<main class="ManagerMain manager-settings-page">
				<div class="manager-settings-container">
					<!-- Success Message -->
					<%
						if (success != null) {
					%>
							<div class="settings-success-message">
								<%= success %>
							</div>
					<%
						}
					%>
					<!-- Header -->
					<div class="manager-settings-header">
						<h2>Settings</h2>
						<p>Manage your account preferences and security.</p>
					</div>
					<!-- Security -->
					<section class="manager-settings-section">
						<h3 class="manager-settings-section-title">
							Security
						</h3>
						<div class="manager-settings-card">
							<div class="manager-settings-card-information">
								<h4>Password</h4>
								<p>Change your password to keep your account secure.</p>
							</div>
							<div class="manager-settings-card-action">
								<a href="${pageContext.request.contextPath}/manager_change_password.jsp" class="manager-settings-button">
									Change Password
								</a>
							</div>
						</div>
					</section>
					<!-- Appearance -->
					<section class="manager-settings-section">
						<h3 class="manager-settings-section-title">
							Appearance
						</h3>
						<div class="manager-settings-card">
							<div class="manager-settings-card-information">
								<h4>Theme</h4>
								<p>Choose how PhnetPhlyx looks.</p>
							</div>
							<div class="manager-theme-options" data-theme-user="<%= currentUser.getUserId() %>">
								<label class="manager-theme-option">
									<input type="radio" name="theme" value="dark" checked>
									<span>Dark</span>
								</label>
								<label class="manager-theme-option">
									<input type="radio" name="theme" value="light">
									<span>Light</span>
								</label>
							</div>
						</div>
					</section>
				</div>
			</main>
		</div>
		<script src="${pageContext.request.contextPath}/scripts/theme.js"></script>
	</body>
</html>