package com.movieweb.model;

import java.sql.Timestamp;

public class Remember_me
{
    private int token_id;
    private int user_id;
    private String token_hash;
    private Timestamp expires_at;
    private Timestamp created_at;

    public Remember_me(){   	
    }
    public Remember_me(
            int token_id,
            int user_id,
            String token_hash,
            Timestamp expires_at,
            Timestamp created_at)
    {
        this.token_id = token_id;
        this.user_id = user_id;
        this.token_hash = token_hash;
        this.expires_at = expires_at;
        this.created_at = created_at;
    }
    public int getTokenId()
    {
        return token_id;
    }
    public void setTokenId(int token_id)
    {
        this.token_id = token_id;
    }
    public int getUserId()
    {
        return user_id;
    }
    public void setUserId(int user_id)
    {
        this.user_id = user_id;
    }
    public String getTokenHash()
    {
        return token_hash;
    }
    public void setTokenHash(String token_hash)
    {
        this.token_hash = token_hash;
    }
    public Timestamp getExpiresAt()
    {
        return expires_at;
    }
    public void setExpiresAt(Timestamp expires_at)
    {
        this.expires_at = expires_at;
    }
    public Timestamp getCreatedAt()
    {
        return created_at;
    }
    public void setCreatedAt(Timestamp created_at)
    {
        this.created_at = created_at;
    }
}