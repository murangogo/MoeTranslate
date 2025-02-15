/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.translate

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.moe.moetranslator.R

class LanguageSelectionDialog(
    private val context: Context,
    private val type: Int,
    private val locales: List<CustomLocale>,
    private val onLanguageSelected: (CustomLocale) -> Unit)
{
    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_languages, null)
        val listView = dialogView.findViewById<ListView>(R.id.languages_list)

        val adapter = object : ArrayAdapter<CustomLocale>(context, android.R.layout.simple_list_item_1, locales) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = locales[position].getDisplayName()
                return view
            }
        }

        listView.adapter = adapter

        builder.setView(dialogView)
        builder.setTitle(if (type == 1) R.string.select_source_language else R.string.select_target_language)

        val dialog = builder.create()

        listView.setOnItemClickListener { _, _, position, _ ->
            onLanguageSelected(locales[position])
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
}