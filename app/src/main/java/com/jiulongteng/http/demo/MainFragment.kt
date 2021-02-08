package com.jiulongteng.http.demo

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.trello.rxlifecycle4.components.support.RxFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

public class MainFragment: RxFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO){

        }

    }
}