package com.example.cacheit.myGamesActivities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cacheit.Firebase
import com.example.cacheit.Firebase.Companion.databaseUsers
import com.example.cacheit.MailingConfig.GMailSender
import com.example.cacheit.MailingConfig.SendEmail
import com.example.cacheit.R
import com.example.cacheit.gamesActivities.GameCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.layout_game_card_item.view.*
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.io.File
import kotlin.concurrent.thread


class MyGamesRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<GameCard> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyGamesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_game_card_item, parent, false),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is MyGamesViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(GroupCardList: List<GameCard>) {
        items = GroupCardList
        notifyDataSetChanged()
    }

    class MyGamesViewHolder constructor(
        itemView: View,
        parentContext: Context
    ): RecyclerView.ViewHolder(itemView) {

        private val gameImage = itemView.img_game_card
        private val gameName = itemView.tv_game_card_name
        private val btnStatus = itemView.btn_status
        private val btnSettings = itemView.btn_settings
        private val btnDelete = itemView.btn_delete

        fun bind(GameCard: GameCard) {
            gameName.text = GameCard.name

            if (GameCard.active) {
                btnStatus.visibility = VISIBLE
            } else {
                btnStatus.visibility = GONE
            }

            btnSettings!!.setOnClickListener { v ->
                var mDatabase: FirebaseDatabase? = null
                var mAuth: FirebaseAuth? = null
                var email: String? = null

                mDatabase = FirebaseDatabase.getInstance()
                mAuth = FirebaseAuth.getInstance()

                val userRef = mDatabase.getReference("/Users/" + mAuth.uid)
                val ref = mDatabase.getReference("/Games/" + GameCard.id)

                val dialog = Dialog(itemView.context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.layout_my_game_details)

                val btnDismiss = dialog.findViewById(R.id.btn_close_details) as ImageButton
                val btnToggleActivity = dialog.findViewById(R.id.btn_action) as Button
                val btnResendEmail = dialog.findViewById(R.id.btn_resend_email) as Button
                val name = dialog.findViewById(R.id.tv_game_details_name) as TextView
                val finished = dialog.findViewById(R.id.tv_times_solved) as TextView
                val canceled = dialog.findViewById(R.id.tv_times_quit) as TextView
                val rating = dialog.findViewById(R.id.rb_game_rating) as RatingBar
                val points = dialog.findViewById(R.id.tv_maker_points) as TextView

                name.text = GameCard.name
                rating.rating = GameCard.rating.toFloat();

                finished.text = GameCard.timesFinished
                canceled.text = GameCard.timesClosed
                points.text = GameCard.gameMakerPoints

                if (GameCard.active) { btnToggleActivity.text = "Deactivate" } else { btnToggleActivity.text = "Activate" }

                btnToggleActivity!!.setOnClickListener { v ->
                    if (GameCard.active) {
                        btnToggleActivity.text = "Activate"
                        btnStatus.visibility = GONE
                        ref.child("active").setValue(false)
                        GameCard.active = false
                    } else {
                        btnToggleActivity.text = "Deactivate"
                        btnStatus.visibility = VISIBLE
                        ref.child("active").setValue(true)
                        GameCard.active = true
                    }
                }

                btnDismiss!!.setOnClickListener { v ->
                    dialog.dismiss()
                }

                btnResendEmail!!.setOnClickListener { v ->

                    val file: File = QRCode.from(GameCard.id).file()

                    val subject = GameCard.name + " QR Code"
                    var message =
                        "Please print this QR Code and place it at the location of your game finish! After you are sure the game is ready, activate it so others can start playing :-)"

                    databaseUsers?.child(Firebase.auth!!.currentUser!!.uid)?.child("email")
                        ?.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                var myWebFetch: SendEmail? = null
                                myWebFetch = SendEmail(
                                    subject,
                                    message,
                                    "cacheitapp@gmail.com",
                                    p0.value.toString(),
                                    file
                                );
                                myWebFetch.execute();
                            }

                        })
                }
                dialog.show()
            }

            btnDelete!!.setOnClickListener { v ->
                val dialogBuilder = AlertDialog.Builder(itemView.context)
                dialogBuilder.setMessage("Are you sure you want to delete this game?")
                    // if the dialog is cancelable
                    .setCancelable(true)
                    .setPositiveButton("Ok") { _: DialogInterface, _: Int ->
                        var mDatabase: FirebaseDatabase? = null
                        mDatabase = FirebaseDatabase.getInstance()
                        val ref = mDatabase!!.getReference("/Games/" + GameCard.id)
                        ref.child("deleted").setValue(true)
                    }

                val alert = dialogBuilder.create()
                alert.show()
            }
            /*val requestOptions = RequestOptions()
                .placeholder(drawable.ic_launcher_background)
                .error(drawable.ic_launcher_background)
*/
            Glide.with(itemView.context)
                //.applyDefaultRequestOptions(requestOptions)
                .load(GameCard.gameImg)
                .into(gameImage)
        }
    }

}

