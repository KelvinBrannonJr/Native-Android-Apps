package kbrannon.youpickfoodpicker.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kbrannon.youpickfoodpicker.R
import kbrannon.youpickfoodpicker.utils.SwipeToDeleteCallback
import kbrannon.youpickfoodpicker.utils.SwipeToEditCallback
import kbrannon.youpickfoodpicker.adapters.YouPickFoodPickerAdapter
import kbrannon.youpickfoodpicker.database.DatabaseHandler
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class MainActivity : AppCompatActivity() {

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    private var toolbarMain: Toolbar? = null
    private var rvFoodPlaces: RecyclerView? = null
    private var tvNoRecordsAvailable: TextView? = null
    private var fabAddFoodPlace: FloatingActionButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbarMain = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbarMain)

        rvFoodPlaces = findViewById(R.id.rv_food_places)
        tvNoRecordsAvailable = findViewById(R.id.tv_no_records_available)
        fabAddFoodPlace = findViewById(R.id.fabAddFoodPlace)

        fabAddFoodPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, ChooseFoodPlace::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getFoodPlaceFromLocalDatabase()


    }

    private fun setupFoodPlacesRecyclerView(foodPlaceList: ArrayList<YouPickFoodPickerModel>){
        rvFoodPlaces?.layoutManager = LinearLayoutManager(this)

        rvFoodPlaces?.setHasFixedSize(true)

        val placesAdapter = YouPickFoodPickerAdapter(this,foodPlaceList)
        rvFoodPlaces?.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: YouPickFoodPickerAdapter.OnClickListener{
            override fun onClick(position: Int, model: YouPickFoodPickerModel) {
                val intent = Intent(this@MainActivity,YouPickFoodPickerDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvFoodPlaces?.adapter as YouPickFoodPickerAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvFoodPlaces)

        val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvFoodPlaces?.adapter as YouPickFoodPickerAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getFoodPlaceFromLocalDatabase()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvFoodPlaces)
    }

    private fun getFoodPlaceFromLocalDatabase(){
        val dbHandler = DatabaseHandler(this)
        val getFoodPlaceList: ArrayList<YouPickFoodPickerModel> = dbHandler.getFoodPlacesList()

        if(getFoodPlaceList.size > 0){
            rvFoodPlaces?.visibility = View.VISIBLE
            tvNoRecordsAvailable?.visibility = View.GONE
            setupFoodPlacesRecyclerView(getFoodPlaceList)
        }else{
            rvFoodPlaces?.visibility = View.GONE
            tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                getFoodPlaceFromLocalDatabase()
            }else{
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }
}