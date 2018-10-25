package com.techflow.blogappdemo.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.techflow.blogappdemo.R;
import com.techflow.blogappdemo.adapter.AdapterBlogRecycler;
import com.techflow.blogappdemo.model.BlogPost;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Context context;

    private RecyclerView recyclerView;
    private List<BlogPost> items_blog_list;
    private AdapterBlogRecycler adapter;

    private Boolean isFirstPageFirstLoad = true;

    //Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        context = this.getActivity();

        findViewById(view);
        initComponent();

        return view;
    }

    public void findViewById(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        items_blog_list = new ArrayList<>();
    }

    public void initComponent() {

        if (mAuth.getCurrentUser() != null) {

            adapter = new AdapterBlogRecycler(items_blog_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);

            //RecyclerView Scroll method
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {

                        String desc = lastVisible.getString("desc");
                        //Toast.makeText(context, "Loading..", Toast.LENGTH_SHORT).show();

                        //call method for load more posts after 3
                        loadMorePost();

                    }

                }
            });

            //Make query for retrieve data from SERVER---
            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("time_stamp", Query.Direction.DESCENDING)
                    .limit(3);

            //Main part to retrieve data from SERVER---
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoad) {
                        // Get the last visible document
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    }

                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                //Get blog_post id with Document
                                String blog_post_id = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_post_id);

                                if (isFirstPageFirstLoad) {

                                    items_blog_list.add(blogPost);

                                } else {

                                    items_blog_list.add(0, blogPost);

                                }

                                adapter.notifyDataSetChanged();

                            }

                        }
                    }

                    isFirstPageFirstLoad = false;

                }
            });

        }

    }

    public void loadMorePost() {

        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("time_stamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        //Main part to retrieve data from SERVER---
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            //Get blog_post id with Document
                            String blog_post_id = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blog_post_id);
                            items_blog_list.add(blogPost);

                            adapter.notifyDataSetChanged();

                        }

                    }

                } else {

                    Toast.makeText(context, "No more posts.", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

}
