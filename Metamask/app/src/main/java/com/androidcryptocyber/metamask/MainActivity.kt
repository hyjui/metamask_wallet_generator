package com.androidcryptocyber.metamask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.androidcryptocyber.metamask.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main) as ActivityMainBinding
        binding.lifecycleOwner = this

        val mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.vm = mainViewModel

        binding.password.setEndIconOnClickListener {
            mainViewModel.setClipboard(this, getString(R.string.password), binding.passwordValue.text.toString())
        }

        binding.mnemonic.setEndIconOnClickListener {
            mainViewModel.setClipboard(this, getString(R.string.mnemonic), binding.mnemonicValue.text.toString())
        }
    }
}