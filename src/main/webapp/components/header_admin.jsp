<%@ page import="com.movieweb.model.Users" %>
<%
    Users currentUser = (Users) session.getAttribute("user");
%>
<header class="Header">
    <!-- Website Name -->
    <a href="${pageContext.request.contextPath}/admin-dashboard" class="website-name">PhnetPhlyx</a>

    <!-- Menu -->
    <div class="header-menu">
        <button
            type="button"
            class="menu-button"
            id="menuButton"
            aria-label="Open menu"
            aria-expanded="false">
            <span class="menu-dots">...</span>
        </button>

        <div class="menu-dropdown" id="menuDropdown">
            <!-- User information: links to the admin profile page -->
            <a href="${pageContext.request.contextPath}/admin-profile" class="user-menu-header user-profile-link">
                <div class="user-avatar">
                    <%
                        if (currentUser.getAvtPath() != null && !currentUser.getAvtPath().isEmpty()) {
                    %>
                            <img
                                src="${pageContext.request.contextPath}/<%= currentUser.getAvtPath() %>"
                                alt="<%= currentUser.getFullName() %>">
                    <%
                        } else {
                    %>
                            <div class="default-avatar">
                                <%= currentUser.getFullName().substring(0, 1).toUpperCase() %>
                            </div>
                    <%
                        }
                    %>
                </div>
                <div class="user-information">
                    <div class="user-name"><%= currentUser.getFullName() %></div>
                    <div class="user-role"><%= currentUser.getRole() %></div>
                </div>
            </a>

            <div class="menu-divider"></div>

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
