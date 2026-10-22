<%
    /* Settings is for logged-in accounts only. Guests are sent to the login page. */
    if (session.getAttribute("user") == null)
    {
        response.sendRedirect(request.getContextPath() + "/log_in.jsp");
        return;
    }
%>
<%
    String success = (String) request.getAttribute("success");
    if (success != null)
    {
%>
    <div class="success-message">
        <%= success %>
    </div>
<%
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
	        Settings - PhnetPhlyx
	    </title>
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main.css">
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/settings.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<section class="Layout1">
		    <!-- Header -->
		    <%@ include file="components/header.jsp" %>
		
		    <!-- Left Sidebar -->
		    <%@ include file="components/left_sidebar.jsp" %>
		
		    <!-- Main Content -->
		    <main class="MainBody">
		        <div class="settings-container">
		            <div class="settings-header">
		                <h2>
		                    Settings
		                </h2>
		                <p>
		                    Manage your account preferences and security.
		                </p>
		            </div>
		
		            <!-- Security -->
		            <section class="settings-section">
		                <h3 class="settings-section-title">
		                    Security
		                </h3>
		                <div class="settings-card">
		                    <div class="settings-card-information">
		                        <h4>
		                            Password
		                        </h4>
		                        <p>
		                            Change your password to keep your account secure.
		                        </p>
		                    </div>
		                    <div class="settings-card-action">
		                        <a
		                            href="${pageContext.request.contextPath}/change_password.jsp"
		                            class="settings-button">
		                            Change Password
		                        </a>
		                    </div>
		                </div>
		            </section>
		
		            <!-- Appearance -->
		            <section class="settings-section">
		                <h3 class="settings-section-title">
		                    Appearance
		                </h3>
		                <div class="settings-card">
		                    <div class="settings-card-information">
		                        <h4>
		                            Theme
		                        </h4>
		                        <p>
		                            Choose how PhnetPhlyx looks.
		                        </p>
		                    </div>
		                    <div class="theme-options" data-theme-user="<%= ((com.movieweb.model.Users) session.getAttribute("user")).getUserId() %>">
		                        <label class="theme-option">
		                            <input
		                                type="radio"
		                                name="theme"
		                                value="dark"
		                                checked>
		                            <span>
		                                Dark
		                            </span>
		                        </label>
		                        <label class="theme-option">
		                            <input
		                                type="radio"
		                                name="theme"
		                                value="light">
		                            <span>
		                                Light
		                            </span>
		                        </label>
		                    </div>
		                </div>
		            </section>
		
					<!-- Payment -->
		            <section class="settings-section">
		                <h3 class="settings-section-title">
		                    Payment
		                </h3>
		                <div class="settings-card">
		                    <div class="settings-card-information">
		                        <h4>
		                            Payment Methods
		                        </h4>
		                        <p>
		                            Manage your saved payment methods.
		                        </p>
		                    </div>
		                    <div class="settings-card-action">
							    <a href="${pageContext.request.contextPath}/payment-method"
							       class="settings-button">
							        Payment Methods
							    </a>
							</div>
		                </div>
		            </section>
		
		            <!-- Danger Zone -->
		            <section class="settings-section danger-zone">
		                <h3 class="settings-section-title">
		                    Danger Zone
		                </h3>
		                <div class="settings-card danger-card">
		                    <div class="settings-card-information">
		                        <h4>
		                            Delete Account
		                        </h4>
		                        <p>
		                            Permanently deactivate your PhnetPhlyx account.
		                        </p>
		                    </div>
		                    <div class="settings-card-action">
		                        <button
								    type="button"
								    class="settings-button delete-button"
								    onclick="confirmDeleteAccount()">								
								    Delete Account
								</button>
		                    </div>
		                </div>
		            </section>
		        </div>
		    </main>
		
		    <!-- Right Sidebar -->
		    <%@ include file="components/right_sidebar.jsp" %>
		
		    <!-- Footer -->
		    <%@ include file="components/footer.jsp" %>		
		</section>
		
		<script
    		src="<%= request.getContextPath() %>/scripts/delete_account.js">
		</script>
		<script src="<%= request.getContextPath() %>/scripts/theme.js"></script>
	</body>
</html>