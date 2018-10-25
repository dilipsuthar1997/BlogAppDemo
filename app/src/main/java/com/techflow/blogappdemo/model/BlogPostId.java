package com.techflow.blogappdemo.model;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BlogPostId {

    @Exclude
    public String blog_post_id;

    public <T extends BlogPostId> T withId(@NonNull final String id) {
        this.blog_post_id = id;

        return (T) this;
    }

}
