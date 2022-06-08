package hu.bme.aut.conicon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import hu.bme.aut.conicon.R
import hu.bme.aut.conicon.constants.NotificationType
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.ui.CommonMethods
import org.json.JSONObject

class UserAdapter(private val context: Context, private val listener: UserItemClickListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    var userElements = arrayListOf<AppUser>()

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var user: AppUser? = null

        var ivProfilePicture: ImageView = itemView.findViewById(R.id.ivProfilePicture)
        var tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        var btnFollow: Button = itemView.findViewById(R.id.btnFollow)
        var btnFollowOut: Button = itemView.findViewById(R.id.btnFollowOut)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onUserClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userElement = userElements[position]
        holder.user = userElement

        val auth = FirebaseAuth.getInstance()
        val userCollection = FirebaseFirestore.getInstance().collection("users")
        val uid = auth.currentUser?.uid.toString()

        if (userElement.photoUrl != null) {
            Picasso.get().load(userElement.photoUrl).into(holder.ivProfilePicture)
        } else {
            holder.ivProfilePicture.setImageDrawable(context.getDrawable(R.drawable.ic_profile))
        }

        holder.tvUsername.text = userElement.username

        when {
            uid == userElement.id -> {
                holder.btnFollow.visibility = View.GONE
                holder.btnFollowOut.visibility = View.GONE
            }
            userElement.followers.contains(uid) -> {
                holder.btnFollow.visibility = View.GONE
                holder.btnFollowOut.visibility = View.VISIBLE
            }
            else -> {
                holder.btnFollow.visibility = View.VISIBLE
                holder.btnFollowOut.visibility = View.GONE
            }
        }

        holder.btnFollow.setOnClickListener {
            userCollection.document(userElement.id).update("followers", FieldValue.arrayUnion(uid))
            userCollection.document(uid).update("following", FieldValue.arrayUnion(userElement.id))
            userElement.followers.add(uid)
            holder.btnFollow.visibility = View.GONE
            holder.btnFollowOut.visibility = View.VISIBLE

            val data = JSONObject()

            data.put("receiverID", userElement.id)
            data.put("type", NotificationType.FOLLOW.value)
            data.put("senderID", uid)

            CommonMethods().getTokens(userElement.id, data, context)
        }

        holder.btnFollowOut.setOnClickListener {
            userCollection.document(userElement.id).update("followers", FieldValue.arrayRemove(uid))
            userCollection.document(uid).update("following", FieldValue.arrayRemove(userElement.id))
            userElement.followers.remove(uid)
            holder.btnFollowOut.visibility = View.GONE
            holder.btnFollow.visibility = View.VISIBLE
        }
    }

    interface UserItemClickListener {
        fun onUserClicked(position: Int)
    }

    override fun getItemCount(): Int = userElements.size

    fun addAll(users: MutableList<AppUser>) {
        this.userElements.addAll(users)
        notifyDataSetChanged()
    }
}