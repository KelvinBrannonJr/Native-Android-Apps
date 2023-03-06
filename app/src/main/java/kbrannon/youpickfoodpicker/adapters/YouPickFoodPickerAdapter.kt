package kbrannon.youpickfoodpicker.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import kbrannon.youpickfoodpicker.R
import kbrannon.youpickfoodpicker.activities.ChooseFoodPlace
import kbrannon.youpickfoodpicker.activities.MainActivity
import kbrannon.youpickfoodpicker.database.DatabaseHandler
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class YouPickFoodPickerAdapter(private val context: Context, private var list: ArrayList<YouPickFoodPickerModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_food_place,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<CircleImageView>(R.id.civ_place_image)?.setImageURI(Uri.parse(model.image))
            holder.itemView.findViewById<TextView>(R.id.tv_title)?.text = model.title
            holder.itemView.findViewById<TextView>(R.id.tv_description)?.text = model.description

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, ChooseFoodPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
    }

    fun removeAt(position: Int) {

        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteFoodPlace(list[position])

        if (isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: YouPickFoodPickerModel)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
