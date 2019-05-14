package com.sedatates.sohbetmuhabbet

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.sedatates.sohbetmuhabbet.Models.UserClass
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    var imageFromGallery: Uri? = null
    var imageFromCamera: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        progressBarPhoto.visibility = View.INVISIBLE

        var user = FirebaseAuth.getInstance().currentUser

        readDataFromDatabase()

        initMyAuthStateListener()

        choosePhoto()

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        // tvSettingsUserId.text = FirebaseAuth.getInstance().currentUser?.uid
        tv_user_email2.text = FirebaseAuth.getInstance().currentUser?.email
        var kullaniciAdi= FirebaseAuth.getInstance().currentUser?.email?.substring(0,
            FirebaseAuth.getInstance().currentUser?.email?.indexOf("@")!!
        )


        btnDelete.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            user?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Hesabınız Silindi", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }

    private fun saveChanges() {
        var user = FirebaseAuth.getInstance().currentUser
        var database = FirebaseDatabase.getInstance().reference

        if (imageFromGallery != null) {

            var compressed = BackgroundImageCompress()
            compressed.execute(imageFromGallery)

        } else if (imageFromCamera != null) {
            var compressed1 = BackgroundImageCompress(imageFromCamera!!)
            var uri: Uri? = null
            compressed1.execute(uri)
        } else {

            Toast.makeText(this, "Fotoğraf Ekleyebilirsiniz!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun choosePhoto() {
        imgGaleri.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                var intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, 100)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    150
                )
            }


        }

        imgCamera.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                var intent2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent2, 200)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 250)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 150) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                var intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, 100)
            } else {
                Toast.makeText(this, "İzin Verilmedi", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == 250) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                var intent2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent2, 200)
            } else {
                Toast.makeText(this, "İzin Verilmedi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {

            imageFromGallery = data.data
            img_user_photo.setImageURI(imageFromGallery)


        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {

            imageFromCamera = data.extras.get("data") as Bitmap
            img_user_photo.setImageBitmap(imageFromCamera)
        }

    }

    inner class BackgroundImageCompress : AsyncTask<Uri, Double, ByteArray?> {

        var myBitmap: Bitmap? = null

        constructor() {}

        constructor(bm: Bitmap) {
            if (bm != null) {
                myBitmap = bm
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Uri?): ByteArray? {

            if (myBitmap == null) {
                myBitmap = MediaStore.Images.Media.getBitmap(this@SettingsActivity.contentResolver, params[0])
            }
            var resimBytes: ByteArray? = null

            for (i in 1..10) {
                resimBytes = convertBitmaptoByte(myBitmap, 100 / i)
                publishProgress(resimBytes!!.size.toDouble())

            }

            return resimBytes

        }

        private fun convertBitmaptoByte(myBitmap: Bitmap?, i: Int): ByteArray? {
            val stream = ByteArrayOutputStream()
            myBitmap?.compress(Bitmap.CompressFormat.JPEG, i, stream)
            return stream.toByteArray()
        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            uploadResimtoFirebase(result)
        }

    }

    private fun uploadResimtoFirebase(result: ByteArray?) {

        progressBarPhoto.visibility = View.VISIBLE

        val storegeRef = FirebaseStorage.getInstance().reference
        var resimEklenecekYer =
            storegeRef.child("user/" + FirebaseAuth.getInstance().currentUser?.uid + "/user_photo")
        var uploudTask = resimEklenecekYer.putBytes(result!!)


        val urlTask = uploudTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation resimEklenecekYer.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result

                FirebaseDatabase.getInstance().reference
                    .child("user")
                    .child(FirebaseAuth.getInstance().currentUser?.uid!!)
                    .child("user_photo")
                    .setValue(downloadUri.toString())

                Toast.makeText(this@SettingsActivity, "Değişiklikler Yapıldı", Toast.LENGTH_SHORT).show()

                progressBarPhoto.visibility = View.INVISIBLE
            } else {

                Toast.makeText(this@SettingsActivity, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun readDataFromDatabase() {

        val userData = FirebaseDatabase.getInstance().reference
        val kullanici = FirebaseAuth.getInstance().currentUser

        var request = userData.child("user")
            .orderByKey()
            .equalTo(kullanici?.uid)

        request.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (singleSnapshot in p0.children) {
                    var readDataSnapshot = singleSnapshot.getValue(UserClass::class.java)


                    tv_user_phone2.text = readDataSnapshot?.user_phone
                    tv_user_name2.text = readDataSnapshot?.user_name

                    if (readDataSnapshot?.user_photo.isNullOrEmpty()) {
                        Picasso.get().load(R.drawable.ic_account_circle).into(img_user_photo)
                    } else {
                        Picasso.get().load(readDataSnapshot?.user_photo).into(img_user_photo)
                    }

                }
            }
        })
    }

    private fun initMyAuthStateListener() {

        mAuthStateListener = FirebaseAuth.AuthStateListener { task ->

            var user = task.currentUser

            if (user != null) {


            } else {
                var intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }

        }
    }

    override fun onResume() {
        super.onResume()

        var user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            var intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener { mAuthStateListener }

    }

    override fun onStop() {
        super.onStop()

        FirebaseAuth.getInstance().removeAuthStateListener { mAuthStateListener }

    }
}
