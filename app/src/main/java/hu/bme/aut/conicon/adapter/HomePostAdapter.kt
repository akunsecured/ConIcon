package hu.bme.aut.conicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods
import org.json.JSONObject

class HomePostAdapter(
    private val listener: HomePostListener,
    private val context: Context
) : RecyclerView.Adapter<HomePostAdapter.PostViewHolder>() {
    val mediaElements = mutableListOf<MediaElement>()
    val linkedUsers = mutableListOf<AppUser>()

    private val userCollection = FirebaseFirestore.getInstance().collection("users")

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var media: MediaElement? = null

        var ivProfilePicture: ImageView = itemView.findViewById(R.id.ivProfilePicture)
        var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        var tvPlace: TextView = itemView.findViewById(R.id.tvPlace)
        var ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        var btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        var btnComment: ImageView = itemView.findViewById(R.id.btnComment)
        var tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        var tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        var tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.media_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val mediaElement = mediaElements[position]
        val linkedUser = linkedUsers[position]

        holder.media = mediaElement

        val auth = FirebaseAuth.getInstance()
        val postReference = FirebaseFirestore.getInstance().collection("posts").document(mediaElement.id)
        val uid = auth.currentUser?.uid.toString()

        if (linkedUser.photoUrl != null) {
            Picasso.get().load(linkedUser.photoUrl).into(holder.ivProfilePicture)
        } else {
            holder.ivProfilePicture.setImageDrawable(context.getDrawable(R.drawable.ic_profile))
        }
        holder.ivProfilePicture.setOnClickListener {
            listener.viewProfile(mediaElement.ownerID)
        }

        holder.tvUsername.text = linkedUser.username
        holder.tvUsername.setOnClickListener {
            listener.viewProfile(mediaElement.ownerID)
        }

        if (mediaElement.postLocation != null) {
            holder.tvPlace.visibility = View.VISIBLE
            holder.tvPlace.text = mediaElement.postLocation.location

            holder.tvPlace.setOnClickListener {
                listener.viewLocation(
                    mediaElement.postLocation.lat!!,
                    mediaElement.postLocation.lng!!
                )
            }
        } else {
            holder.tvPlace.visibility = View.GONE
        }

        Picasso.get().load(mediaElement.mediaLink).into(holder.ivPostImage)

        if (!mediaElement.likes.contains(uid)) {
            holder.btnLike.setImageResource(R.drawable.ic_heart_empty)
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_heart_filled)
        }

        holder.btnLike.setOnClickListener {
            if (mediaElement.likes.contains(uid)) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_empty)

                mediaElement.likes.remove(uid)
                postReference.update("likes", FieldValue.arrayRemove(uid))
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled)

                val animation = AnimationUtils.loadAnimation(context, R.anim.anim_like_button)
                holder.btnLike.startAnimation(animation)

                mediaElement.likes.add(uid)
                postReference.update("likes", FieldValue.arrayUnion(uid))

                val data = JSONObject()

                data.put("receiverID", mediaElement.ownerID)
                data.put("mediaID", mediaElement.id)
                data.put("type", NotificationType.IMAGE_LIKE.value)
                data.put("senderID", uid)

                CommonMethods().getTokens(mediaElement.ownerID, data, context)
            }

            checkLikes(holder, mediaElement)
        }

        holder.btnComment.setOnClickListener {
            listener.viewComments(mediaElement)
        }

        checkLikes(holder, mediaElement)

        if (mediaElement.details != null) {
            holder.tvDetails.visibility = View.VISIBLE
            holder.tvDetails.text = mediaElement.details
        } else {
            holder.tvDetails.visibility = View.GONE
        }

        holder.tvDate.text = CommonMethods().formatPostDate(mediaElement.date)
    }

    private fun checkLikes(holder: HomePostAdapter.PostViewHolder, mediaElement: MediaElement) {
        holder.tvLikes.visibility = if (mediaElement.likes.size == 0) View.GONE else View.VISIBLE
        holder.tvLikes.text = "${mediaElement.likes.size} likes"
        holder.tvLikes.setOnClickListener {
            listener.viewLikes(mediaElement.likes)
        }
    }

    override fun getItemCount(): Int = mediaElements.size

    interface HomePostListener {
        fun viewLikes(likes: MutableList<String>)
        fun viewProfile(userID: String)
        fun viewLocation(lat: Double, lng: Double)
        fun viewComments(post: MediaElement)
    }

    fun addPosts(posts: List<MediaElement>) {
        for (post in posts) {
            addPost(post)
        }
    }

    private fun addPost(post: MediaElement) {
        userCollection.document(post.ownerID).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val linkedUser = document.toObject(AppUser::class.java)!!
                mediaElements.add(post)
                linkedUsers.add(linkedUser)

                notifyDataSetChanged()
            }
        }
    }
}