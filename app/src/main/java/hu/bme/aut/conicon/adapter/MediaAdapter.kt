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
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.MediaElement

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

        if (linkedUser.photoUrl != null) {
            Picasso.get().load(linkedUser.photoUrl).into(holder.ivProfilePicture)
        }

        holder.tvUsername.text = linkedUser.username

        holder.tvPlace.visibility = View.GONE

        Picasso.get().load(mediaElement.mediaLink).into(holder.ivPostImage)

        holder.btnLike.setOnClickListener {
            val drawable = holder.btnLike.drawable

            if (drawable.constantState == AppCompatResources.getDrawable(context, R.drawable.ic_heart_empty)?.constantState) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled)
                holder.tvLikes.text = "${mediaElement.likes.size + 1} likes"
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_empty)
                holder.tvLikes.text = "${mediaElement.likes.size - 1} likes"
            }
        }

        holder.btnComment.setOnClickListener {
            Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show()
        }

        holder.tvLikes.text = "${mediaElement.likes.size} likes"
    }

    interface MediaItemClickListener {

    }

    override fun getItemCount(): Int = mediaElements.size
}