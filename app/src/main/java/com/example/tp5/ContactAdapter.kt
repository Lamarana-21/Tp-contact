package com.example.tp5

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView

class ContactAdapter(
    context: Context,
    contacts: List<Contact>
) : ArrayAdapter<Contact>(context, 0, contacts) {

    private class ViewHolder(view: View) {
        val imgPhoto: ShapeableImageView = view.findViewById(R.id.imgContactPhoto)
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val contact = getItem(position)
        if (contact != null) {
            val fullName = "${contact.nom} ${contact.prenom}".trim()
            holder.tvName.text = fullName
            holder.tvPhone.text = contact.telephone

            ImageUtils.loadPhoto(context, contact.photoUri, holder.imgPhoto)
        }

        return view
    }
}
