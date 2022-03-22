package com.example.socialbutterfly.model

data class post (
    val text : String = "",
    val createdBy : user = user(),
    val createdAt : Long = 0L ,
    val likedBy : ArrayList<String> = ArrayList() //to store user ids who have liked the post
// size of this arrayList will give us number of like counts
)