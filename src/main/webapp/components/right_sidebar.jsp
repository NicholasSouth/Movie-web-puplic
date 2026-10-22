<%@ page import="java.util.List" %>
<%@ page import="com.movieweb.model.Ads" %>
<%@ page import="com.movieweb.DAO.AdsDAO" %>
<aside class="RightSidebar">
<%
    List<Ads> advertisements = (List<Ads>) request.getAttribute("advertisements");
    if (advertisements == null) {
        // No servlet sets this attribute for the customer pages, so the sidebar loads the active ads itself.
        // AdsDAO logs SQL errors and returns an empty list, so a database problem never breaks the page.
        advertisements = new AdsDAO().getActiveAdvertisements();
    }
    for (Ads ad : advertisements) {
        String adHref = ad.getLink();
        // Only http(s) links are rendered as links, and everything is HTML-escaped.
        boolean adSafeLink = adHref != null
                && (adHref.startsWith("http://") || adHref.startsWith("https://"));
        String adImage = request.getContextPath() + "/"
                + com.movieweb.util.ViewUtils.h(ad.getImage_path());
        String adName = com.movieweb.util.ViewUtils.h(ad.getAd_name());
        if (adSafeLink) {
%>
            <a href="<%= com.movieweb.util.ViewUtils.h(adHref) %>" class="ad-link" target="_blank" rel="noopener noreferrer">
                <img src="<%= adImage %>" alt="<%= adName %>" class="ad">
            </a>
	<%
        } else {
	%>
            <img src="<%= adImage %>" alt="<%= adName %>" class="ad">
<%
        }
    }
%>
</aside>
