package com.lakehub.adherenceapp.repositories

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lakehub.adherenceapp.R
import com.lakehub.adherenceapp.activities.chv.ChvDashboardActivity
import com.lakehub.adherenceapp.activities.client.ClientHomeActivity
import com.lakehub.adherenceapp.data.Role
import com.lakehub.adherenceapp.data.User
import kotlinx.coroutines.tasks.await
import java.io.File

class UserRepository {

    companion object {
        const val USER_COLLECTION_REF = "users"
    }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val isAuthenticated get() = auth.currentUser?.uid != null

    val userId get() = auth.currentUser?.phoneNumber ?: throw IllegalStateException("Current user is not signed in")

    val userImageFilename get() = "$userId.jpeg"

    suspend fun signIn(credential: AuthCredential) {
        auth.signInWithCredential(credential).await()
    }

    suspend fun getCurrentUser() : User? {
        val snapshot = db.collection(USER_COLLECTION_REF).document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun validateAccessKey(accessKey: String): Boolean {
        return getCurrentUser()?.accessKey?.equals(accessKey, ignoreCase = true) ?: false
    }

    fun storeUserImageLocally(context: Context) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imgRef = storageRef.child("client_images/$userImageFilename")
            val mContextWrapper = ContextWrapper(context)
            val mDirectory: File = mContextWrapper.getDir(
                "user_images",
                Context.MODE_PRIVATE
            )
            val file = File(mDirectory, userImageFilename)
            imgRef.getFile(file)
    }

    fun updateAccessKey(hasAccessKey: Boolean, accessKey: String? = null) {
        val data = mutableMapOf<String, Any>(User::hasAccessKey.name to hasAccessKey)
        if(hasAccessKey && accessKey != null)
            data[User::accessKey.name] = accessKey

        db.collection(USER_COLLECTION_REF).document(userId).update(data)
    }

    fun updateActiveState(active: Boolean)  {
        val data = mutableMapOf<String, Any>(User::active.name to active)
        db.collection(USER_COLLECTION_REF).document(userId).update(data)
    }



}