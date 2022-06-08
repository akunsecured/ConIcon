package hu.bme.aut.conicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.CommentElement
import hu.bme.aut.conicon.ui.CommonMethods

class PostCommentAdapter(
    private val listener: PostCommentListener,
    private val context: Context
) : RecyclerView.Adapter<PostCommentAdapter.PostCommentViewHolder>() {
    val commentElements = mutableListOf<CommentElement>()
    val linkedUsers = mutableListOf<AppUser>()
    private val userCollection = FirebaseFirestore.getInstance().collection("users")

    inner class PostCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnLongClickListener {
        var comment: CommentElement? = null

        var ivProfilePicture : CircleImageView = itemView.findViewById(R.id.ivProfilePicture)
        var tvUsername : TextView = itemView.findViewById(R.id.tvUsername)
        var tvMessage : TextView = itemView.findViewById(R.id.tvMessage)
        var tvDate : TextView = itemView.findViewById(R.id.tvDate)

        init {
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View?): Boolean {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val commentElement = commentElements[adapterPosition]
            if (commentElement.sentBy == uid) {
                listener.onCommentLongClicked(commentElement)
            }
            return true
        }
    }

    interface PostCommentListener {
        fun onCommentLongClicked(comment: CommentElement)
        fun viewProfile(userID: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCommentViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return PostCommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostCommentViewHolder, position: Int) {
        val commentElement = commentElements[position]
        val linkedUser = linkedUsers[position]

        holder.comment = commentElement

        if (linkedUser.photoUrl != null) {
            Picasso.get().load(linkedUser.photoUrl).into(holder.ivProfilePicture)
        } else {
            holder.ivProfilePicture.setImageDrawable(context.getDrawable(R.drawable.ic_profile))
        }

        holder.ivProfilePicture.setOnClickListener {
            listener.viewProfile(linkedUser.id)
        }

        holder.tvUsername.text = linkedUser.username

        holder.tvUsername.setOnClickListener {
            listener.viewProfile(linkedUser.id)
        }

        holder.tvMessage.text = commentElement.message

        holder.tvDate.text =
            if (commentElement.time != null)
                CommonMethods().formatConversationDate(commentElement.time!!.time)
            else
                CommonMethods().formatConversationDate(commentElement.sentTime)
    }

    override fun getItemCount(): Int = commentElements.size

    fun addComments(comments: List<CommentElement>) {
        for (comment in comments) {
            addComment(comment)
        }
    }

    fun addComment(comment: CommentElement) {
        var linkedUser: AppUser? = linkedUsers.find { user -> user.id == comment.sentBy }
        if (linkedUser == null) {
            userCollection.document(comment.sentBy).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    linkedUser = document.toObject(AppUser::class.java)
                    commentElements.add(comment)
                    linkedUsers.add(linkedUser!!)

                    notifyDataSetChanged()
                }
            }
        } else {
            commentElements.add(comment)
            linkedUsers.add(linkedUser!!)

            notifyDataSetChanged()
        }
    }
}