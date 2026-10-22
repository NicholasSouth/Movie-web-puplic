<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta 
			name="viewport" 
			content="width=device-width, initial-scale=1.0">
		<title>Forgot Password - PhnetPhlyx</title>
		<link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main.css">
		<link 
			rel="stylesheet"
			href="${pageContext.request.contextPath}/styles/forget.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<main class="forgot-page">
		    <div class="forgot-container">
		    
		        <!-- Website Name -->
		        <a href="${pageContext.request.contextPath}/main.jsp" class="website-name">
		            PhnetPhlyx
		        </a>
		        <h1>
		            Forgot Password
		        </h1>
		        <p class="subtitle">
		            Reset your password using your email or phone number.
		        </p>
		
		        <!-- Server Error -->
		        <% 
		        	if (request.getAttribute("error") != null) { 
		        %>
			            <div class="error-message">
			                <%= request.getAttribute("error") %>
			            </div>
		        <% 
		        	} 
		        %>
				       
		        <form
		            class="forgot-form"
		            action="${pageContext.request.contextPath}/forgot-password"
		            method="post"
		            onsubmit="return validateForgotForm();">
		            
		            <!-- Account -->
		            <div class="input-group">
		                <label for="account">
		                    Username, Email or Phone Number
		                </label>
		                <input
		                    type="text"
		                    id="account"
		                    name="account"
		                    placeholder="Enter your username, email or phone number"
		                    required
		                >
		            </div>	
		            <p class="note">
		                We will send the verification code to the email
		                associated with your account.
		            </p>
		
		            <!-- New Password -->
		            <div class="input-group">
		                <label for="new-password">
		                    New Password
		                </label>
		                <div class="password-wrapper">
		                    <input
		                        type="password"
		                        id="new-password"
		                        name="new_password"
		                        placeholder="Enter your new password"
		                        required
		                    >
		                    <jsp:include page="/components/password_toggle.jsp">
        						<jsp:param name="inputId" value="new-password" />
    						</jsp:include>
		                </div>
		            </div>
		
		            <!-- Confirm Password -->
		            <div class="input-group">
		                <label for="confirm-password">
		                    Confirm New Password
		                </label>
		                <div class="password-wrapper">
		                    <input
		                        type="password"
		                        id="confirm-password"
		                        name="confirm_password"
		                        placeholder="Confirm your new password"
		                        required
		                    >
		                    <jsp:include page="/components/password_toggle.jsp">
        						<jsp:param name="inputId" value="confirm-password" />
    						</jsp:include>
		                </div>
		            </div>
		
		            <!-- Email Verifications -->
		            <jsp:include page="/components/email_verification.jsp">
					    <jsp:param name="inputId" value="verification-code" />
					    <jsp:param name="inputName" value="verification_code" />
					    <jsp:param name="sendFunction" value="sendVerificationCode" />
					</jsp:include>
		
		            <!-- Reset Password -->
		            <button type="submit" class="reset-button">
		                Reset Password
		            </button>
		        </form>
		
		        <!-- Back to Login -->
		        <p class="login-text">
		            Remember your password?
		            <a 
		            	href="${pageContext.request.contextPath}/log_in.jsp" 
		            	class="login-link">
		                Back to Login
		            </a>
		        </p>
		    </div>
		</main>
		
		<script
			src="${pageContext.request.contextPath}/scripts/forget.js">		    
		</script>		
	</body>
</html>