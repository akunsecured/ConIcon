package hu.bme.aut.conicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement
import hu.bme.aut.conicon.ui.CommonMethods

/**
 * This class is the Adapter of the posts' RecyclerView
 */
class MediaAdapter(private val context: Context, private val listener: MediaItemClickListener) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {
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
        val postReference = Firebase.database.reference.child("posts/${mediaElement.id}")
        val uid = auth.currentUser?.uid.toString()

        if (linkedUser.photoUrl != null) {
            Picasso.get().load(linkedUser.photoUrl).into(holder.ivProfilePicture)
        } else {
            holder.ivProfilePicture.setImageDrawable(context.getDrawable(R.drawable.ic_profile))
        }

        holder.tvUsername.text = linkedUser.username

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
                postReference.child("likes/$uid").removeValue()
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled)

                mediaElement.likes.add(uid)
                postReference.child("likes/$uid").setValue(true)
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
    }

    interface MediaItemClickListener {

    }

    override fun getItemCount(): Int = mediaElements.size

    fun addPosts(posts: MutableList<MediaElement>) {
        for (post in posts) {
            addPost(post)
        }
    }

    private fun addPost(post: MediaElement) {
        val usersReference = Firebase.database.reference.child("users")

        usersReference.child(post.ownerID).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.value != null) {
                val userHash = dataSnapshot.value as HashMap<*, *>

                val username = userHash["username"] as String
                var photoUrl: String? = null
                if (userHash["photoUrl"] != null) {
                    photoUrl = userHash["photoUrl"] as String
                }

                mediaElements.add(post)
                linkedUsers.add(
                    AppUser(
                        post.ownerID,
                        username,
                        "",
                        photoUrl,
                        mutableListOf(),
                        mutableListOf(),
                        mutableListOf()
                    )
                )

                notifyDataSetChanged()
            }
        }.addOnFailureListener { ex ->
            Toast.makeText(context, ex.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}