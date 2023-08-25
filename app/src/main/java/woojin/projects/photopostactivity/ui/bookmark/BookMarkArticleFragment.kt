package woojin.projects.photopostactivity.ui.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import woojin.projects.photopostactivity.R
import woojin.projects.photopostactivity.data.ArticleModel
import woojin.projects.photopostactivity.databinding.FragmentBookmarkArticleBinding

class BookMarkArticleFragment : Fragment(R.layout.fragment_bookmark_article) {

    private lateinit var binding: FragmentBookmarkArticleBinding
    private lateinit var bookmarkAdapter: BookMarkArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookmarkArticleBinding.bind(view)

        binding.toolbar.setupWithNavController(findNavController())

        bookmarkAdapter = BookMarkArticleAdapter {
            findNavController().navigate(
                BookMarkArticleFragmentDirections.actionBookMarkArticleFragmentToArticleDetailFragment(
                    articleId = it.articleId.orEmpty()
                )
            )
        }

        binding.articleRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter
            val uid = Firebase.auth.currentUser?.uid.orEmpty()
            Firebase.firestore.collection("bookmark")
                .document(uid)
                .get()
                .addOnSuccessListener {
                    val list = it.get("articleIds") as List<*>
                    if (list.isNotEmpty()) {
                        Firebase.firestore.collection("articles")
                            .whereIn("articleId", list)
                            .get()
                            .addOnSuccessListener { result ->
                                bookmarkAdapter.submitList(result.toObjects(ArticleModel::class.java))
                            }.addOnFailureListener { e ->
                                e.printStackTrace()
                            }
                    }
                }.addOnFailureListener {
                    it.printStackTrace()
                }
        }
    }
}