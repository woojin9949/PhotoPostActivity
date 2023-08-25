package woojin.projects.photopostactivity.ui.article

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WriteArticleViewModel : ViewModel() {

    private var _selectedUri = MutableLiveData<Uri?>()

    //State를 사용하는 추세 ->Compose에서도 원할하게 사용함
    var selectedUri: LiveData<Uri?> = _selectedUri

    fun updateSelectedUri(uri: Uri?) {
        _selectedUri.value = uri
    }

}