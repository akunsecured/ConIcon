package hu.bme.aut.conicon.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import hu.bme.aut.conicon.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class contains all of the methods which are commonly used
 */
class CommonMethods {
    /**
     * Regex for the validation of the username
     */
    val regexUsername = "^(?=[a-zA-Z0-9._])(?!.*[_.]{2})[^_.].*[^_.]\$".toRegex()

    /**
     * This method makes the showing of EditText errors easier
     * @param context Context
     * @param til Field that caused the error
     * @param message Message of the error
     */
    fun showEditTextError(context: Context, til: TextInputLayout, message: String) {
        til.error = message
        til.defaultHintTextColor = AppCompatResources.getColorStateList(context, R.color.error)
        til.requestFocus()
    }

    /**
     * This method clears the EditText error
     * @param context Context
     * @param til Field that's error message should be deleted
     */
    fun removeEditTextError(context: Context, til: TextInputLayout) {
        til.error = ""
        til.defaultHintTextColor = AppCompatResources.getColorStateList(context, R.color.orange)
    }

    /**
     * Checks if the Password field and it's confirm field texts are the same
     * @param tietPassword The EditText of the password's field
     * @param tietConfirmPassword The EditText of the password's confirm field
     * @param tilConfirmPassword The password's confirm field
     * @param context Context
     * @return If the fields match, it returns true
     */
    fun checkPasswordsMatch(
        tietPassword: TextInputEditText,
        tietConfirmPassword: TextInputEditText,
        tilConfirmPassword: TextInputLayout,
        context: Context
    ) : Boolean {
        return when {
            tietPassword.text.toString() != tietConfirmPassword.text.toString() -> {
                showEditTextError(context, tilConfirmPassword, "The two passwords do not match!")
                false
            }
            else -> true
        }
    }

    /**
     * Checks if the length of the given EditText's text is enough for the Firebase to store
     * @param tiet The EditText of the field
     * @param til The field
     * @param context Context
     * @param minLength Minimum length of the text
     * @param maxLength Maximum length of the text
     * @return True if the text is between the given parameters
     */
    fun checkEditTextLength(
            tiet: TextInputEditText,
            til: TextInputLayout,
            context: Context,
            minLength: Int,
            maxLength: Int
    ) : Boolean {
        return when {
            tiet.text.toString().length in minLength..maxLength -> true
            else -> {
                showEditTextError(context, til, "This field's length must be between $minLength and $maxLength characters!")
                false
            }
        }
    }

    /**
     * Checks if the length of the given EditText's text matches to the given regex pattern
     * @param tiet The EditText of the field
     * @param til The field
     * @param context Context
     * @param regex Regular expression
     * @return True if the text matches
     */
    fun validateEditTextCharactersByRegex(
            tiet: TextInputEditText,
            til: TextInputLayout,
            context: Context,
            regex: Regex
    ) : Boolean {
        return when {
            tiet.text.toString().matches(regex) -> true
            else -> {
                showEditTextError(context, til, "This field contains not allowed characters!")
                false
            }
        }
    }

    /**
     * This method validates the EditText to match the rules which are given as parameters
     * @param context Context
     * @param tiet The EditText of the field
     * @param til The field
     * @param minLength Minimum length of the text
     * @param maxLength Maximum length of the text
     * @param regex Regular expression
     */
    fun validateEditText(context: Context, tiet: TextInputEditText, til: TextInputLayout, minLength: Int, maxLength: Int, regex: Regex) : Boolean {
        if (checkEditTextLength(tiet, til, context, minLength, maxLength)) {
            if (validateEditTextCharactersByRegex(tiet, til, context, regex)) {
                removeEditTextError(context, til)
                return true
            }
        }

        return false
    }

    /**
     * This method formats the given date
     * @param date The Long form of the date to format
     */
    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: Long) : String {
        val diff = Date().time - date

        return when {
            diff < 86400001 -> {
                SimpleDateFormat("HH:mm").format(date)
            }
            diff in 86400001..604800000 -> {
                SimpleDateFormat("EEEE").format(date)
            }
            diff in 604800001..31556952000 -> {
                SimpleDateFormat("MMM. dd").format(date)
            }
            else -> {
                SimpleDateFormat("yyyy. MMM. dd.").format(date)
            }
        }
    }
}