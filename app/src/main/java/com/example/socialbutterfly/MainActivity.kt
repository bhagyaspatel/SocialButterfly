package com.example.socialbutterfly

import android.app.DownloadManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialbutterfly.model.daos.PostDao
import com.example.socialbutterfly.model.post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), postAdapterInterface {

    private lateinit var adapter : postAdapter
    private lateinit var postDao : PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener{
            val intent = Intent (this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        setRecyclerView ()
    }

    private fun setRecyclerView() {
        //this postAdpater takes FirestoreRecyclerViewOption as parameter & iss recyclerViewOption ko
        //banane ke lie hume 1 query chahiye and issi query me hum humara sara logic likhege jo logic
        //se hume humara sara ka sara data uss recyclerView me store karna hai

        postDao = PostDao()
        val postCollection = postDao.postCollections
        val query = postCollection.orderBy("createdAt", Query.Direction.DESCENDING)
        val reyclerViewOption = FirestoreRecyclerOptions.Builder<post>().setQuery(query, post::class.java).build()

        adapter = postAdapter(reyclerViewOption, this)

        //attaching our adapter and layout manager to our recyclerView(id of our recyclerView in activity_main.xml)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager (this)
    }

    //agar hum chahte hai ki humara adapter firebase ho rahe sare changes ko listen kare to hume usko
    //batana padega ki konse time pe listen karna start karna hai and konse time pe bandh karna hai
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
         postDao.updateLikes(postId)
    }
}
