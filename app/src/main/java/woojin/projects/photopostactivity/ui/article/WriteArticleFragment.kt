package woojin.projects.photopostactivity.ui.article

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import woojin.projects.photopostactivity.R
import woojin.projects.photopostactivity.data.ArticleModel
import woojin.projects.photopostactivity.databinding.FragmentWriteBinding
import java.util.UUID

class WriteArticleFragment : Fragment(R.layout.fragment_write) {

    private lateinit var binding: FragmentWriteBinding
    private lateinit var viewModel: WriteArticleViewModel

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                //ViewModel uri 주입
                viewModel.updateSelectedUri(uri)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteBinding.bind(view)

        setupViewModel()
        if (viewModel.selectedUri.value == null) {
            startPicker()
        }
        setupPhotoImageView()
        setupDeleteButton()
        setupSubmitButton(view)
        setupBackButton()

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get<WriteArticleViewModel>()

        viewModel.selectedUri.observe(viewLifecycleOwner) {
            //비동기로 호출
            binding.photoImageView.setImageURI(it)
            if (it != null) {
                binding.plusButton.isVisible = false
                binding.deleteButton.isVisible = true
            } else {
                binding.deleteButton.isVisible = false
                binding.plusButton.isVisible = true
            }
        }
    }

    private fun startPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun setupPhotoImageView() {
        binding.photoImageView.setOnClickListener {
            if (viewModel.selectedUri == null) {
                startPicker()
            }
        }
    }

    private fun setupDeleteButton() {
        binding.deleteButton.setOnClickListener {
            binding.photoImageView.setImageURI(null)
            viewModel.updateSelectedUri(null)

        }
    }

    private fun showProgress() {
        binding.progressBarLayout.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBarLayout.isVisible = false
    }

    private fun uploadImage(
        uri: Uri,
        successHandler: (String) -> Unit,
        errorHandler: (Throwable?) -> Unit
    ) {
        val fileName = "${UUID.randomUUID()}.png"
        Firebase.storage.reference.child("articles/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Firebase.storage.reference.child("articles/photo/$fileName")
                        .downloadUrl
                        .addOnSuccessListener {
                            //storage에 업로드한 이미지의 downloadUrl을 받아옴
                            successHandler(it.toString())
                        }.addOnFailureListener {
                            errorHandler(it)
                        }
                } else {
                    errorHandler(task.exception)
                }
            }
    }

    private fun setupSubmitButton(view: View) {
        binding.submitButton.setOnClickListener {
            showProgress()
            if (viewModel.selectedUri.value != null) {
                val photoUri = viewModel.selectedUri.value ?: return@setOnClickListener
                uploadImage(
                    photoUri,
                    successHandler = {
                        //람다식으로 uploadImage의 successHandler를 통해 uploadArticle 메소드 실행 ->
                        //이미지의 url과 작성글
                        uploadArticle(it, binding.descriptionEditText.text.toString())
                    },
                    errorHandler = {
                        Snackbar.make(view, "이미지 업로드에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                        hideProgress()
                    })

            } else {
                Snackbar.make(view, "이미지가 선택되지 않았습니다", Snackbar.LENGTH_SHORT).show()
                hideProgress()
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            val action = WriteArticleFragmentDirections.actionBack()
            findNavController().navigate(action)
        }
    }


    private fun uploadArticle(photoUrl: String, description: String) {
        val articleId = UUID.randomUUID().toString()
        val articleModel = ArticleModel(
            articleId = articleId,
            createdAt = System.currentTimeMillis(),
            description = description,
            imageUrl = photoUrl
        )
        Firebase.firestore.collection("articles").document(articleId)
            .set(articleModel)
            .addOnSuccessListener {
                findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragment())
                hideProgress()
            }.addOnFailureListener {
                it.printStackTrace()
                view?.let { view ->
                    Snackbar.make(view, "글 작성에 실패하였습니다.", Snackbar.LENGTH_SHORT).show()
                }
                hideProgress()
            }

        hideProgress()
    }
}