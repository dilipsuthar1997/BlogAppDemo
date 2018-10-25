package com.techflow.blogappdemo.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.techflow.blogappdemo.R;
import com.techflow.blogappdemo.model.BlogPost;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class AdapterBlogRecycler extends RecyclerView.Adapter<AdapterBlogRecycler.ViewHolder> {

    private Context context;
    private List<BlogPost> items;

    //Firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;


    public AdapterBlogRecycler(List<BlogPost> items) {

        this.items = items;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog_list, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String current_user_id = mAuth.getCurrentUser().getUid();
        final String blog_post_id = items.get(position).blog_post_id;

        String desc_data = items.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = items.get(position).getImage_url();
        String thumb_url = items.get(position).getThumb_url();
        holder.setBlogImage(image_url, thumb_url);

        String user_id = items.get(position).getUser_id();

        //User data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String blog_user_name = task.getResult().get("name").toString();
                    String blog_user_image = task.getResult().get("image").toString();

                    holder.setBlogUserData(blog_user_name, blog_user_image);

                }

            }
        });

        //Get Date
        /* Error: Null object reference when upload new post
         */
        long milliseconds = items.get(position).getTime_stamp().getTime();
        String date = android.text.format.DateFormat.format("dd/MM/yy", new Date(milliseconds)).toString();
        holder.setBlogDate(date);

        //Get Likes Count
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    int count = queryDocumentSnapshots.size();
                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });

        //Get Likes --- check for like btn
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                /* Error: when user goes to SignOut from the App
                 */
                if (documentSnapshot.exists()) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_red));

                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_grey));

                }

            }
        });

        //Likes feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("time_stamp", FieldValue.serverTimestamp());

                            //Create another collection for each collection for Likes
                            //Method - 1
                            //firebaseFirestore.collection("Posts").document(blog_post_id).collection("Likes").document(current_user_id).set(likesMap);

                            //Method - 2
                            firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(current_user_id).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(current_user_id).delete();

                        }

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private TextView blogDesc;
        private TextView blogDate;
        private ImageView blogImage;
        private TextView blogUserName;
        private ImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            blogLikeBtn = view.findViewById(R.id.blog_like_btn);

        }

        public void setDescText(String desc) {
            blogDesc = view.findViewById(R.id.blog_desc);
            blogDesc.setText(desc);
        }

        public void setBlogImage(String image_url, String thumb_url) {
            blogImage = view.findViewById(R.id.blog_image);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image_view);

            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(image_url)
                    .thumbnail(Glide.with(context).load(thumb_url))
                    .into(blogImage);
        }

        public void setBlogDate(String date) {
            blogDate = view.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void setBlogUserData(String name, String profile_url) {
            blogUserName = view.findViewById(R.id.blog_user_name);
            blogUserImage = view.findViewById(R.id.blog_user_image);

            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.default_image);

            blogUserName.setText(name);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(profile_url).into(blogUserImage);
        }

        public void updateLikesCount(int count) {
            blogLikeCount = view.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");
        }
    }

}
