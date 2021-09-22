package hu.bme.aut.conicon.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.ConversationElement
import hu.bme.aut.conicon.ui.CommonMethods

class ConversationAdapter(
    private val listener: ConversationItemListener,
    options: FirestoreRecyclerOptions<ConversationElement>
) : FirestoreRecyclerAdapter<ConversationElement, ConversationAdapter.ConversationViewHolder>(options) {
    var conversationElements = mutableListOf<ConversationElement>()

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var conversation: ConversationElement? = null

        var ivProfilePicture: CircleImageView = itemView.findViewById(R.id.ivProfilePicture)
        var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        var tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        var tvLastMessageDate: TextView = itemView.findViewById(R.id.tvLastMessageDate)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onConversationItemClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.conversation_item, parent, false)
        return ConversationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int, model: ConversationElement) {
        conversationElements.add(model)
        holder.conversation = model

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        var userID = ""
        for (key in model.participantIDs.keys) {
            if (key != uid) {
                userID = key
                break
            }
        }

        val collection = FirebaseFirestore.getInstance().collection("users")
        collection.document(userID).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(AppUser::class.java)
                if (user != null) {
                    if (user.photoUrl != null) {
                        Picasso.get().load(user.photoUrl).into(holder.ivProfilePicture)
                    }

                    holder.tvUsername.text = user.username
                }
            }
        }

        val lastMessage = model.lastMessage!!
        val lastMessageText =
                if (lastMessage.sentBy == uid) "You: ".plus(
                        if (lastMessage.isItMedia) "Photo has been sent"
                        else lastMessage.message
                )
                else
                    if (lastMessage.isItMedia) "Photo has been sent"
                    else lastMessage.message
        holder.tvLastMessage.text = lastMessageText
        holder.tvLastMessageDate.text = CommonMethods().formatConversationDate(lastMessage.time!!.time)
    }

    interface ConversationItemListener {
        fun onConversationItemClicked(position: Int)
        fun onConversationSizeChange()
    }

    override fun onDataChanged() {
        super.onDataChanged()
        listener.onConversationSizeChange()
    }
}