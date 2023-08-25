package woojin.projects.photopostactivity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import woojin.projects.photopostactivity.R
import woojin.projects.photopostactivity.data.ArticleModel
import woojin.projects.photopostactivity.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var articleAdapter: HomeArticleAdapter
    private var uid = Firebase.auth.currentUser?.uid.orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupWriteButton(view)
        setupBookmarkButton()
        setupRecyclerView()
        fetchFirestoreData()
    }

    private fun fetchFirestoreData() {
        Firebase.firestore.collection("bookmark").document(uid).get()
            .addOnSuccessListener {
                val bookMarkList = it.get("articleIds") as? List<*>

                Firebase.firestore.collection("articles")
                    .get()
                    .addOnSuccessListener { result ->
                        val list = result.map { snapshot -> snapshot.toObject<ArticleModel>() }
                            .map { model ->
                                ArticleItem(
                                    articleId = model.articleId.orEmpty(),
                                    description = model.description.orEmpty(),
                                    imageUrl = model.imageUrl.orEmpty(),
                                    isBookMark = bookMarkList?.contains(model.articleId.orEmpty())
                                        ?: false
                                )
                            }
                        articleAdapter.submitList(list)
                    }
            }
    }


    private fun setupBookmarkButton() {
        binding.bookmarkImageButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToBookMarkArticleFragment())
        }
    }

    private fun setupWriteButton(view: View) {
        binding.writeButton.setOnClickListener {
            if (Firebase.auth.currentUser != null) {
                val action = HomeFragmentDirections.actionHomeFragmentToWriteArticleFragment()
                findNavController().navigate(action)
            } else {
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    private fun setupRecyclerView() {
        articleAdapter = HomeArticleAdapter(
            onItemClicked = {
                //게시글 클릭시 navigation Attribute를 통해 정보 대입 가능
                //HomeFragment -----> ArticleDetailFragment
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(
                        articleId = it.articleId.orEmpty()
                    )
                )
            },
            onBookmarkClicked = { articleId, isBookmark ->
                Firebase.firestore.collection("bookmark").document(uid)
                    .update(
                        "articleIds",
                        if (isBookmark) {
                            FieldValue.arrayUnion(articleId)
                        } else {
                            FieldValue.arrayRemove(articleId)
                        }
                    ).addOnFailureListener {
                        if (it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                            if (isBookmark) {
                                Firebase.firestore.collection("bookmark").document(uid)
                                    .set(
                                        hashMapOf(
                                            "articleIds" to listOf(articleId)
                                        )
                                    )
                            }
                        }
                    }
            }
        )

        binding.homeRecyclerView.apply {
            adapter = articleAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }
}