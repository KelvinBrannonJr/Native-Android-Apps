package kbrannon.youpickfoodpicker.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kbrannon.youpickfoodpicker.databinding.ActivityYouPickFoodPickerDetailBinding
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel

class YouPickFoodPickerDetailActivity : AppCompatActivity() {
    private var binding: ActivityYouPickFoodPickerDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityYouPickFoodPickerDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarFoodPlaceDetail)

        var youPickFoodPickerDetailModel: YouPickFoodPickerModel? = null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            youPickFoodPickerDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as YouPickFoodPickerModel?
        }

        if(youPickFoodPickerDetailModel != null){
            setSupportActionBar(binding?.toolbarFoodPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = youPickFoodPickerDetailModel.title

            binding?.toolbarFoodPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }
            binding?.ivDetailPlaceImage?.setImageURI(Uri.parse(youPickFoodPickerDetailModel.image))
            binding?.tvDescription?.text = youPickFoodPickerDetailModel.description
            binding?.tvLocation?.text = youPickFoodPickerDetailModel.location

            binding?.btnViewOnMap?.setOnClickListener {
                val intent = Intent(this,MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,youPickFoodPickerDetailModel)
                startActivity(intent)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}