package com.sedatates.sohbetmuhabbet

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerViewAccessibilityDelegate
import android.view.MenuItem
import android.widget.Adapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sedatates.sohbetmuhabbet.Fragments.CreateNewRoomFragment
import com.sedatates.sohbetmuhabbet.Models.NewChatRoomModel
import com.sedatates.sohbetmuhabbet.Models.SohbetMesajModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var allChatRooms:ArrayList<NewChatRoomModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        init()

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

                var intent2 = Intent(this, SettingsActivity::class.java)
                startActivity(intent2)

            }

            R.id.nav_logout -> {

                var user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    FirebaseAuth.getInstance().signOut()
                    var intent = Intent(this, LoginRegisterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }

                return true

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun init(){
        fabNewRoom.setOnClickListener {
            val newRoomFragment= CreateNewRoomFragment()
            newRoomFragment.show(supportFragmentManager,"see")
        }

        readDataFromDatabase()

    }

    private fun readDataFromDatabase() {

        allChatRooms= ArrayList<NewChatRoomModel>()

        val ref=FirebaseDatabase.getInstance().reference

       var sorgu= ref.child("Sohbet Odası").addListenerForSingleValueEvent(object:ValueEventListener{
           override fun onCancelled(p0: DatabaseError) {

           }

           override fun onDataChange(p0: DataSnapshot) {

               for (tekSohbetOdasi in p0.children){
                   val hashMap=(tekSohbetOdasi.getValue() as HashMap<String,Object>)

                   val currentSohbetOdasi=NewChatRoomModel()

                   currentSohbetOdasi.room_id=hashMap.get("room_id").toString()
                   currentSohbetOdasi.room_name=hashMap.get("room_name").toString()
                   currentSohbetOdasi.room_seviye=hashMap.get("room_seviye").toString()
                   currentSohbetOdasi.userId=hashMap.get("userId").toString()

                   val allMassages=ArrayList<SohbetMesajModel>()

                   for (mesajlar in tekSohbetOdasi.child("sohbet_odasi_mesajlari").children){

                       val currentMesaj=SohbetMesajModel()

                       currentMesaj.id=mesajlar.getValue(SohbetMesajModel::class.java)?.id
                       currentMesaj.massage=mesajlar.getValue(SohbetMesajModel::class.java)?.massage
                       currentMesaj.name=mesajlar.getValue(SohbetMesajModel::class.java)?.name
                       currentMesaj.picture=mesajlar.getValue(SohbetMesajModel::class.java)?.picture
                       currentMesaj.time=mesajlar.getValue(SohbetMesajModel::class.java)?.time

                       allMassages.add(currentMesaj)

                   }

                   currentSohbetOdasi.sohbet_odasi_mesajlari=allMassages

                   allChatRooms.add(currentSohbetOdasi)

               }
           }

       })

    }
}
