<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta 
			name="viewport" 
			content="width=device-width, initial-scale=1.0">
		<title>Register - PhnetPhlyx</title>
		<link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main.css">
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/register.css">
		<%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<main class="register-page">
		    <div class="register-container">
		    
		        <!-- Website Name -->
		        <a href="${pageContext.request.contextPath}/main.jsp" class="website-name">
		            PhnetPhlyx
		        </a>
		        <h1>
		            Create an Account
		        </h1>	
		        <p class="register-subtitle">
		            Join PhnetPhlyx and start booking your movies.
		        </p>
		
		        <!-- Server Error Message -->
		        <% 
		        	if (request.getAttribute("error") != null) { 
		        %>
			            <div class="error-message">
			                <%= request.getAttribute("error") %>
			            </div>
		        <% 
		        	} 
		        %>
		
		        <!-- Register Form -->
		        <form
		            class="register-form"
		            action="${pageContext.request.contextPath}/register"
		            method="post"
		            onsubmit="return validateRegisterForm();">
		            <!-- Username -->
		            <div class="input-group">
		                <label for="username">
		                    Username
		                </label>
		                <input
		                    type="text"
		                    id="username"
		                    name="username"
		                    placeholder="Enter your username"
		                    required
		                    maxlength="50"
		                >
		            </div>
		
		            <!-- Full Name -->
		            <div class="input-group">
		                <label for="full-name">
		                    Full Name
		                </label>
		                <input
		                    type="text"
		                    id="full-name"
		                    name="full_name"
		                    placeholder="Enter your full name"
		                    required
		                    maxlength="100"
		                >
		            </div>
		
		            <!-- Email -->
		            <div class="input-group">
		                <label for="email">
		                    Email
		                </label>
		                <input
		                    type="email"
		                    id="email"
		                    name="email"
		                    placeholder="Enter your email address"
		                    required
		                    maxlength="255"
		                >
		            </div>
		
		            <!-- Phone -->
		            <div class="input-group">
		                <label for="phone">
		                    Phone Number
		                </label>
		                <input
		                    type="tel"
		                    id="phone"
		                    name="phone"
		                    placeholder="Enter your phone number (optional)"
		                    maxlength="20"
		                >
		            </div>
		            <p class="contact-note">
		                Email is required for account verification.
		                Phone number is optional.
		            </p>
		
		            <!-- Password -->
		            <div class="input-group">
		                <label for="password">
		                    Password
		                </label>
		                <div class="password-wrapper">
		                    <input
		                        type="password"
		                        id="password"
		                        name="password"
		                        placeholder="Create a password"
		                        required
		                    >
		                    <jsp:include page="/components/password_toggle.jsp">
        							<jsp:param name="inputId" value="password" />
    						</jsp:include>
		                </div>
		            </div>
		
		            <!-- Confirm Password -->
		            <div class="input-group">
		                <label for="confirm-password">
		                    Confirm Password
		                </label>
		                <div class="password-wrapper">
		                    <input
		                        type="password"
		                        id="confirm-password"
		                        name="confirm_password"
		                        placeholder="Confirm your password"
		                        required
		                    >
		                    <jsp:include page="/components/password_toggle.jsp">
        							<jsp:param name="inputId" value="confirm-password" />
    						</jsp:include>
		                </div>
		            </div>
		
		            <!-- Manager Option -->
		            <label class="manager-option">
		                <input
		                    type="checkbox"
		                    id="manager"
		                    name="manager"
		                    value="true"
		                >
		                <span class="manager-text">
		                    <strong>
		                        I am a manager
		                    </strong>
		                    <small>
		                        Manager accounts require administrator approval.
		                    </small>
		                </span>
		            </label>
		
					<!-- Email Verifications -->
		            <jsp:include page="/components/email_verification.jsp">
					    <jsp:param name="inputId" value="verification-code" />
					    <jsp:param name="inputName" value="verification_code" />
					    <jsp:param name="sendFunction" value="sendVerificationCode" />
					</jsp:include>
		
		            <!-- Register Button -->
		            <button type="submit" class="register-button">
		                Create Account
		            </button>
		        </form>
		
		        <!-- Login -->
		        <p class="login-text">
		            Already have an account?
		            <a 
		            	href="${pageContext.request.contextPath}/log_in.jsp"
		             	class="login-link">
		                Back to log in
		            </a>
		        </p>
		    </div>
		</main>
		
		<script 
			src="${pageContext.request.contextPath}/scripts/register.js">
		</script>
	</body>
</html>