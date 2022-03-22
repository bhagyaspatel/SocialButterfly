package com.example.socialbutterfly.model.daos

//i have created daos Package inside model package which was outside the model package in tutorial
//see if any problem occurs or not

import com.example.socialbutterfly.model.user
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class userDao {
    private val db = FirebaseFirestore.getInstance() //getting user database referance
    private val usersCollection = db.collection("users")

    //adding user to data base when user sign in
    fun addUser (User : user?) {
        User?.let {

            GlobalScope.launch(Dispatchers.IO) {
                usersCollection.document(User.uid).set(User) //it = user..
                //userCollection will be automatically created ..can see this is in ur project in firebase site
                //document is a userCollection function ..hum chahte hai ki iss document me humare data
                //ki jo id ho woh humare uid ke equal ho
                // yeh call hum bg thread pe karna chahege
                //bcz main thread pe karne se screen pause ho jati hai jab tak task complete na ho jaye
            }
        }
    }

    fun getUserById (uId : String) : Task<DocumentSnapshot> {
        return usersCollection.document(uId).get()
    }

}