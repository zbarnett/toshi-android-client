package com.tokenbrowser.model.local;

public interface TokenEntity {
    String getTokenId();
    String getAvatar();
    String getDisplayName();
    Double getReputationScore();
    String getAbout();
    int getReviewCount();
}
