package hu.bme.aut.conicon.di

import androidx.lifecycle.ViewModel
import co.zsmb.rainbowcake.dagger.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import hu.bme.aut.conicon.ui.chat.ChatViewModel
import hu.bme.aut.conicon.ui.conversations.ConversationsViewModel
import hu.bme.aut.conicon.ui.editprofile.EditProfileViewModel
import hu.bme.aut.conicon.ui.likes.UsersViewModel
import hu.bme.aut.conicon.ui.login.LoginViewModel
import hu.bme.aut.conicon.ui.main.MainViewModel
import hu.bme.aut.conicon.ui.main.home.HomeViewModel
import hu.bme.aut.conicon.ui.main.postupload.PostUploadViewModel
import hu.bme.aut.conicon.ui.main.profile.ProfileViewModel
import hu.bme.aut.conicon.ui.post.PostViewModel
import hu.bme.aut.conicon.ui.search.SearchViewModel
import hu.bme.aut.conicon.ui.setusername.SetUsernameViewModel
import hu.bme.aut.conicon.ui.signup.SignUpViewModel

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(signUpViewModel: SignUpViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetUsernameViewModel::class)
    abstract fun bindSetUsernameViewModel(setUsernameViewModel: SetUsernameViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(homeViewModel: HomeViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PostUploadViewModel::class)
    abstract fun bindPostUploadViewModel(postUploadViewModel: PostUploadViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UsersViewModel::class)
    abstract fun bindUsersViewModel(usersViewModel: UsersViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConversationsViewModel::class)
    abstract fun bindConversationsViewModel(conversationsViewModel: ConversationsViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun bindChatViewModel(chatViewModel: ChatViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PostViewModel::class)
    abstract fun bindPostViewModel(postViewModel: PostViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    abstract fun bindEditProfileViewModel(editProfileViewModel: EditProfileViewModel) : ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel) : ViewModel
}