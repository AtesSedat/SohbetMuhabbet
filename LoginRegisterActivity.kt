package com.sedatates.sohbetmuhabbet

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sedatates.sohbetmuhabbet.Fragments.SifremiUnuttumFragment
import com.sedatates.sohbetmuhabbet.Models.UserClass
import kotlinx.android.synthetic.main.activity_login_register.*

class LoginRegisterActivity : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        val initializeApp = FirebaseApp.initializeApp(this)

        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){

            }else{

            }
        }


        progressBarHide()

        btnRegister.setOnClickListener {
            if (etEmail.text.isNullOrEmpty() && etPassword.text.isNullOrEmpty()) {

                Toast.makeText(
                    this, "Email ve Password alanlarını doldurunuz!"
                    , Toast.LENGTH_SHORT
                )
                    .show()

            } else {
                progressBarShow()

                createNewUser(etEmail.text.toString(), etPassword.text.toString())
            }
        }

        btnLogin.setOnClickListener {
            if (etEmail.text.isNullOrEmpty() && etPassword.text.isNullOrEmpty()) {

                Toast.makeText(
                    this, "Email ve Password alanlarını doldurunuz!"
                    , Toast.LENGTH_SHORT
                )
                    .show()

            } else {
                progressBarShow()
                login(etEmail.text.toString(), etPassword.text.toString())
            }
        }

        tvSifremiUnuttum.setOnClickListener {
            var sendResetEmail= SifremiUnuttumFragment()
            sendResetEmail.show(supportFragmentManager,"send")
        }

        initMyAuthStateListener()

    }

    private fun login(email: String, password: String) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    progressBarHide()

                    var user=FirebaseAuth.getInstance().currentUser


                    if (user!!.isEmailVerified){
                        var intent = Intent(this@LoginRegisterActivity, MainActivity::class.java)
                        intent.putExtra("email",user.email)
                        startActivity(intent)
                        finish()

                    }else{
                        Toast.makeText(
                            this@LoginRegisterActivity, "Be Verified your email address and try again" ,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                } else {

                    progressBarHide()
                    Toast.makeText(
                        this@LoginRegisterActivity, "Sign In failed." + task.exception,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    private fun createNewUser(email: String, password: String) {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    progressBarHide()
                    // Sign in success, update UI with the signed-in UserClass's information
                    sendEmailVerification()

                    //Database işlemleri

                    var dataToDatabase= UserClass()
                    dataToDatabase.user_name=etEmail.text.toString().substring(0, etEmail.text.toString().indexOf("@"))
                    dataToDatabase.user_id=FirebaseAuth.getInstance().currentUser?.uid
                    dataToDatabase.user_phone="121212"
                    dataToDatabase.user_photo=""
                    dataToDatabase.user_category="50"


                    FirebaseDatabase.getInstance().reference
                        .child("user")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(dataToDatabase).addOnCompleteListener { task1 ->
                            if (task1.isSuccessful){
                                Toast.makeText(this@LoginRegisterActivity, "createUserWithEmail:success", Toast.LENGTH_SHORT).show()

                                FirebaseAuth.getInstance().signOut()
                            }
                        }

                } else {
                    // If sign in fails, display a message to the UserClass.
                    progressBarHide()
                    Toast.makeText(this@LoginRegisterActivity, "Authentication failed." + task.exception, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendEmailVerification() {

        var user=FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(this){ task: Task<Void> ->
            if (task.isSuccessful){
                Toast.makeText(
                    this@LoginRegisterActivity, "Check your emailbox" ,
                    Toast.LENGTH_SHORT
                )
                    .show()

            }else{
                Toast.makeText(
                    this@LoginRegisterActivity, "Email can not send" + task.exception,
                    Toast.LENGTH_SHORT
                )
                    .show()

            }
        }
    }

    private fun progressBarShow() {
        progressBar.visibility = View.VISIBLE
    }

    private fun progressBarHide() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun initMyAuthStateListener() {

        mAuthStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici= p0.currentUser

                if(kullanici != null){

                    if(kullanici.isEmailVerified){
                        Toast.makeText(this@LoginRegisterActivity, "Mail onaylanmış giriş yapılabilir", Toast.LENGTH_SHORT).show()
                        var intent=Intent(this@LoginRegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }else {
                        Toast.makeText(this@LoginRegisterActivity, "Mail adresinizi onaylayıp öyle giriş yapın", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()

        var mAuth=FirebaseAuth.getInstance()
        mAuth.addAuthStateListener { mAuthStateListener }

    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener { mAuthStateListener }
    }

}
