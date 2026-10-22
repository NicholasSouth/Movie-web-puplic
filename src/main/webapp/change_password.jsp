<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.movieweb.model.Users" %>
<%
    Users currentUser = (Users) session.getAttribute("user");

    // User must be logged in to access this page.
    if (currentUser == null)
    {
        response.sendRedirect(request.getContextPath() + "/log_in.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta
	        name="viewport"
	        content="width=device-width, initial-scale=1.0">
	    <title>
	        Change Password - PhnetPhlyx
	    </title>
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main.css">
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/change_password.css">
	        <%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<section class="Layout1">
		    <!-- Header -->
		    <jsp:include page="/components/header.jsp" />
		
		    <!-- Left Sidebar -->
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main Content -->
		    <main class="MainBody">
		        <div class="change-password-container">
		            <!-- Page Title -->
		            <h2>
		                Change Password
		            </h2>
		            <p class="change-password-subtitle">
		                Change your password to keep your account secure.
		            </p>
		
		            <!-- Error Message -->
		            <%
		                if (request.getAttribute("error") != null)
		                {
		            %>
			                <div class="error-message">
			                    <%= request.getAttribute("error") %>
			                </div>
		            <%
		                }
		            %>							
		
		            <!-- Change Password Form -->
		            <form
		                class="change-password-form"
		                action="${pageContext.request.contextPath}/change-password"
		                method="post">
		
		                <!-- Current Password -->
		                <div class="input-group">
		                    <label for="currentPassword">
		                        Current Password
		                    </label>
		                    <div class="password-wrapper">
		                        <input
		                            type="password"
		                            id="currentPassword"
		                            name="currentPassword"
		                            placeholder="Enter your current password"
		                            autocomplete="current-password"
		                            required>
		                        <jsp:include page="/components/password_toggle.jsp">
        							<jsp:param name="inputId" value="currentPassword" />
    							</jsp:include>
		                    </div>
		                </div>
		
		                <!-- New Password -->
		                <div class="input-group">
		                    <label for="newPassword">
		                        New Password
		                    </label>
		                    <div class="password-wrapper">
		                        <input
		                            type="password"
		                            id="newPassword"
		                            name="newPassword"
		                            placeholder="Enter your new password"
		                            autocomplete="new-password"
		                            required>
		                        <jsp:include page="/components/password_toggle.jsp">
        							<jsp:param name="inputId" value="newPassword" />
    							</jsp:include>
		                    </div>
		                </div>
		
		                <!-- Confirm New Password -->
		                <div class="input-group">
		                    <label for="confirmPassword">
		                        Confirm New Password
		                    </label>
		                    <div class="password-wrapper">
		                        <input
		                            type="password"
		                            id="confirmPassword"
		                            name="confirmPassword"
		                            placeholder="Re-enter your new password"
		                            autocomplete="new-password"
		                            required>
		                        <jsp:include page="/components/password_toggle.jsp">
        							<jsp:param name="inputId" value="confirmPassword" />
    							</jsp:include>
		                    </div>
		                </div>
		
		                <!-- Password Requirements -->
		                <div class="password-requirements">
		                    <p>
		                        Password requirements:
		                    </p>
		                    <ul>
		                        <li>
		                            New password must not be empty.
		                        </li>
		                        <li>
		                            New password must match the confirmation.
		                        </li>
		                    </ul>
		                </div>
		                
						<!-- Email Verifications -->
			            <jsp:include page="/components/email_verification.jsp">
						    <jsp:param name="inputId" value="verification-code" />
						    <jsp:param name="inputName" value="verification_code" />
						    <jsp:param name="sendFunction" value="sendVerificationCode" />
						</jsp:include>
						
		                <!-- Buttons -->
		                <div class="change-password-actions">
		                    <a
		                        href="${pageContext.request.contextPath}/settings.jsp"
		                        class="cancel-button">
		                        Cancel
		                    </a>
		                    <button
		                        type="submit"
		                        class="change-password-button">
		                        Change Password
		                    </button>
		                </div>
		            </form>
		        </div>
		    </main>
		
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>
		</section>
		
		<!-- Existing Password Toggle Script -->
		<script
		    src="${pageContext.request.contextPath}/scripts/password_toggle.js">
		</script>
		<script
		    src="${pageContext.request.contextPath}/scripts/change_password.js">
		</script>
	</body>
</html>