<%@ page import="com.movieweb.model.Users" %>

<%
    Users currentUser = (Users) session.getAttribute("user");
    if (currentUser == null)
    {
        response.sendRedirect(request.getContextPath() + "/log_in.jsp");
        return;
    }
    if (currentUser == null || !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
        response.sendRedirect(request.getContextPath() + "/main.jsp");
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
	        Change Information
	    </title>
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/main_manager.css">
	    <link
	        rel="stylesheet"
	        href="${pageContext.request.contextPath}/styles/manager_information_change.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>

	<body>
		<div class="Layout2">
		    <!-- Header -->
		    <jsp:include page="/components/header_manager.jsp" />
		
		    <!-- Left Sidebar -->
		    <%@ include file="components/manager_navigation.jsp" %>
		
		    <!-- Main Body -->
		    <main class="ManagerMain information-change-page">
		        <section class="information-change-section">
		            <div class="section-header">
		                <h1>
		                    Change Information
		                </h1>
		                <p class="section-description">
		                    Update your personal information.
		                </p>
		            </div>
		            <form
		                action="${pageContext.request.contextPath}/manager-information-change"
		                method="post"
		                enctype="multipart/form-data"
		                class="information-change-form">
		                <!-- Avatar -->
						<div class="form-group">
						    <label for="avatar">
						        Avatar
						    </label>
						    <input
						        type="file"
						        id="avatar"
						        name="avatar"
						        class="file-input-hidden"
						        accept=".jpg,.jpeg,.png,.webp">
						    <!-- Custom styled choosing filebutton acting as a label -->
						    <label for="avatar" class="custom-file-button">
						        <img 
							        src="${pageContext.request.contextPath}/pictures/assessments/upload.png" 
							        alt="Upload Icon" 
							        class="upload-icon">
						        Choose File
						    </label>
						    <p class="form-hint">
						        JPG, PNG, or WebP. Maximum size: 5 MB.
						    </p>
						</div>
						
						<!-- Banner -->
						<div class="form-group">
						    <label for="banner">
						        Banner
						    </label>
						    <input
						        type="file"
						        id="banner"
						        name="banner"
						        class="file-input-hidden"
						        accept=".jpg,.jpeg,.png,.webp">
						    <label for="banner" class="custom-file-button">
						        <img 
							        src="${pageContext.request.contextPath}/pictures/assessments/upload.png" 
							        alt="Upload Icon" 
							        class="upload-icon">
						        Choose File
						    </label>
						    <p class="form-hint">
						        JPG, PNG, or WebP. Maximum size: 10 MB.
						    </p>
						</div>
						
		                <!-- Username -->
		                <div class="form-group">
		                    <label for="username">
		                        Username
		                    </label>
		                    <input
		                        type="text"
		                        id="username"
		                        name="username"
		                        value="<%= currentUser.getUsername() %>"
		                        readonly>
		                    <p class="form-hint">
		                        Your username cannot be changed.
		                    </p>
		                </div>
		
		
		                <!-- Full Name -->
		                <div class="form-group">
		                    <label for="fullName">
		                        Full Name
		                    </label>
		                    <input
		                        type="text"
		                        id="fullName"
		                        name="fullName"
		                        value="<%= currentUser.getFullName() != null
		                            ? currentUser.getFullName()
		                            : "" %>"
		                        required>
		                </div>
		
		                <!-- Email -->
		                <div class="form-group">
		                    <label for="email">
		                        Email
		                    </label>
		                    <input
		                        type="email"
		                        id="email"
		                        name="email"
		                        value="<%= currentUser.getEmail() != null
		                            ? currentUser.getEmail()
		                            : "" %>"
		                        required>
		                </div>
		
		                <!-- Phone -->
		                <div class="form-group">
		                    <label for="phone">
		                        Phone
		                    </label>
		                    <input
		                        type="text"
		                        id="phone"
		                        name="phone"
		                        value="<%= currentUser.getPhone() != null
		                            ? currentUser.getPhone()
		                            : "" %>">
		                    <p class="form-hint">
		                        Phone number is optional.
		                    </p>
		                </div>
		
		                <!-- Error Message -->
		                <%
		                    String errorMessage = (String) request.getAttribute("errorMessage");
		                    if (errorMessage != null)
		                    {
		                %>
		                    <div class="form-error">
		                        <%= errorMessage %>
		                    </div>
		                <%
		                    }
		                %>
		
		                <!-- Save/Cancel -->
		                <div class="form-actions">
		                    <button
		                        type="submit"
		                        class="save-button">
		                        Save Changes
		                    </button>
		                    <a
		                        href="${pageContext.request.contextPath}/manager-profile"
		                        class="cancel-button">
		                        Cancel
		                    </a>
		                </div>
		            </form>
		        </section>
		    </main>
		</div>
	</body>
</html>