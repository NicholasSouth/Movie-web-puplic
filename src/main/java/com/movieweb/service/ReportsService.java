package com.movieweb.service;

import java.sql.SQLException;
import java.util.Objects;

import com.movieweb.DAO.CommentsDAO;
import com.movieweb.DAO.ReportsDAO;
import com.movieweb.DAO.UsersDAO;
import com.movieweb.model.Comments;
import com.movieweb.model.Reports;
import com.movieweb.model.Users;

public class ReportsService {
    private final ReportsDAO reportsDAO;
    private final CommentsDAO commentsDAO;
    private final UsersDAO usersDAO;
    private final EmailService emailService;
    public ReportsService() {
        this(
                new ReportsDAO(),
                new CommentsDAO(),
                new UsersDAO(),
                new EmailService()
        );
    }   
    public ReportsService(
            ReportsDAO reportsDAO,
            CommentsDAO commentsDAO,
            UsersDAO usersDAO,
            EmailService emailService) {
        this.reportsDAO = Objects.requireNonNull(reportsDAO);
        this.commentsDAO = Objects.requireNonNull(commentsDAO);
        this.usersDAO = Objects.requireNonNull(usersDAO);
        this.emailService = Objects.requireNonNull(emailService);
    }

    /* Checks whether the user has already reported the comment. */
    public boolean hasReported(int user_id, int comment_id)
            throws SQLException {
        requirePositive(user_id, "User ID");
        requirePositive(comment_id, "Comment ID");
        return reportsDAO.hasReported(user_id, comment_id);
    }

    /* Adds a report and sends an email notification. */
    public boolean addReport(int user_id, int comment_id)
            throws SQLException {
        requirePositive(user_id, "User ID");
        requirePositive(comment_id, "Comment ID");

        // Prevent the same user from reporting the same comment twice.
        if (reportsDAO.hasReported(user_id, comment_id)) {
            return false;
        }

        // Get the reported comment.
        Comments comment = commentsDAO.getCommentById(comment_id);
        if (comment == null) {
            return false;
        }

        // Get the comment author.
        Users commentUser = usersDAO.getUserById(comment.getUserId());

        // Get the person who reported the comment.
        Users reporter = usersDAO.getUserById(user_id);
        if (commentUser == null || reporter == null) {
            return false;
        }
        Reports report = new Reports();
        report.setUserId(user_id);
        report.setCommentId(comment_id);

        // Save report first.
        boolean success = reportsDAO.addReport(report);
        if (!success) {
            return false;
        }

        // Send notification only after the report is stored.
        emailService.sendReport(
                comment.getCreatedAt(),
                comment.getCommentText(),
                commentUser.getUsername(),
                reporter.getUsername()
        );
        return true;
    }

    private static void requirePositive(int value, String label) {
        if (value < 1) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
    }
}