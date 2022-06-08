package hu.bme.aut.conicon.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import hu.bme.aut.conicon.constants.AppConstants
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.network.model.MessageElement
import hu.bme.aut.conicon.ui.CommonMethods
import org.json.JSONObject
import java.util.*

class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)

        if (remoteInput != null && intent != null) {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                val uid = auth.currentUser?.uid.toString()
                val reply = remoteInput.getCharSequence(AppConstants.REPLY_KEY)
                val conversationID = intent.getStringExtra("conversationID")
                val senderID = intent.getStringExtra("senderID")

                if (reply != null && conversationID != null && senderID != null) {
                    val senderConversationRef =
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                            .collection("conversations").document("$uid+$senderID")
                    val senderChatCollection =
                        senderConversationRef.collection("messages")

                    val receiverConversationRef =
                        FirebaseFirestore.getInstance().collection("users").document(senderID)
                            .collection("conversations").document("$senderID+$uid")
                    val receiverChatCollection =
                        receiverConversationRef.collection("messages")

                    val newMessageDocument = senderChatCollection.document()
                    val newMessageElement =
                        MessageElement(
                            newMessageDocument.id,
                            uid,
                            reply.toString(),
                            Date().time
                        )

                    senderChatCollection.document(newMessageDocument.id).set(newMessageElement).addOnSuccessListener {
                        Toast.makeText(context, "Sending reply", Toast.LENGTH_SHORT).show()

                        receiverChatCollection.document(newMessageDocument.id).set(newMessageElement).addOnSuccessListener {
                            senderConversationRef.update("lastMessage", newMessageElement).addOnSuccessListener {
                                receiverConversationRef.update("lastMessage", newMessageElement).addOnSuccessListener {
                                    val data = JSONObject()

                                    data.put("conversationID", "$senderID+$uid")
                                    data.put("senderID", uid)
                                    data.put("receiverID", senderID)
                                    data.put("message", reply.toString())
                                    data.put("type", NotificationType.MESSAGE.value)

                                    CommonMethods().getTokens(senderID, data, context!!)

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
    }
}