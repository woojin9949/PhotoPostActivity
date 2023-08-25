package woojin.projects.photopostactivity.ui.article

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import woojin.projects.photopostactivity.R
import woojin.projects.photopostactivity.data.ArticleModel
import woojin.projects.photopostactivity.databinding.FragmentArticleDetailBinding

class ArticleDetailFragment : Fragment(R.layout.fragment_article_detail) {

    private lateinit var binding: FragmentArticleDetailBinding
    private val args: ArticleDetailFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentArticleDetailBinding.bind(view)
        val articleId = args.articleId

        binding.toolbar.setupWithNavController(findNavController())

        Firebase.firestore.collection("articles").document(articleId)
            .get()
            .addOnSuccessListener {
                val model = it.toObject<ArticleModel>()
                binding.descriptionTextView.text = model?.description

                Glide.with(binding.thumbnailImageView)
                    .load(model?.imageUrl)
                    .into(binding.thumbnailImageView)

            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

}