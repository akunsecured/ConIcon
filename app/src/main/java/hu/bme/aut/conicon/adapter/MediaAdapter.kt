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
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods

/**
 * This class is the Adapter of the posts' RecyclerView
 */
class MediaAdapter(private val context: Context, private val listener: MediaItemListener) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
    var mediaElements = arrayListOf<MediaElement>()
    var linkedUsers = arrayListOf<AppUser>()

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.media_item, parent, false)
        return MediaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
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

        holder.tvPlace.visibility = View.GONE

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
            }

            checkLikes(holder, mediaElement)
        }

        holder.btnComment.setOnClickListener {
            Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show()
        }

        checkLikes(holder, mediaElement)

        if (mediaElement.details != null) {
            holder.tvDetails.visibility = View.VISIBLE
            holder.tvDetails.text = mediaElement.details
        } else {
            holder.tvDetails.visibility = View.GONE
        }

        holder.tvDate.text = CommonMethods().formatDate(mediaElement.date)
    }

    private fun checkLikes(holder: MediaViewHolder, mediaElement: MediaElement) {
        holder.tvLikes.visibility = if (mediaElement.likes.size == 0) View.GONE else View.VISIBLE
        holder.tvLikes.text = "${mediaElement.likes.size} likes"
        holder.tvLikes.setOnClickListener {
            listener.viewLikes(mediaElement.likes)
        }
    }

    interface MediaItemListener {
        fun viewLikes(likes: MutableList<String>)
        fun viewProfile(userID: String)
    }

    override fun getItemCount(): Int = mediaElements.size

    fun addPosts(posts: MutableList<MediaElement>) {
        for (post in posts) {
            addPost(post)
        }
    }

    private fun addPost(post: MediaElement) {
        val userCollection = FirebaseFirestore.getInstance().collection("users")
        userCollection.document(post.ownerID).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val userDocument = document.data as Map<String, Any>
                val username = userDocument["username"].toString()
                val email = userDocument["email"].toString()
                val photoUrl = userDocument["photoUrl"] as String?
                val followers = userDocument["followers"] as MutableList<String>
                val following = userDocument["following"] as MutableList<String>
                val posts = userDocument["posts"] as MutableList<String>

                mediaElements.add(post)
                linkedUsers.add(
                        AppUser(
                                post.ownerID,
                                username, email, photoUrl, followers, following, posts
                        )
                )

                notifyDataSetChanged()
            }
        }.addOnFailureListener { ex ->
            Toast.makeText(context, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}