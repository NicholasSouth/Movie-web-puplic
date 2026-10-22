<%
    String currentAdminPage = (String) request.getAttribute("currentAdminPage");
    if (currentAdminPage == null) {
        currentAdminPage = "";
    }
%>

<aside class="AdminNavigation">
    <nav class="admin-navigation-nav">
        <a
            href="${pageContext.request.contextPath}/admin-dashboard"
            class="admin-nav-item <%= "dashboard".equals(currentAdminPage) ? "active" : "" %>">
            Dashboard
        </a>
        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                REPORTS
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-statistics"
                class="admin-nav-item <%= "statistics".equals(currentAdminPage) ? "active" : "" %>">
                Statistics
            </a>
        </div>

        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                THEATERS
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-theaters"
                class="admin-nav-item <%= "theaters".equals(currentAdminPage) ? "active" : "" %>">
                Theaters
            </a>
        </div>

        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                MOVIES
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-movies"
                class="admin-nav-item <%= "movies".equals(currentAdminPage) ? "active" : "" %>">
                Movies
            </a>
            <a
                href="${pageContext.request.contextPath}/admin-genres-tags"
                class="admin-nav-item <%= "genres-tags".equals(currentAdminPage) ? "active" : "" %>">
                Genres &amp; Tags
            </a>
        </div>

        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                ACCOUNTS
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-managers"
                class="admin-nav-item <%= "managers".equals(currentAdminPage) ? "active" : "" %>">
                Managers
            </a>
            <a
                href="${pageContext.request.contextPath}/admin-theater-requests"
                class="admin-nav-item <%= "theater-requests".equals(currentAdminPage) ? "active" : "" %>">
                Theater Requests
            </a>
            <a
                href="${pageContext.request.contextPath}/admin-users"
                class="admin-nav-item <%= "users".equals(currentAdminPage) ? "active" : "" %>">
                Users
            </a>
        </div>

        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                MODERATION
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-reports"
                class="admin-nav-item <%= "reports".equals(currentAdminPage) ? "active" : "" %>">
                Reported Comments
            </a>
        </div>
        <div class="admin-nav-section">
            <div class="admin-nav-section-title">
                SALES
            </div>
            <a
                href="${pageContext.request.contextPath}/admin-promotions"
                class="admin-nav-item <%= "promotions".equals(currentAdminPage) ? "active" : "" %>">
                Promotions
            </a>
            <a
                href="${pageContext.request.contextPath}/admin-refunds"
                class="admin-nav-item <%= "refunds".equals(currentAdminPage) ? "active" : "" %>">
                Refunds
            </a>
            <a
                href="${pageContext.request.contextPath}/admin-ads"
                class="admin-nav-item <%= "ads".equals(currentAdminPage) ? "active" : "" %>">
                Advertisements
            </a>
        </div>
    </nav>
</aside>
