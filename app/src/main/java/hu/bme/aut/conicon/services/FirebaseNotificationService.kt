package hu.bme.aut.conicon.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.constants.AppConstants
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.NavigationActivity
import hu.bme.aut.conicon.ui.chat.ChatFragment
import java.lang.Exception
import java.util.*

class FirebaseNotificationService : FirebaseMessagingService() {
    private val auth = FirebaseAuth.getInstance()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        updateToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data

            if (auth.currentUser != null && data["receiverID"] == auth.currentUser?.uid.toString()) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    createOreoNotification(
                        data["conversationID"].toString(),
                        data["senderID"].toString(),
                        data["message"].toString()
                    )
                } else {
                    createNotification(
                        data["conversationID"].toString(),
                        data["senderID"].toString(),
                        data["message"].toString()
                    )
                }
            }
        }
    }

    private fun updateToken(token: String) {
        if (auth.currentUser != null) {
            val uid = auth.currentUser?.uid.toString()
            val tokenReference = FirebaseFirestore.getInstance().collection("Tokens").document(uid)
            tokenReference.update("tokens.$token", true)
        }
    }

    private fun createNotification(
        conversationID: String,
        senderID: String,
        message: String
    ) {
        val userReference = FirebaseFirestore.getInstance().collection("users").document(senderID)
        userReference.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(AppUser::class.java)!!

                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val builder = NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                builder.setContentTitle(user.username)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setColor(ResourcesCompat.getColor(resources, R.color.orange, null))
                    .setSound(uri)

                val intent = Intent(this, NavigationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("conversationID", conversationID)
                intent.putExtra("senderID", senderID)

                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                builder.setContentIntent(pendingIntent)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(senderID.hashCode(), builder.build())
            }
        }
    }

    private fun createOreoNotification(
        conversationID: String,
        senderID: String,
        message: String
    ) {
        val userReference = FirebaseFirestore.getInstance().collection("users").document(senderID)
        userReference.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val user = document.toObject(AppUser::class.java)!!

                val channel = NotificationChannel(AppConstants.CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH)

                channel.setShowBadge(true)
                channel.enableLights(true)
                channel.enableVibration(true)
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                val intent = Intent(this, NavigationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("conversationID", conversationID)
                intent.putExtra("senderID", senderID)

                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                val notificationBuilder = Notification.Builder(this, AppConstants.CHANNEL_ID)
                    .setContentTitle(user.username)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setLights(Color.YELLOW, 100, 100)
                    .setContentIntent(pendingIntent)
                    .setDefaults(0)

                val context = this

                if (user.photoUrl != null) {
                    Picasso.get().load(user.photoUrl).into(
                        object: Target {
                            override fun onBitmapLoaded(
                                bitmap: Bitmap?,
                                from: Picasso.LoadedFrom?
                            ) {
                                notificationBuilder.setLargeIcon(bitmap)
                            }

                            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                notificationBuilder.setLargeIcon(
                                    getBitmapFromVector(applicationContext, R.drawable.ic_profile)
                                )
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
                        }
                    )
                } else {
                    notificationBuilder.setLargeIcon(
                        getBitmapFromVector(applicationContext, R.drawable.ic_profile)
                    )
                }

                val notification = notificationBuilder.build()

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
                manager.notify(senderID.hashCode(), notification)
            }
        }
    }

    private fun getBitmapFromVector(context: Context, drawableID: Int) : Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableID)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}