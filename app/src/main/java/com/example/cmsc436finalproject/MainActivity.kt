package com.example.cmsc436finalproject

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.cmsc436finalproject.databinding.ActivityMainBinding
import com.google.firebase.provider.FirebaseInitProvider


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init firebase APIs
        FirebaseInitProvider()
        Log.i("main activity", "firebase init")

        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        Log.i("main activity", "view inflated")

//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .add(R.id.container, AccountSettingsFragment())
//                .commitNow()
//        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // Log.d("focus", "touchevent")
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}