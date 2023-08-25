package woojin.projects.photopostactivity.ui.home

data class ArticleItem(
    val articleId: String,
    val description: String,
    var isBookMark: Boolean,
    val imageUrl: String,
)