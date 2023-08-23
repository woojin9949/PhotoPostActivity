package woojin.projects.photopostactivity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import woojin.projects.photopostactivity.databinding.FragmentAuthBinding

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var binding: FragmentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAuthBinding.bind(view)

        setupSignUpButton()
        setupSignInOutButton()
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser == null) {
            //로그아웃 상태
            initViewsToSignOutState()
        } else {
            initViewsToSignInState()
        }
    }

    private fun setupSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "이메일 혹은 패스워드 입력을 다시 해주세요.", Snackbar.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(binding.root, "회원가입 성공하였습니다.", Snackbar.LENGTH_SHORT).show()
                        initViewsToSignInState()
                    } else {
                        Snackbar.make(binding.root, "회원가입 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupSignInOutButton() {
        binding.signInOutButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (Firebase.auth.currentUser == null) {
                //로그인해야함
                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(binding.root, "이메일 혹은 패스워드 입력을 다시 해주세요.", Snackbar.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            initViewsToSignInState()
                            Snackbar.make(
                                binding.root,
                                "${email.substringBefore("@")}님 환영합니다",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "로그인 실패, 이메일 혹은 패스워드를 확인해주세요",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }.addOnFailureListener {
                        it.printStackTrace()
                    }
            } else {
                Firebase.auth.signOut()
                initViewsToSignOutState()
            }
        }
    }

    /*
    * 로그인상태
    * */
    private fun initViewsToSignInState() {
        binding.emailEditText.setText(Firebase.auth.currentUser?.email)
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isVisible = false
        binding.signInOutButton.text = getString(R.string.signOut)
        binding.signUpButton.isEnabled = false
    }

    /*
    * 로그아웃상태
    * */
    private fun initViewsToSignOutState() {
        binding.emailEditText.text.clear()
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.isVisible = true
        binding.passwordEditText.text.clear()
        binding.signInOutButton.text = getString(R.string.signIn)
        binding.signUpButton.isEnabled = true
    }
}