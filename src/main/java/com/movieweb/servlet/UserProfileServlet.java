package com.movieweb.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Movies;
import com.movieweb.model.Users;
import com.movieweb.service.FavouriteMoviesService;
import com.movieweb.DAO.Booking_showtimesDAO;

@WebServlet("/user-profile")
public class UserProfileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private FavouriteMoviesService favouriteMoviesService;
    private UsersDAO usersDAO;
    private Booking_showtimesDAO bookingShowtimesDAO;

    @Override
    public void init()
            throws ServletException
    {
        favouriteMoviesService = new FavouriteMoviesService();
        usersDAO = new UsersDAO();
        bookingShowtimesDAO = new Booking_showtimesDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        /* User must be logged in. */
        if (session == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }
        Users currentUser = (Users) session.getAttribute("user");
        if (currentUser == null)
        {
            response.sendRedirect(request.getContextPath() + "/log_in.jsp");
            return;
        }

        /*Check whether a specific user profile was requested.*/
        String userIdParameter = request.getParameter("user_id");
        Users profileUser;
        if (userIdParameter == null
            || userIdParameter.trim().isEmpty())
        {
            /*No user_id means the logged-in user's own profile.*/
            profileUser = currentUser;
        }
        else
        {
            try
            {
                int userId = Integer.parseInt(userIdParameter);
                profileUser = usersDAO.getUserById(userId);
                if (profileUser == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found.");
                    return;
                }
            }
            catch (NumberFormatException e)
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID.");
                return;
            }
        }

        /*Check whether this is the logged-in user's own profile.*/
        boolean isOwnProfile = currentUser.getUserId() == profileUser.getUserId();

        /* Get favourite movies, booking history.
         * Own profile:
         * Always allow the owner to see their favourites, bookings.
         * Other profile:
         * Only load favourites when the user has made them public, bookings are always hidden .
         */
        List<Movies> favouriteMovies = null;
        if (isOwnProfile
            || profileUser.getFavouriteMoviesVisibility())
        {
            favouriteMovies = favouriteMoviesService.getFavouriteMovies(profileUser.getUserId());
        }
        List<Map<String, Object>> bookingHistory = null;

        if (isOwnProfile) {
            bookingHistory = bookingShowtimesDAO.getBookingHistoryByUserId(profileUser.getUserId());
        }
        
        /*Give the profile information to user_profile.jsp.*/
        request.setAttribute("profileUser", profileUser);
        request.setAttribute("loggedInUser", currentUser);
        request.setAttribute("isOwnProfile", isOwnProfile);
        request.setAttribute("movieGridMovies", favouriteMovies);
        request.setAttribute("bookingHistory", bookingHistory);
        request.getRequestDispatcher("/user_profile.jsp").forward(request, response);
    }
}