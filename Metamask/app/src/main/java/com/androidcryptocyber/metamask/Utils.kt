package com.androidcryptocyber.metamask

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.MnemonicUtils
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Utils {
    /**
     * @throws IllegalStateException    – If the word list has not been loaded.
     * @throws IllegalArgumentException – If the given entropy is invalid.
     */
    fun generateMnemonic(): String {
        val initialEntropy = ByteArray(16)
        SecureRandom().nextBytes(initialEntropy)
        return MnemonicUtils.generateMnemonic(initialEntropy)
    }

    suspend fun generateDerivedAddress(mnemonic: String, password: String = "", index: Int = 0) =
        suspendCoroutine<DerivedAddress> { cont ->
            val credentials = createCredentials(mnemonic, password, index)
            cont.resume(
                DerivedAddress(
                    address = credentials.address,
                    path = getStringPathForIndex(index),
                    privateKey = privateKeyToString(credentials.ecKeyPair.privateKey)
                )
            )
        }

    private fun createCredentials(
        mnemonic: String,
        password: String = "",
        index: Int = 0
    ): Credentials {
        // https://stackoverflow.com/questions/52107608/how-to-use-mnemonic-to-recovery-my-ethereum-wallet
        val seed = MnemonicUtils.generateSeed(mnemonic, password)
        val masterKeypair = Bip32ECKeyPair.generateKeyPair(seed)
        val path = getPathForIndex(index)
        val childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path)
        return Credentials.create(childKeypair)
    }

    private fun getPathForIndex(index: Int) = intArrayOf(
        44 or Bip32ECKeyPair.HARDENED_BIT,
        60 or Bip32ECKeyPair.HARDENED_BIT,
        0 or Bip32ECKeyPair.HARDENED_BIT,
        0,
        index
    )

    private fun getStringPathForIndex(index: Int) = "m/44'/0'/0'/$index"
    private fun privateKeyToString(privateKey: BigInteger): String =
        Numeric.toHexStringWithPrefixZeroPadded(privateKey, Keys.PRIVATE_KEY_LENGTH_IN_HEX)
}