<%@ page import="com.movieweb.model.Users" %>
<%
    Users currentUser = (Users) session.getAttribute("user");
%>
<header class="Header">
    <!-- Website Name -->
    <a href="${pageContext.request.contextPath}/main.jsp" class="website-name">PhnetPhlyx</a>

    <!-- Menu -->
    <div class="header-menu">
        <!-- Menu Button -->
        <button 
	        type="button" 
	        class="menu-button" 
	        id="menuButton" 
	        aria-label="Open menu" 
	        aria-expanded="false">
            <span class="menu-dots">...</span>
        </button>

        <!-- Dropdown Menu -->
        <div class="menu-dropdown" id="menuDropdown">

			<!-- Fix the link to manager_profile -->
            <!-- User Information -->
            <a 
            	href="${pageContext.request.contextPath}/manager-profile" 
            	class="user-menu-header user-profile-link">

                <!-- Avatar -->
                <div class="user-avatar">
                    <%
                        if (currentUser.getAvtPath() != null && !currentUser.getAvtPath().isEmpty()) {
                    %>
	                        <img 
	                        	src="${pageContext.request.contextPath}/<%= currentUser.getAvtPath() %>" 
	                        	alt="<%= currentUser.getFullName() %>">
                    <%
                        } 
                        else {
                    %>
	                        <div class="default-avatar">
	                            <%= currentUser.getFullName().substring(0, 1).toUpperCase() %>
	                        </div>
                    <%
                        }
                    %>
                </div>

                <!-- Name and Role -->
                <div class="user-information">
                    <div class="user-name"><%= currentUser.getFullName() %></div>
                    <div class="user-role"><%= currentUser.getRole() %></div>
                </div>
            </a>

            <!-- Divider -->
            <div class="menu-divider"></div>

			<!-- Fix the link to manager_setting -->
            <!-- Settings -->
            <a href="${pageContext.request.contextPath}/manager_settings.jsp" class="menu-item">
                <img 
                	src="${pageContext.request.contextPath}/pictures/assessments/setting.png" 
                	alt="Settings" 
                	class="menu-item-icon">
                <span>Settings</span>
            </a>

            <!-- Log Out -->
            <a href="${pageContext.request.contextPath}/logout" class="menu-item logout-item">
                <img 
	                src="${pageContext.request.contextPath}/pictures/assessments/log_out.png" 
	                alt="Log out" 
	                class="menu-item-icon">
                <span>Log out</span>
            </a>
        </div>
    </div>
</header>
<script src="${pageContext.request.contextPath}/scripts/header.js"></script>