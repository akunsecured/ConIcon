package hu.bme.aut.conicon.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.constants.AppConstants
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.NavigationActivity
import java.lang.Exception
import java.util.*

@SuppressLint("UnspecifiedImmutableFlag")
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
            val typeValue = data["type"]!!.toInt()

            val type = NotificationType.getByValue(typeValue)!!

            if (
                auth.currentUser != null
                && data["receiverID"] == auth.currentUser?.uid.toString()
                && data["receiverID"] != data["senderID"]
            ) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    createOreoNotification(
                        type,
                        data
                    )
                } else {
                    createNotification(
                        type,
                        data
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
        type: NotificationType,
        dataMap: MutableMap<String, String>
    ) {
        val senderID = dataMap["senderID"].toString()
        val userReference = FirebaseFirestore.getInstance().collection("users").document(senderID)
        userReference.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(AppUser::class.java)!!

                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val builder = NotificationCompat.Builder(this, AppConstants.CHAT_CHANNEL_ID)
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setColor(ResourcesCompat.getColor(resources, R.color.color_application, null))
                    .setSound(uri)

                val notifyID: Int

                when (type) {
                    NotificationType.MESSAGE -> {
                        val message = dataMap["message"].toString()
                        val conversationID = dataMap["conversationID"].toString()

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("conversationID", conversationID)
                        intent.putExtra("senderID", senderID)
                        intent.putExtra("type", NotificationType.MESSAGE.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        builder.setContentTitle(user.username)
                            .setContentText(message)
                            .setContentIntent(pendingIntent)

                        notifyID = senderID.hashCode()
                    }

                    NotificationType.IMAGE_LIKE -> {
                        val mediaID = dataMap["mediaID"].toString()

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("mediaID", mediaID)
                        intent.putExtra("type", NotificationType.IMAGE_LIKE.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        builder.setContentTitle("New like")
                            .setContentText("${user.username} has liked one of your posts")
                            .setContentIntent(pendingIntent)

                        notifyID = mediaID.hashCode()
                    }

                    NotificationType.FOLLOW -> {
                        val receiverID = dataMap["receiverID"].toString()

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("receiverID", receiverID)
                        intent.putExtra("type", NotificationType.FOLLOW.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        builder.setContentTitle("New follower")
                            .setContentText("${user.username} has started following you")
                            .setContentIntent(pendingIntent)

                        notifyID = receiverID.hashCode()
                    }
                }


                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(notifyID, builder.build())
            }
        }
    }

    private fun createOreoNotification(
        type: NotificationType,
        dataMap: MutableMap<String, String>
    ) {
        val senderID = dataMap["senderID"].toString()
        val userReference = FirebaseFirestore.getInstance().collection("users").document(senderID)
        userReference.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val user = document.toObject(AppUser::class.java)!!

                val channel: NotificationChannel
                val notificationBuilder: Notification.Builder
                val notifyID: Int

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                when (type) {
                    NotificationType.MESSAGE -> {
                        val conversationID = dataMap["conversationID"].toString()
                        val message = dataMap["message"].toString()

                        channel = NotificationChannel(
                            AppConstants.CHAT_CHANNEL_ID,
                            "New message",
                            NotificationManager.IMPORTANCE_HIGH
                        )

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("conversationID", conversationID)
                        intent.putExtra("senderID", senderID)
                        intent.putExtra("type", NotificationType.MESSAGE.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        notificationBuilder = Notification.Builder(this, AppConstants.CHAT_CHANNEL_ID)
                            .setContentTitle(user.username)
                            .setContentText(message)
                            .setContentIntent(pendingIntent)

                        setNotificationLargeIcon(user, notificationBuilder)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val replyRemote = RemoteInput.Builder(AppConstants.REPLY_KEY).run {
                                setLabel(getString(R.string.type_to_message))
                                build()
                            }

                            val replyIntent = Intent(this, DirectReplyReceiver::class.java)
                            replyIntent.putExtra("conversationID", conversationID)
                            replyIntent.putExtra("senderID", senderID)
                            val replyPendingIntent =
                                PendingIntent.getBroadcast(this, 1, replyIntent, PendingIntent.FLAG_ONE_SHOT)

                            val replyAction = Notification.Action.Builder(
                                0, "Reply", replyPendingIntent
                            ).addRemoteInput(replyRemote).build()

                            notificationBuilder.addAction(replyAction)
                        }

                        notifyID = senderID.hashCode()
                    }

                    NotificationType.IMAGE_LIKE -> {
                        val mediaID = dataMap["mediaID"].toString()

                        channel = NotificationChannel(
                            AppConstants.IMAGE_LIKE_CHANNEL_ID,
                            "Image liked",
                            NotificationManager.IMPORTANCE_HIGH
                        )

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("mediaID", mediaID)
                        intent.putExtra("type", NotificationType.IMAGE_LIKE.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        notificationBuilder = Notification.Builder(this, AppConstants.IMAGE_LIKE_CHANNEL_ID)
                            .setContentTitle("New like")
                            .setContentText("${user.username} has liked one of your posts")
                            .setContentIntent(pendingIntent)

                        notifyID = mediaID.hashCode()
                    }

                    NotificationType.FOLLOW -> {
                        val receiverID = dataMap["receiverID"].toString()

                        channel = NotificationChannel(
                            AppConstants.FOLLOW_CHANNEL_ID,
                            "New follower",
                            NotificationManager.IMPORTANCE_HIGH
                        )

                        val intent = Intent(this, NavigationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtra("receiverID", receiverID)
                        intent.putExtra("type", NotificationType.FOLLOW.value)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

                        notificationBuilder = Notification.Builder(this, AppConstants.FOLLOW_CHANNEL_ID)
                            .setContentTitle("New follower")
                            .setContentText("${user.username} has started following you")
                            .setContentIntent(pendingIntent)

                        setNotificationLargeIcon(user, notificationBuilder)

                        notifyID = receiverID.hashCode()
                    }
                }

                notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(true)
                    .setLights(Color.YELLOW, 100, 100)
                    .setDefaults(0)

                channel.setShowBadge(true)
                channel.enableLights(true)
                channel.enableVibration(true)
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                manager.createNotificationChannel(channel)

                val notification = notificationBuilder.build()
                manager.notify(notifyID, notification)
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

    private fun setNotificationLargeIcon(user: AppUser, notificationBuilder: Notification.Builder) {
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
    }
}