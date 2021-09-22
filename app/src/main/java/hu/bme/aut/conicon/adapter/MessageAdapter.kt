package hu.bme.aut.conicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.MessageElement
import hu.bme.aut.conicon.ui.CommonMethods

class MessageAdapter(options: FirestoreRecyclerOptions<MessageElement>,
                     private val context: Context,
                     private val listener: MessageListener)
    : FirestoreRecyclerAdapter<MessageElement, RecyclerView.ViewHolder>(options) {
    var messages = arrayListOf<MessageElement>()

    companion object {
        const val SENT_VIEWER_TYPE = 1
        const val RECEIVED_VIEWER_TYPE = 2
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        var message: MessageElement? = null

        var tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        var ivMessage: ImageView = itemView.findViewById(R.id.ivMessage)

        init {
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            return listener.onMessageLongClicked(adapterPosition)
        }
    }

    inner class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        var message: MessageElement? = null

        var tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        var ivMessage: ImageView = itemView.findViewById(R.id.ivMessage)

        init {
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            return listener.onMessageLongClicked(adapterPosition)
        }
    }

    interface MessageListener {
        fun onMessageLongClicked(position: Int) : Boolean
        fun scrollToLast()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SENT_VIEWER_TYPE) {
            val view = LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false)
            return SentViewHolder(view)
        }

        val view = LayoutInflater.from(context).inflate(R.layout.message_received, parent, false)
        return ReceivedViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: MessageElement) {
        messages.add(model)

        if (holder is SentViewHolder) {
            if (model.isItMedia && model.mediaLink != null) {
                holder.tvMessage.visibility = View.GONE
                holder.ivMessage.visibility = View.VISIBLE
                Picasso.get().load(model.mediaLink).into(holder.ivMessage)
            } else {
                holder.tvMessage.visibility = View.VISIBLE
                holder.ivMessage.visibility = View.GONE
                holder.tvMessage.text = model.message
            }
            holder.tvDate.text =
                    if(model.time != null) CommonMethods().formatMessageDate(model.time!!.time)
                    else CommonMethods().formatMessageDate(model.sentFromClient)
        } else {
            (holder as ReceivedViewHolder).tvDate.text =
                    if(model.time != null) CommonMethods().formatMessageDate(model.time!!.time)
                    else CommonMethods().formatMessageDate(model.sentFromClient)
            if (model.isItMedia && model.mediaLink != null) {
                holder.tvMessage.visibility = View.GONE
                holder.ivMessage.visibility = View.VISIBLE
                Picasso.get().load(model.mediaLink).into(holder.ivMessage)
            } else {
                holder.tvMessage.visibility = View.VISIBLE
                holder.ivMessage.visibility = View.GONE
                holder.tvMessage.text = model.message
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (snapshots[position].sentBy == FirebaseAuth.getInstance().uid) {
            return SENT_VIEWER_TYPE
        }

        return RECEIVED_VIEWER_TYPE
    }

    override fun onDataChanged() {
        super.onDataChanged()
        listener.scrollToLast()
    }
}