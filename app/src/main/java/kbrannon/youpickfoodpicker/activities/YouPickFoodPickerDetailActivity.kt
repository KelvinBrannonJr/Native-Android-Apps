package kbrannon.youpickfoodpicker.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import kbrannon.youpickfoodpicker.R
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class YouPickFoodPickerDetailActivity : AppCompatActivity() {
    private var youPickFoodPickerDetailModel: YouPickFoodPickerModel? = null
    private var ivDetailPlaceImage: ImageView? = null
    private var tvDescription: TextView? = null
    private var tvLocation: TextView? = null
    private var btnViewOnMap: Button? = null
    private var toolbarFoodPlaceDetail: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_you_pick_food_picker_detail)

        toolbarFoodPlaceDetail = findViewById(R.id.toolbar_food_place_detail)
        ivDetailPlaceImage = findViewById(R.id.iv_detail_place_image)
        tvDescription = findViewById(R.id.tv_description)
        tvLocation = findViewById(R.id.tv_location)
        btnViewOnMap = findViewById(R.id.btn_view_on_map)


        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            youPickFoodPickerDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as YouPickFoodPickerModel?
        }

        if(youPickFoodPickerDetailModel != null){
            setSupportActionBar(toolbarFoodPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = youPickFoodPickerDetailModel!!.title

            toolbarFoodPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }


            ivDetailPlaceImage?.setImageURI(Uri.parse(youPickFoodPickerDetailModel!!.image))
            tvDescription?.text = youPickFoodPickerDetailModel!!.description
            tvLocation?.text = youPickFoodPickerDetailModel!!.location

            btnViewOnMap?.setOnClickListener {
                val intent = Intent(this@YouPickFoodPickerDetailActivity,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,youPickFoodPickerDetailModel)
                startActivity(intent)
            }
        }

    }

}