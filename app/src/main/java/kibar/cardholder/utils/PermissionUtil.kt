package kibar.cardholder.utils

import android.Manifest

sealed class PermissionUtil(val name: String, val permsArray: Array<String>) {

    companion object {
        fun createFromRequestCode(requestCode: Int): PermissionUtil? = when (requestCode) {
            Camera.requestCode -> Camera
            ReadExternalStorage.requestCode -> ReadExternalStorage
            else -> null
        }
    }

    open val requestCode: Int by lazy { hashCode() }

    abstract val storageKeyName: String
    open val askUserDialogTitle = "İzin gerekli"
    open val askUserDialogMessage = ""

    object Camera :
        PermissionUtil(Manifest.permission.CAMERA, permsArray = arrayOf(Manifest.permission.CAMERA)) {
        override val storageKeyName: String = "cameraPermissionAskedBefore"
        override val askUserDialogMessage: String = "Kamerayı kullanabilmek için izin vermelisiniz"
    }

    object ReadExternalStorage :
        PermissionUtil(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            permsArray = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
        override val storageKeyName: String = "readExternalStoragePermissionAskedBefore"
        override val askUserDialogMessage: String =
            "Depolama alanını kullanabilmek için izin vermelisiniz"
    }

}