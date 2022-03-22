package com.example.socialbutterfly

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.socialbutterfly.model.daos.userDao
import com.example.socialbutterfly.model.user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SigninActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("505905325936-5j3bjk6o790kem229q9uhcjpi15k7hi3.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        SignInButton.setOnClickListener {
            signIn()
        }
    }

    //onStart() always run first after the onCreate()
    //hum yaha yeh handle kar rahe hai ki user agar ek baar sign kar de to use baar baar sign in karna na pade
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)

        //currentUser will be null if user has not signed in ever ..this case is handled in updateUI()
    }

    private fun signIn() {

        //passing an intent : yeh intent woh hai jo hum jab sign in button pe dabayege aur yeh hume
        //sare google se linked accounts dikhayega for sign in options

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

        //stratActivityForResult ka fayda yeh hota hai ki jab aap uss activity se wapas aa rahe ho to
        //aap bata sakte ho ki hum wapis aa gaye..i.e. callBack milta hai
    }

    //callBack function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(Completedtask: Task<GoogleSignInAccount>) {
        try{
            val account =
                Completedtask.getResult(ApiException::class.java)
            Log.d (TAG, "fireBaseAuthWithGoogle " + account?.id)
            firebaseAuthWithGoogle(account?.idToken!!)
        }catch (e : ApiException){
            Log.w(TAG, "SignInResult : Failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        SignInButton.visibility = View.GONE

//        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        //GlobalScope is used to call an top level coroutine
        //coroutines are just like thread but which don't run on main thread, it runs on bg thread
        // and which runs concurrently
        //with the rest of the code. However, a coroutine is not bound to any particular thread.
        // It may suspend its execution in one thread and resume in another one.

        //.await() (only works in coroutines) is to avoid callBackHELL
        GlobalScope.launch(Dispatchers.IO){
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user

            //hum chahte hai ki humari UI update hone ka kaam main thread pe ho to hume wapis main
            //thread pe switch karna hoga , uske lie withContext() use karte hai
            //bcz UI thread jo hai woh backGround thread se work nahi karta hai usko UI thread se hi
            //update kar sakte hai
            withContext(Dispatchers.Main){
                updateUI (firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {

        //if we get the FireBaseUser obj we start our MainActivity using intent and finish this one
        if (firebaseUser != null){

            //adding user to db after signin completion
            val User = user(firebaseUser.uid, firebaseUser.displayName, firebaseUser.photoUrl.toString())
            val usersDao = userDao()
            usersDao.addUser(User)

            val MainActivityIntent = Intent (this, MainActivity::class.java)
            startActivity(MainActivityIntent)
            finish()
        }

        //if we dont get the obj we (unable to login) we dont want to change this page
        else{
            SignInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}