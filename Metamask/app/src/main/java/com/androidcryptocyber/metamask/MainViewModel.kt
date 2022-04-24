package com.androidcryptocyber.metamask

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidcryptocyber.metamask.databinding.DerivedAddressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel : ViewModel() {
    val mnemonic = MutableLiveData("")
    val password = MutableLiveData("")
    val isLoading = MutableLiveData(false)

    private var index = 0

    fun create(derivedAddresses: LinearLayout) {
        try {
            mnemonic.value = Utils.generateMnemonic()
            newDerivedAddress(derivedAddresses)
        } catch (e: IllegalStateException) {
            showToast(derivedAddresses.context, R.string.word_list_not_loaded)
        } catch (e: IllegalArgumentException) {
            showToast(derivedAddresses.context, R.string.invalid_entropy)
        }
    }

    fun newDerivedAddress(derivedAddresses: LinearLayout) {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val derivedAddress = Utils.generateDerivedAddress(
                mnemonic = mnemonic.value ?: "",
                password = password.value ?: "",
                index = index
            )
            withContext(Dispatchers.Main) {
                val layoutInflater = LayoutInflater.from(derivedAddresses.context)
                val derivedAddressBinding = DerivedAddressBinding.inflate(layoutInflater)
                val derivedAddressView =
                    getDerivedAddressView(derivedAddress, derivedAddressBinding)
                derivedAddresses.addView(derivedAddressView)
                scrollToBottom(derivedAddresses.parent.parent as ScrollView)
                isLoading.value = false
                index++
            }
        }
    }

    private fun getDerivedAddressView(
        derivedAddress: DerivedAddress,
        derivedAddressBinding: DerivedAddressBinding
    ): View {
        derivedAddressBinding.pathValue.setText(derivedAddress.path)
        derivedAddressBinding.addressValue.setText(derivedAddress.address)
        derivedAddressBinding.privateKeyValue.setText(derivedAddress.privateKey)

        derivedAddressBinding.path.setEndIconOnClickListener {
            val context = it.context
            setClipboard(context, context.getString(R.string.path), derivedAddress.path)
        }

        derivedAddressBinding.address.setEndIconOnClickListener {
            val context = it.context
            setClipboard(context, context.getString(R.string.address), derivedAddress.address)
        }

        derivedAddressBinding.privateKey.setEndIconOnClickListener {
            val context = it.context
            setClipboard(
                context,
                context.getString(R.string.private_key),
                derivedAddress.privateKey
            )
        }
        return derivedAddressBinding.root
    }

    private fun scrollToBottom(scrollView: ScrollView) {
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    fun setClipboard(context: Context, label: String, text: String) {
        val clipboardManager =
            context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)
        showToast(context, R.string.text_copied_to_clipboard)
    }

    private fun showToast(context: Context, message: Int) {
        Toast.makeText(
            context,
            context.getString(message),
            Toast.LENGTH_LONG
        ).show()
    }

    fun reset(derivedAddresses: LinearLayout) {
        mnemonic.value = ""
        password.value = ""
        index = 0
        derivedAddresses.removeAllViews()
    }
}