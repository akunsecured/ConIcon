package hu.bme.aut.conicon.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.network.model.MediaElement

class UserMediaAdapter(private val listener: UserPostItemClickListener) : RecyclerView.Adapter<UserMediaAdapter.UserPostViewHolder>() {
    var userPostElements = mutableListOf<MediaElement>()

    inner class UserPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var userpost: MediaElement? = null

        var ivPostImage : ImageView = itemView.findViewById(R.id.ivPostImage)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onUserPostItemClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.user_posts_item, parent, false)
        return UserPostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int) {
        val userPostElement = userPostElements[position]
        holder.userpost = userPostElement
        Picasso.get().load(userPostElement.mediaLink).into(holder.ivPostImage)
    }

    interface UserPostItemClickListener {
        fun onUserPostItemClicked(position: Int)
    }

    override fun getItemCount(): Int = userPostElements.size

    fun update(posts: MutableList<MediaElement>) {
        this.userPostElements = posts.asReversed()
        notifyDataSetChanged()
    }
}