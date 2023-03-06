package kbrannon.youpickfoodpicker.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kbrannon.youpickfoodpicker.utils.SwipeToDeleteCallback
import kbrannon.youpickfoodpicker.utils.SwipeToEditCallback
import kbrannon.youpickfoodpicker.adapters.YouPickFoodPickerAdapter
import kbrannon.youpickfoodpicker.database.DatabaseHandler
import kbrannon.youpickfoodpicker.databinding.ActivityMainBinding
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarMain)


        binding?.fabAddFoodPlace?.setOnClickListener{
            val intent = Intent(this@MainActivity, ChooseFoodPlace::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
    getFoodPlaceFromLocalDatabase()
    }

    private fun setupFoodPlacesRecyclerView(foodPlaceList: ArrayList<YouPickFoodPickerModel>){
        binding?.rvFoodPlaces?.layoutManager = LinearLayoutManager(this)

        binding?.rvFoodPlaces?.setHasFixedSize(true)

        val placesAdapter = YouPickFoodPickerAdapter(this,foodPlaceList)
        binding?.rvFoodPlaces?.adapter = placesAdapter

        placesAdapter.setOnClickListener(object: YouPickFoodPickerAdapter.OnClickListener{
            override fun onClick(position: Int, model: YouPickFoodPickerModel) {
                val intent = Intent(this@MainActivity,YouPickFoodPickerDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object: SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvFoodPlaces?.adapter as YouPickFoodPickerAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvFoodPlaces)

        val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvFoodPlaces?.adapter as YouPickFoodPickerAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getFoodPlaceFromLocalDatabase()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvFoodPlaces)
    }

    private fun getFoodPlaceFromLocalDatabase(){
        val dbHandler = DatabaseHandler(this)
        val getFoodPlaceList: ArrayList<YouPickFoodPickerModel> = dbHandler.getFoodPlacesList()

        if(getFoodPlaceList.size > 0){
            binding?.rvFoodPlaces?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupFoodPlacesRecyclerView(getFoodPlaceList)
        }else{
            binding?.rvFoodPlaces?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}