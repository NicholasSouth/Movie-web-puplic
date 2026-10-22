<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Users" %>
<%@ page import="com.movieweb.model.Theaters" %>
<%@ page import="com.movieweb.util.ViewUtils" %>
<%
    /* Always load the profile through the servlet. */
    if (request.getDispatcherType() != javax.servlet.DispatcherType.FORWARD
        || request.getAttribute("profileUser") == null
        || request.getAttribute("assignedTheaters") == null) {
        response.sendRedirect(request.getContextPath() + "/manager-profile");
        return;
    }

    Users loggedInUser = (Users) session.getAttribute("user");
    Users profileUser = (Users) request.getAttribute("profileUser");
    List<Theaters> assignedTheaters = (List<Theaters>) request.getAttribute("assignedTheaters");
    boolean theaterLoadError = Boolean.TRUE.equals(request.getAttribute("theaterLoadError"));

    /* Only the logged-in manager can view this profile. */
    if (loggedInUser == null
        || !"MANAGER".equalsIgnoreCase(loggedInUser.getRole())
        || profileUser.getUserId() != loggedInUser.getUserId()) {
        response.sendRedirect(request.getContextPath() + "/main.jsp");
        return;
    }
    String fullName = ViewUtils.text(profileUser.getFullName(), profileUser.getUsername());
    String avatarLetter = ViewUtils.hasText(fullName) ? fullName.trim().substring(0, 1).toUpperCase() : "?";
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
	<head>
	    <meta charset="UTF-8">
	    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title><%= ViewUtils.h(fullName) %> - Manager Profile</title>
	
	    <!-- Shared manager layout, header and navigation styles -->
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/main_manager.css">
	
	    <!-- Existing profile and theater styles -->
	    <link rel="stylesheet" href="${pageContext.request.contextPath}/styles/manager_profile.css">
	    <%@ include file="/components/theme.jsp" %>
	</head>
	<body>
		<section class="Layout2">
		
		    <!-- Shared Manager Header -->
		    <jsp:include page="/components/header_manager.jsp" />
		
		    <!-- Shared Manager Navigation -->
		    <%@ include file="components/manager_navigation.jsp" %>
		
		    <!-- Main Body -->
		    <main class="ManagerMain manager-profile-main">
		
		        <!-- Profile Header -->
		        <section class="profile-header">
		
		            <!-- Banner -->
		            <div class="profile-banner">
		                <%
		                    if (ViewUtils.hasText(profileUser.getBannerPath())) {
		                %>
				                <img src="<%= ViewUtils.h(ViewUtils.asset(contextPath, profileUser.getBannerPath())) %>" alt="Profile banner">
		                <%
		                    } 
		                    else {
		                %>
				                <div class="default-banner"></div>
		                <%
		                    }
		                %>
		            </div>
		
		            <!-- Profile Information -->
		            <div class="profile-header-info">
		
		                <!-- Avatar -->
		                <div class="profile-avatar">
		                    <%
		                        if (ViewUtils.hasText(profileUser.getAvtPath())) {
		                    %>
				                    <img src="<%= ViewUtils.h(ViewUtils.asset(contextPath, profileUser.getAvtPath())) %>" alt="<%= ViewUtils.h(fullName) %>">
		                    <%
		                        } 
		                        else {
		                    %>
				                    <div class="profile-default-avatar">
				                        <%= ViewUtils.h(avatarLetter) %>
				                    </div>
		                    <%
		                        }
		                    %>
		                </div>
		
		                <!-- Manager Identity and Contact Information -->
		                <div class="profile-identity">
		                    <h1><%= ViewUtils.h(fullName) %></h1>
		                    <p class="profile-username">
		                        @<%= ViewUtils.h(profileUser.getUsername()) %>
		                    </p>
		                    <span class="profile-role">Theater Manager</span>
		
		                    <!-- Visible on the manager's own profile -->
		                    <div class="manager-contact">
		                        <p>
		                            <span>Email:</span>
		                            <%= ViewUtils.h(ViewUtils.text(profileUser.getEmail(), "Not provided")) %>
		                        </p>
		                        <p>
		                            <span>Phone:</span>
		                            <%= ViewUtils.h(ViewUtils.text(profileUser.getPhone(), "Not provided")) %>
		                        </p>
		                    </div>
		                </div>
		
		                <!-- Edit Profile -->
		                <div class="profile-actions">
		                    <a href="${pageContext.request.contextPath}/manager-information-change" class="profile-settings-button">
		                        <img src="${pageContext.request.contextPath}/pictures/assessments/setting.png" alt="">
		                        <span>Edit Profile</span>
		                    </a>
		                </div>
		
		            </div>
		        </section>
		
		        <!-- Assigned Theaters -->
		        <section id="assigned-theaters" class="profile-section">
		            <div class="section-header">
		                <div>
		                    <h2>My Theaters</h2>
		                    <p class="section-description">
		                        Active theaters assigned to you by the administrator.
		                    </p>
		                </div>
		
		                <div class="section-header-actions">
		                    <%
		                        if (!theaterLoadError) {
		                    %>
					                <span class="private-label">
					                    <%= assignedTheaters.size() %> assigned
					                </span>
		                    <%
		                        }
		                    %>
		                    <a href="${pageContext.request.contextPath}/manager-theater-requests" class="theater-button">
		                        Request Assign / Remove
		                    </a>
		                </div>
		            </div>
		
		            <!-- Theater Loading Error -->
		            <%
		                if (theaterLoadError) {
		            %>
				            <div class="manager-load-error" role="alert">
				                <p>
				                    Unable to load all of your theaters.
				                    Please try again.
				                </p>
				                <a href="${pageContext.request.contextPath}/manager-profile" class="theater-button">
				                    Try Again
				                </a>
				            </div>
		            <%
		                }
		            %>
		
		            <!-- Theater Cards -->
		            <%
		                if (!assignedTheaters.isEmpty()) {
		            %>
				            <div class="theater-grid">
				                <%
				                    for (Theaters theater : assignedTheaters) {
				                %>
						                <article class="theater-card">
						
						                    <!-- Theater Image -->
						                    <div class="theater-image">
						                        <%
						                            if (ViewUtils.hasText(theater.getTheater_image_path())) {
						                        %>
								                        <img src="<%= ViewUtils.h(ViewUtils.asset(contextPath, theater.getTheater_image_path())) %>" alt="<%= ViewUtils.h(theater.getTheater_name()) %>" loading="lazy">
						                        <%
						                            } 
						                            else {
						                        %>
								                        <div class="manager-theater-placeholder">
								                            PhnetPhlyx
								                        </div>
						                        <%
						                            }
						                        %>
						                    </div>
						
						                    <!-- Theater Information -->
						                    <div class="theater-content">
						                        <h3>
						                            <%= ViewUtils.h(theater.getTheater_name()) %>
						                        </h3>
						                        <p class="theater-address">
						                            <%= ViewUtils.h(theater.getTheater_address()) %>
						                        </p>
						                        <div class="theater-info">
						                            <%
						                                if (theater.getOpen_time() != null && theater.getClosing_time() != null) {
						                            %>
								                            <span>
								                                Open:
								                                <%= theater.getOpen_time().toString().substring(0, 5) %>
								                                -
								                                <%= theater.getClosing_time().toString().substring(0, 5) %>
								                            </span>
						                            <%
						                                } 
						                                else {
						                            %>
								                            <span>
								                                Opening hours not provided.
								                            </span>
						                            <%
						                                }
						                            %>
						                        </div>
						                        <a href="${pageContext.request.contextPath}/manager-theater?theaterId=<%= theater.getTheater_id() %>" class="theater-button">
						                            View Theater
						                        </a>
						                    </div>
						
						                </article>
				                <%
				                    }
				                %>
				            </div>
		            <%
		                } 
		                else if (!theaterLoadError) {
		            %>
				            <div class="booking-placeholder">
				                <h3>No theaters assigned yet</h3>
				                <p>
				                    Contact the administrator to have a theater assigned
				                    to your account, or send a request.
				                </p>
				                <a href="${pageContext.request.contextPath}/manager-theater-requests" class="theater-button">
				                    Request a Theater
				                </a>
				            </div>
		            <%
		                }
		            %>
		        </section>
		    </main>
		
		</section>
	</body>
</html>