<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta 
			name="viewport" 
			content="width=device-width, initial-scale=1.0">
		<title>Login - PhnetPhlyx</title>
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/log_in.css">
		<link 
			rel="stylesheet" 
			href="${pageContext.request.contextPath}/styles/main.css">
		<%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<main class="login-page">
		    <div class="login-container">
		    	
		        <!-- Website Name -->
		        <a href="${pageContext.request.contextPath}/main.jsp" class="website-name">
		            PhnetPhlyx
		        </a>
		        <p class="login-subtitle">
		            Sign in to continue to PhnetPhlyx
		        </p>
		
		        <!-- Error Message -->
		        <% 
		        	if (request.getAttribute("error") != null) { 
		        %>
			            <div class="error-message">
			                <%= request.getAttribute("error") %>
			            </div>
		        <% 
		        	}
		        %>
		
		        <!-- Login Form -->
		        <form 
			        class="login-form" 
			        action="${pageContext.request.contextPath}/login" 
			        method="post">
			        
		            <!-- Email or Phone Number -->
		            <div class="input-group">
		                <label for="login">
		                    Username, Email or Phone Number
		                </label>
		                <input
		                    type="text"
		                    id="login"
		                    name="login"
		                    placeholder="Enter your username, email or phone number"
		                    value="<%= request.getAttribute("login") != null ? request.getAttribute("login") : "" %>"
		                    required
		                >
		            </div>
		
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
		                        placeholder="Enter your password"
		                        required>
		                    <jsp:include page="/components/password_toggle.jsp">
        						<jsp:param name="inputId" value="password" />
    						</jsp:include>
		                </div>
		            </div>
		
		            <!-- Login Options -->
		            <div class="login-options">
		                <!-- Remember Me -->
		                <label class="remember-me">
		                    <input type="checkbox" name="remember_me" value="true">
		                    <span>
		                        Remember me
		                    </span>
		                </label>
		
		                <!-- Forgot Password -->
		                <a href="${pageContext.request.contextPath}/forget.jsp" class="forgot-password">
		                    Forgot password?
		                </a>
		            </div>
		
		            <!-- Login Button -->
		            <button type="submit" class="login-button">
		                Login
		            </button>
		        </form>
		
		        <!-- Register -->
		        <p class="register-text">
		            Don't have an account?
		            <a href="${pageContext.request.contextPath}/register.jsp" class="register-link">
		                Sign up
		            </a>
		        </p>
		    </div>
		</main>
	</body>
</html>