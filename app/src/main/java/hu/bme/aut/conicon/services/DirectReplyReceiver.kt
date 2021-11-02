package hu.bme.aut.conicon.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import hu.bme.aut.conicon.constants.AppConstants
import hu.bme.aut.conicon.network.model.MessageElement
import java.util.*

class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        if (remoteInput != null && intent != null) {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                val reply = remoteInput.getCharSequence(AppConstants.REPLY_KEY)
                val conversationID = intent.getStringExtra("conversationID")
                val senderID = intent.getStringExtra("senderID")

                if (reply != null && conversationID != null && senderID != null) {
                    val conversationRef =
                        FirebaseFirestore.getInstance().collection("conversations").document(conversationID)
                    val messagesCollection =
                        conversationRef.collection("messages")
                    val newDocument = messagesCollection.document()
                    val newMessage =
                        MessageElement(
                            newDocument.id,
                            auth.currentUser?.uid.toString(),
                            reply.toString(),
                            Date().time
                        )
                    messagesCollection.document(newDocument.id).set(newMessage).addOnSuccessListener {
                        Toast.makeText(context, "Sending reply", Toast.LENGTH_SHORT).show()

                        conversationRef.update("lastMessage", newMessage).addOnSuccessListener {
                            Toast.makeText(context, "Reply sent successfully", Toast.LENGTH_SHORT).show()
                            val manager =
                                context?.getSystemService(FirebaseMessagingService.NOTIFICATION_SERVICE) as NotificationManager
                            manager.cancel(senderID.hashCode())
                        }
                    }
                }
            }
        }
    }
}