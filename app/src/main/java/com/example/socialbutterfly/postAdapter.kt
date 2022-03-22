package com.example.socialbutterfly

import android.view.LayoutInflater
import android.view.OnReceiveContentListener
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialbutterfly.model.post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


//we can also use RecyclerViewAdapter but FirestoreRecycler Adapter give us some inbuilt functionalities
//i.e. we dont have to bring data (which was generally array) nor we need to implement getItemCount fun etc.

class postAdapter(options: FirestoreRecyclerOptions<post>, val listener: postAdapterInterface) : FirestoreRecyclerAdapter<post, postAdapter.PostViewHolder>(
    options
) {

    class PostViewHolder (itemView : View) : RecyclerView.ViewHolder (itemView){
        val userImage = itemView.findViewById<ImageView>(R.id.userImage)
        val userName = itemView.findViewById<TextView>(R.id.userName)
        val CreatedAt = itemView.findViewById<TextView>(R.id.CreatedAt)
        val postTitle = itemView.findViewById<TextView>(R.id.postTitle)
        val likeButton = itemView.findViewById<ImageView>(R.id.likeButton)
        val likeCount = itemView.findViewById<TextView>(R.id.likeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val obj = inflater.inflate(R.layout.item_post, parent, false)
        val viewHolder = PostViewHolder(obj)
        viewHolder.likeButton.setOnClickListener {
            //we need to pass userId to our onLikeCklicked fun but we dont have any referance of "post"
            //so we use snapshots..which is available inside fireStore
            listener.onLikeClicked(snapshots.getSnapshot(viewHolder.absoluteAdapterPosition).id)
            //.id is document ki id jo humne as a user id set ki hui hai
            //hover the mouse pointer over it to see
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: post) {
        holder.userName.text = model.createdBy.UserName
        holder.postTitle.text = model.text
        Glide.with(holder.userImage.context).load(model.createdBy.ImageUrl).circleCrop().into(holder.userImage)
        holder.likeCount.text = model.likedBy.size.toString()
        holder.CreatedAt.text = Utils.getTimeAgo(model.createdAt)

        //changing heart shape like button to red/white based on click
        //we are handeling clickListener in main activity , then it is calling function in PostDao
        //which makes changes in our FireBase to which our adapter is listening so according to that
        //change we will update our like icon

        val auth = Firebase.auth
        val currentUserId = auth.currentUser!!.uid
        val isLikedBy = model.likedBy.contains(currentUserId)

        if (isLikedBy){
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.context, R.drawable.ic_baseline_favorite_24))
        }
        else{
            holder.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.likeButton.context, R.drawable.ic_baseline_favorite_border_24))
        }
    }
}

interface postAdapterInterface{
    fun onLikeClicked (postId:String)
}