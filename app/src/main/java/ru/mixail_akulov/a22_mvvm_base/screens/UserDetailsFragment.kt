package ru.mixail_akulov.a22_mvvm_base.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import ru.mixail_akulov.a22_mvvm_base.R
import ru.mixail_akulov.a22_mvvm_base.databinding.FragmentUserDetailsBinding
import ru.mixail_akulov.a22_mvvm_base.tasks.SuccessResult

class UserDetailsFragment : Fragment() {

    private lateinit var binding: FragmentUserDetailsBinding
    private val viewModel: UserDetailsViewModel by viewModels { factory() }

    // 8.12 Указываем какого пользовател необходимо загрузить
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadUser(requireArguments().getLong(ARG_USER_ID))
    }

    // 8.13 Отрисовываем экран
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailsBinding.inflate(layoutInflater, container, false)

        viewModel.actionShowToast.observe(viewLifecycleOwner, Observer {
            it.getValue()?.let { messageRes -> navigator().toast(messageRes) }
        })
        viewModel.actionGoBack.observe(viewLifecycleOwner, Observer {
            it.getValue()?.let { navigator().goBack() }
        })

        viewModel.state.observe(viewLifecycleOwner, Observer {
            binding.contentContainer.visibility = if (it.showContent) {
                val userDetails = (it.userDetailsResult as SuccessResult).data
                binding.userNameTextView.text = userDetails.user.name
                if (userDetails.user.photo.isNotBlank()) {
                    Glide.with(this)
                        .load(userDetails.user.photo)
                        .circleCrop()
                        .into(binding.photoImageView)
                } else {
                    Glide.with(this)
                        .load(R.drawable.ic_user_avatar)
                        .into(binding.photoImageView)
                }
                binding.userDetailsTextView.text = userDetails.details

                View.VISIBLE
            } else {
                View.GONE
            }

            binding.progressBar.visibility = if (it.showProgress) View.VISIBLE else View.GONE
            binding.deleteButton.isEnabled = it.enableDeleteButton
        })

        binding.deleteButton.setOnClickListener {
            viewModel.deleteUser()

            // 9.7 Показываем сообщение об удалении и возвращаемся назад,
            // т.к. страница теперь эта удалена.
//            navigator().toast(R.string.user_has_been_deleted)
//            navigator().goBack()
        }

        return binding.root
    }

    // 8.9 Т.к.фрагмент будет принимать параметры,то мы добавляем companion object, внутри которого
    // описываем метод newInstance()
    companion object {

        // 8.10 Создаем костанту
        private const val ARG_USER_ID = "ARG_USER_ID"

        fun newInstance(userId: Long): UserDetailsFragment {
            val fragment = UserDetailsFragment()

            // 8.11 для использования, при передаче id пользователя в аргументах,т.к. в деталях может быть большое
            //количество данных, а в bundle не рекоментуется передавать большое количество данных
            fragment.arguments = bundleOf(ARG_USER_ID to userId)
            return fragment
        }

    }
}