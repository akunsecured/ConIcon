package hu.bme.aut.conicon.ui.main.home

import androidx.lifecycle.viewModelScope
import co.zsmb.rainbowcake.base.RainbowCakeViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.conicon.network.model.AppUser
import hu.bme.aut.conicon.network.model.CommentElement
import hu.bme.aut.conicon.network.model.MediaElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(

) : RainbowCakeViewModel<HomeViewState>(Initialize) {
    fun init() {
        viewState = Initialize
    }

    fun getPosts() = viewModelScope.launch {
        viewState = Loading
        delay(1000)

        val posts = mutableListOf<MediaElement>()
        // val users = mutableListOf<AppUser>()
        // val usersReference = Firebase.database.reference.child("users")
        val postsReference = Firebase.database.reference.child("posts")

        postsReference.get().addOnSuccessListener { postsDataSnapshot ->
            if (postsDataSnapshot.hasChildren()) {
                val query = postsReference.orderByChild("date")

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChildren()) {
                            for (child in snapshot.children) {
                                val postHash = child.value as HashMap<*, *>

                                val id = postHash["id"] as String
                                val date = -(postHash["date"] as Long)
                                val ownerID = postHash["ownerID"] as String
                                val mediaLink = postHash["mediaLink"] as String

                                val likes = mutableListOf<String>()
                                if (postHash["likes"] != null) {
                                    val likesHash = postHash["likes"] as HashMap<*, *>

                                    for (key in likesHash.keys) {
                                        likes.add(key as String)
                                    }
                                }

                                val comments = mutableListOf<CommentElement>()
                                if (postHash["comments"] != null) {
                                    // TODO: Adding comments
                                }

                                var details: String? = null
                                if (postHash["details"] != null) {
                                    details = postHash["details"] as String
                                }

                                posts.add(
                                    MediaElement(
                                        id, date, ownerID, mediaLink, likes, comments, details
                                    )
                                )

                                /*
                                usersReference.child(ownerID).get().addOnSuccessListener { userDataSnapshot ->
                                    if (userDataSnapshot.value == null) {
                                        // If the post's owner does not exist, the post must be immediately removed!
                                        posts.removeLast()
                                    } else {
                                        val userHash = userDataSnapshot.value as HashMap<*, *>

                                        val username = userHash["username"] as String
                                        val email = userHash["email"] as String

                                        var photoUrl: String? = null
                                        if (userHash["photoUrl"] != null) {
                                            photoUrl = userHash["photoUrl"] as String
                                        }

                                        val followers = mutableListOf<String>()
                                        if (userHash["followers"] != null) {
                                            val followersHash = userHash["followers"] as HashMap<*, *>

                                            for (key in followersHash.keys) {
                                                followers.add(key as String)
                                            }
                                        }

                                        val following = mutableListOf<String>()
                                        if (userHash["following"] != null) {
                                            val followingHash = userHash["following"] as HashMap<*, *>

                                            for (key in followingHash.keys) {
                                                following.add(key as String)
                                            }
                                        }

                                        val posts = mutableListOf<String>()
                                        // TODO: Adding posts to user

                                        users.add(
                                            AppUser(
                                                ownerID, username, email, photoUrl, followers, following, posts
                                            )
                                        )
                                    }
                                }.addOnFailureListener { ex ->
                                    viewState = FirebaseError(ex.message.toString())
                                }
                                */
                            }
                        }

                        viewState = PostsReady(posts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        FirebaseError(error.message)
                    }

                })

                /*
                query.get().addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.childrenCount > 0) {
                        for (child in dataSnapshot.children) {
                            val postHash = child.value as HashMap<*, *>

                            val date = -(postHash["date"] as Long)
                            val ownerID = postHash["ownerID"] as String
                            val mediaLink = postHash["mediaLink"] as String

                            val likes = mutableListOf<String>()
                            if (postHash["likes"] != null) {
                                val likesHash = postHash["likes"] as HashMap<*, *>

                                for (key in likesHash.keys) {
                                    likes.add(key as String)
                                }
                            }

                            val comments = mutableListOf<CommentElement>()
                            if (postHash["comments"] != null) {
                                // TODO: Adding comments
                            }

                            var details: String? = null
                            if (postHash["details"] != null) {
                                details = postHash["details"] as String
                            }

                            posts.add(
                                MediaElement(
                                    date, ownerID, mediaLink, likes, comments, details
                                )
                            )

                            /*
                            usersReference.child(ownerID).get().addOnSuccessListener { userDataSnapshot ->
                                if (userDataSnapshot.value == null) {
                                    // If the post's owner does not exist, the post must be immediately removed!
                                    posts.removeLast()
                                } else {
                                    val userHash = userDataSnapshot.value as HashMap<*, *>

                                    val username = userHash["username"] as String
                                    val email = userHash["email"] as String

                                    var photoUrl: String? = null
                                    if (userHash["photoUrl"] != null) {
                                        photoUrl = userHash["photoUrl"] as String
                                    }

                                    val followers = mutableListOf<String>()
                                    if (userHash["followers"] != null) {
                                        val followersHash = userHash["followers"] as HashMap<*, *>

                                        for (key in followersHash.keys) {
                                            followers.add(key as String)
                                        }
                                    }

                                    val following = mutableListOf<String>()
                                    if (userHash["following"] != null) {
                                        val followingHash = userHash["following"] as HashMap<*, *>

                                        for (key in followingHash.keys) {
                                            following.add(key as String)
                                        }
                                    }

                                    val posts = mutableListOf<String>()
                                    // TODO: Adding posts to user

                                    users.add(
                                        AppUser(
                                            ownerID, username, email, photoUrl, followers, following, posts
                                        )
                                    )
                                }
                            }.addOnFailureListener { ex ->
                                viewState = FirebaseError(ex.message.toString())
                            }
                            */
                        }
                    }

                    viewState = PostsReady(posts)
                }.addOnFailureListener { ex ->
                    viewState = FirebaseError(ex.message.toString())
                }
            } else {
                viewState = PostsReady(posts)
            }
                 */
            } else {
                viewState = PostsReady(posts)
            }
        }.addOnFailureListener { ex ->
            viewState = FirebaseError(ex.message.toString())
        }

        // TODO: Query for all of the posts -> order them (negative timestamps are required), then filter them (owned by followed user)
    }
}