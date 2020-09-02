package com.example.cacheit.userProfileActivities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cacheit.Firebase
import com.example.cacheit.R
import com.example.cacheit.authenticationActivities.LoginActivity
import com.example.cacheit.gameplayActivities.GameplayData
import com.example.cacheit.gamesActivities.TopSpacingItemDecoration
import com.example.cacheit.leaderboardActivities.PlayerData
import com.example.cacheit.myGamesActivities.MyGamesRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class UserProfileActivity : Fragment() {

    private lateinit var myCompletedGamesRecyclerAdapter: MyCompletedGamesRecyclerAdapter
    private var root: View? = null
    private var username: TextView? = null
    private var email: TextView? = null
    private var btnSettings: ImageButton? = null
    private var rvCompletedGames: RecyclerView? = null
    private var playerPoints: TextView? = null
    private var makerPoints: TextView? = null
    private var userImg: CircleImageView? = null
    private var logout: TextView? = null
    private var noGames: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.activity_user_profile, container, false)
        super.onCreate(savedInstanceState)

        initialise()
        return root
    }
        private fun initialise() {
            initRecyclerView()
            addDataSet()

            username = root?.findViewById<TextView>(R.id.tv_username) as TextView
            email = root?.findViewById<TextView>(R.id.tv_email) as TextView
            btnSettings = root?.findViewById<ImageButton>(R.id.btn_settings) as ImageButton
            userImg = root?.findViewById<CircleImageView>(R.id.img_profile_photo) as CircleImageView
            playerPoints = root?.findViewById<TextView>(R.id.tv_player_points) as TextView
            makerPoints = root?.findViewById<TextView>(R.id.tv_maker_points) as TextView
            logout = root?.findViewById<TextView>(R.id.tv_logout) as TextView
            noGames = root?.findViewById<TextView>(R.id.tv_no_games_completed) as TextView

            var mDatabase: FirebaseDatabase? = null
            mDatabase = FirebaseDatabase.getInstance()
            var mAuth: FirebaseAuth? = null
            mAuth = FirebaseAuth.getInstance()

            Log.e("auth id", mAuth.uid.toString())
            val userRef = mDatabase!!.getReference("/Users/" + mAuth.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    username!!.text = p0.child("username").value.toString()
                    email!!.text = p0.child("email").value.toString()
                    playerPoints!!.text = p0.child("playerScore").value.toString()
                    makerPoints!!.text = p0.child("makerScore").value.toString()

                    Glide.with(activity!!)
                        //.applyDefaultRequestOptions(requestOptions)
                        .load(p0.child("profilePhotoUrl").value.toString())
                        .into(userImg!!)
                }
            })

            btnSettings!!.setOnClickListener{
                val userSettingsFragment = UserProfileSettingsActivity.newInstance()
                childFragmentManager.beginTransaction().replace(R.id.user_profile, userSettingsFragment).commit()
            }

            logout!!.setOnClickListener{
                val tokenMap: MutableMap<String, Any> = HashMap()
                tokenMap["token_id"] = ""

                Firebase.firestore?.collection("Users")?.document(Firebase.userId)?.update(tokenMap)
                Firebase.databaseUsers?.child(Firebase.userId)?.child("tokenId")?.removeValue()
                Firebase.auth?.signOut()
                val intent = Intent(activity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            if (GameplayData.myCompletedGameplays.size == 0) {
                noGames?.visibility = VISIBLE
                rvCompletedGames?.visibility = GONE
            }

        }


        private fun addDataSet() {
            Log.e(tag, "Fetched user's games: " + GameplayData.myCompletedGameplays)
            myCompletedGamesRecyclerAdapter.submitList(GameplayData.myCompletedGameplays)
            Log.e("completed games", GameplayData.myCompletedGameplays.size.toString())
        }

        private fun initRecyclerView() {
            rvCompletedGames = root?.findViewById<RecyclerView>(R.id.rv_my_completed_games)
            rvCompletedGames?.apply {
                layoutManager = LinearLayoutManager(activity)
                val topSpacingDecoration = TopSpacingItemDecoration(30)
                addItemDecoration(topSpacingDecoration)
                myCompletedGamesRecyclerAdapter = MyCompletedGamesRecyclerAdapter()
                adapter = myCompletedGamesRecyclerAdapter
            }
        }

        companion object {
            fun newInstance(): UserProfileActivity = UserProfileActivity()
        }

    }