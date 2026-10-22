<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
	Users loggedInUser = (Users) session.getAttribute("user");
	if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	/* Always load the page through the servlet. */
	if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD
			|| request.getAttribute("profileUser") == null) {
		response.sendRedirect(request.getContextPath() + "/admin-profile");
		return;
	}
	Users profileUser = (Users) request.getAttribute("profileUser");
	if (profileUser.getUserId() != loggedInUser.getUserId()) {
		response.sendRedirect(request.getContextPath() + "/main.jsp");
		return;
	}
	request.setAttribute("currentAdminPage", "profile");
	String ctx = request.getContextPath();
	String fullName = ViewUtils.text(profileUser.getFullName(), profileUser.getUsername());
	String avatarLetter = ViewUtils.hasText(fullName) ? fullName.trim().substring(0, 1).toUpperCase() : "?";
	String flash = (String) request.getAttribute("flash");
	String flashError = (String) request.getAttribute("flashError");
	String errorSection = (String) request.getAttribute("errorSection");
	String errorMessage = (String) request.getAttribute("errorMessage");
	String formFullName = request.getAttribute("formFullName") != null ? (String) request.getAttribute("formFullName") : ViewUtils.text(profileUser.getFullName(), "");
	String formEmail = request.getAttribute("formEmail") != null ? (String) request.getAttribute("formEmail") : ViewUtils.text(profileUser.getEmail(), "");
	String formPhone = request.getAttribute("formPhone") != null ? (String) request.getAttribute("formPhone") : ViewUtils.text(profileUser.getPhone(), "");
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title><%= ViewUtils.h(fullName) %> Admin Profile | PhnetPhlyx</title>
		<link rel="stylesheet" href="<%= ctx %>/styles/main_admin.css">
		<link rel="stylesheet" href="<%= ctx %>/styles/admin_profile.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	
	<body>
		<section class="Layout3">
			<jsp:include page="/components/header_admin.jsp" />
			<%@ include file="components/admin_navigation.jsp" %>
			<main class="AdminMain">
				<div class="ap-page">

					<%
						if (flash != null) {
					%>
							<div class="admin-alert admin-alert-success" role="status"><%= ViewUtils.h(flash) %></div>
					<%
						}
					%>
					<%
						if (flashError != null) {
					%>
							<div class="admin-alert admin-alert-error" role="alert"><%= ViewUtils.h(flashError) %></div>
					<%
						}
					%>

					<!-- Profile -->
					<section class="ap-header">
						<div class="ap-banner">
							<%
								if (ViewUtils.hasText(profileUser.getBannerPath())) {
							%>
									<img src="<%= ViewUtils.h(ViewUtils.asset(ctx, profileUser.getBannerPath())) %>" alt="Profile banner">
							<%
								}
							%>
						</div>
						<div class="ap-header-info">
							<div class="ap-avatar">
								<%
									if (ViewUtils.hasText(profileUser.getAvtPath())) {
								%>
										<img src="<%= ViewUtils.h(ViewUtils.asset(ctx, profileUser.getAvtPath())) %>" alt="<%= ViewUtils.h(fullName) %>">
								<%
									} else {
								%>
										<div class="default-avatar"><%= ViewUtils.h(avatarLetter) %></div>
								<%
									}
								%>
							</div>
							<div class="ap-identity">
								<h2><%= ViewUtils.h(fullName) %></h2>
								<p class="ap-username">@<%= ViewUtils.h(profileUser.getUsername()) %></p>
								<span class="ap-role">Administrator</span>
								<div class="ap-contact">
									<p>Email: <span><%= ViewUtils.h(ViewUtils.text(profileUser.getEmail(), "Not provided")) %></span></p>
									<p>Phone: <span><%= ViewUtils.h(ViewUtils.text(profileUser.getPhone(), "Not provided")) %></span></p>
								</div>
							</div>
						</div>
					</section>

					<!-- Information change -->
					<section class="admin-panel" id="information">
						<div class="admin-panel-header">
							<h3>Personal information</h3>
						</div>
						<div class="admin-panel-content">
							<%
								if ("information".equals(errorSection) && errorMessage != null) {
							%>
									<div class="admin-alert admin-alert-error" role="alert"><%= ViewUtils.h(errorMessage) %></div>
							<%
								}
							%>
							<form class="ap-form" action="<%= ctx %>/admin-profile" method="post" enctype="multipart/form-data">
								<input type="hidden" name="action" value="information">

								<div class="ap-field">
									<label for="avatar">Avatar</label>
									<div class="ap-file-row">
										<input class="ap-file-input" type="file" id="avatar" name="avatar" accept=".jpg,.jpeg,.png,.webp">
										<label for="avatar" class="admin-btn">Choose file</label>
										<span class="ap-file-name" id="avatarName">No file chosen</span>
									</div>
									<p class="ap-hint">JPG, PNG, or WebP. Maximum size: 5 MB.</p>
								</div>

								<div class="ap-field">
									<label for="banner">Banner</label>
									<div class="ap-file-row">
										<input class="ap-file-input" type="file" id="banner" name="banner" accept=".jpg,.jpeg,.png,.webp">
										<label for="banner" class="admin-btn">Choose file</label>
										<span class="ap-file-name" id="bannerName">No file chosen</span>
									</div>
									<p class="ap-hint">JPG, PNG, or WebP. Maximum size: 10 MB.</p>
								</div>

								<div class="ap-field">
									<label for="username">Username</label>
									<input class="admin-input" type="text" id="username" value="<%= ViewUtils.h(profileUser.getUsername()) %>" readonly>
									<p class="ap-hint">Your username cannot be changed.</p>
								</div>

								<div class="ap-field">
									<label for="fullName">Full name</label>
									<input class="admin-input" type="text" id="fullName" name="fullName" value="<%= ViewUtils.h(formFullName) %>" required>
								</div>

								<div class="ap-field">
									<label for="email">Email</label>
									<input class="admin-input" type="email" id="email" name="email" value="<%= ViewUtils.h(formEmail) %>" required>
									<p class="ap-hint">Verification codes for your password change are sent to this address.</p>
								</div>

								<div class="ap-field">
									<label for="phone">Phone</label>
									<input class="admin-input" type="text" id="phone" name="phone" value="<%= ViewUtils.h(formPhone) %>">
									<p class="ap-hint">Optional.</p>
								</div>

								<div class="ap-actions">
									<button type="submit" class="admin-btn admin-btn-primary">Save changes</button>
								</div>
							</form>
						</div>
					</section>

					<!-- Password change -->
					<section class="admin-panel" id="password">
						<div class="admin-panel-header">
							<h3>Change password</h3>
						</div>
						<div class="admin-panel-content">
							<%
								if ("password".equals(errorSection) && errorMessage != null) {
							%>
									<div class="admin-alert admin-alert-error" role="alert"><%= ViewUtils.h(errorMessage) %></div>
							<%
								}
							%>
							<form class="ap-form" id="adminPasswordForm" action="<%= ctx %>/admin-profile" method="post"
								data-send-url="<%= ctx %>/admin-send-verification">
								<input type="hidden" name="action" value="password">

								<div class="ap-field">
									<label for="currentPassword">Current password</label>
									<div class="password-wrapper">
										<input class="admin-input" type="password" id="currentPassword" name="currentPassword"
											autocomplete="current-password" required>
										<jsp:include page="/components/password_toggle.jsp">
											<jsp:param name="inputId" value="currentPassword" />
										</jsp:include>
									</div>
								</div>

								<div class="ap-field">
									<label for="newPassword">New password</label>
									<div class="password-wrapper">
										<input class="admin-input" type="password" id="newPassword" name="newPassword"
											autocomplete="new-password" required>
										<jsp:include page="/components/password_toggle.jsp">
											<jsp:param name="inputId" value="newPassword" />
										</jsp:include>
									</div>
									<p class="ap-hint">At least 8 characters, different from your current password.</p>
								</div>

								<div class="ap-field">
									<label for="confirmPassword">Confirm new password</label>
									<div class="password-wrapper">
										<input class="admin-input" type="password" id="confirmPassword" name="confirmPassword"
											autocomplete="new-password" required>
										<jsp:include page="/components/password_toggle.jsp">
											<jsp:param name="inputId" value="confirmPassword" />
										</jsp:include>
									</div>
								</div>

								<div class="ap-field">
									<label for="verification-code">Email verification</label>
									<div class="ap-verify-row">
										<input class="admin-input" type="text" id="verification-code" name="verification_code"
											placeholder="6-digit code" maxlength="6" inputmode="numeric"
											autocomplete="one-time-code" required>
										<button type="button" class="admin-btn" id="adminSendCodeButton"
											onclick="sendAdminVerificationCode()">Send Code</button>
									</div>
									<p class="ap-hint">A code is sent to your account email and expires in 5 minutes.</p>
								</div>

								<div class="ap-actions">
									<button type="submit" class="admin-btn admin-btn-primary">Change password</button>
								</div>
							</form>
						</div>
					</section>

					<!-- Appearance -->
					<section class="admin-panel" id="appearance">
						<div class="admin-panel-header">
							<h3>Appearance</h3>
						</div>
						<div class="admin-panel-content">
							<div class="ap-theme-row">
								<div>
									<strong>Theme</strong>
									<p class="ap-theme-hint">Choose how PhnetPhlyx looks.</p>
								</div>
								<div class="ap-theme-options" data-theme-user="<%= loggedInUser.getUserId() %>">
									<label class="ap-theme-option">
										<input type="radio" name="theme" value="dark" checked>
										<span>Dark</span>
									</label>
									<label class="ap-theme-option">
										<input type="radio" name="theme" value="light">
										<span>Light</span>
									</label>
								</div>
							</div>
						</div>
					</section>

					<!-- Account -->
					<section class="admin-panel" id="account">
						<div class="admin-panel-header">
							<h3>Account</h3>
						</div>
						<div class="admin-panel-content">
							<div class="ap-note">
								Administrator accounts cannot be deleted.
							</div>
						</div>
					</section>

				</div>
			</main>
		</section>
		<script src="<%= ctx %>/scripts/admin_profile.js"></script>
		<script src="<%= ctx %>/scripts/theme.js"></script>
	</body>
</html>