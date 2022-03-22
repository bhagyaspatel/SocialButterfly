package com.example.socialbutterfly.model.daos

import com.example.socialbutterfly.model.post
import com.example.socialbutterfly.model.user
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {

    val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("post")
    val auth = Firebase.auth

    fun addPost(text: String) {
        val currentUser = auth.currentUser!!.uid

        //agar somehow koi user bina log in kiye postAdd karne ki kosis kar raha hai to hum chaege
        //ki uss case me app crash ho jaye ..that's why we used !! ..currentuser!! ..i.e. by !! we
        //are assuring that currentUser is pakka not null,,and if it comes out be null it will
        //result in app crash

        GlobalScope.launch {
            val userDao = userDao()
            val user = userDao.getUserById (currentUser).await().toObject(user::class.java)!! //to get user

            val currentTime = System.currentTimeMillis() //return Long
            val post = post(text, user, currentTime)

            postCollections.document().set(post)
        }
    }

    fun getPostById (postId : String):Task<DocumentSnapshot>{
        return postCollections.document(postId).get()
    }

    fun updateLikes (postId : String){

        GlobalScope.launch {
            val currentUser = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(post::class.java)!!
            val isLikedBy = post.likedBy.contains(currentUser)

            //if the user has already liked the post before and he again tries to like is (basically
            //dislikes it again) we remove him from the likeBy array
            //and if he is not in array we add his userid

            if (isLikedBy){
                post.likedBy.remove(currentUser)
            }
            else{
                post.likedBy.add(currentUser)
            }
            postCollections.document(postId).set(post)
        }
    }

}