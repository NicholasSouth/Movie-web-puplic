package com.movieweb.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Map;

import com.movieweb.model.Users;
import com.movieweb.DAO.CommentsDAO;
import com.movieweb.model.Comments;

public class CommentsService {
    private final CommentsDAO commentsDAO;
    public CommentsService() {
        this(new CommentsDAO());
    }
    public CommentsService(CommentsDAO commentsDAO) {
        this.commentsDAO = Objects.requireNonNull(commentsDAO);
    }

    /* Adds a comment to a movie. */
    public boolean addComment(int user_id, int movie_id, String comment_text)
            throws SQLException {
        requirePositive(user_id, "User ID");
        requirePositive(movie_id, "Movie ID");
        if (comment_text == null || comment_text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }
        Comments comment = new Comments();
        comment.setUserId(user_id);
        comment.setMovieId(movie_id);
        comment.setCommentText(comment_text.trim());
        return commentsDAO.addComment(comment);
    }

    /* Returns all comments for a movie, newest first. */
    public List<Comments> getCommentsByMovieId(int movie_id)
            throws SQLException {
        requirePositive(movie_id, "Movie ID");
        return commentsDAO.getCommentsByMovieId(movie_id);
    }
    private static void requirePositive(int value, String label) {
        if (value < 1) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
    }
    
    public Map<Integer, Users> getCommentUsersByMovieId(int movie_id)
            throws SQLException {
        requirePositive(movie_id, "Movie ID");
        return commentsDAO.getCommentUsersByMovieId(movie_id);
    }
}