<%@ page import="com.movieweb.model.Users" %>
<%
    Users currentUser = (Users) session.getAttribute("user");
%>

<header class="Header">
    <!-- Website Name -->
    <a
        href="${pageContext.request.contextPath}/main.jsp"
        class="website-name">
        PhnetPhlyx
    </a>

    <!-- Search Bar -->
    <div class="search-container">
        <!-- Same search as the Movies page: sends ?search=... to /movies (press Enter) -->
        <form
            class="search-form"
            action="${pageContext.request.contextPath}/movies"
            method="get">
            <input
                type="search"
                name="search"
                maxlength="200"
                value="<%= com.movieweb.util.ViewUtils.h(request.getParameter("search")) %>"
                placeholder="Search by movie, actor, director, or author..."
                class="search-bar">
        </form>
    </div>

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
        <div
            class="menu-dropdown"
            id="menuDropdown">

           	<%
                /*Guest Mode*/
                if (currentUser == null) {
           	%>
                <!-- Guest Menu -->
                <div class="guest-menu">
                    <a
                        href="${pageContext.request.contextPath}/log_in.jsp"
                        class="menu-item login-item">
                        Log in / Register
                    </a>
                </div>
           	<%
                /*Logged-in Mode */
                } 
                else {
            %>
		                <!-- User Information -->
		                <a 
		                	href="${pageContext.request.contextPath}/user-profile" 
		                	class="user-menu-header user-profile-link">
		                    <!-- Avatar -->
		                    <div class="user-avatar">
		                        <%
		                            if (currentUser.getAvtPath() != null &&
		                                !currentUser.getAvtPath().isEmpty()) {
				                %>
			                            <img
			                                src="${pageContext.request.contextPath}/<%= currentUser.getAvtPath() %>"
			                                alt="<%= currentUser.getFullName() %>">
				                <%
		                            } 
		                            else {
				                %>
			                            <div class="default-avatar">
			                                <%= currentUser.getFullName()
			                                    .substring(0, 1)
			                                    .toUpperCase() %>
			                            </div>
				                <%
		                            }
		                        %>
		                    </div>
		
		                    <!-- Name and Role -->
		                    <div class="user-information">
		                        <div class="user-name">
		                            <%= currentUser.getFullName() %>
		                        </div>
		                        <div class="user-role">
		                            <%= currentUser.getRole() %>
		                        </div>
		                    </div>
		                </a>
		
		                <!-- Divider -->
		                <div class="menu-divider"></div>
		
		                <!-- Settings -->
		                <a
		                    href="${pageContext.request.contextPath}/settings.jsp"
		                    class="menu-item">
		                    <img 
		                    	src="${pageContext.request.contextPath}/pictures/assessments/setting.png"
								alt="Settings" 
								class="menu-item-icon">
		                    <span>
		                        Settings
		                    </span>
		                </a>
		
		                <!-- Log Out -->
		                <a
		                    href="${pageContext.request.contextPath}/logout"
		                    class="menu-item logout-item">
		                    <img 
		                    	src="${pageContext.request.contextPath}/pictures/assessments/log_out.png" 
		                    	alt="Log out" 
		                    	class="menu-item-icon">
		                    <span>
		                        Log out
		                    </span>
		                </a>
            <%
                }
            %>
        </div>
    </div>
</header>
<script
    src="${pageContext.request.contextPath}/scripts/header.js">
</script>