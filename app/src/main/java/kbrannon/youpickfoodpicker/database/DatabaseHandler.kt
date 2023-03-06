package kbrannon.youpickfoodpicker.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel


class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION){

    companion object {
        private const val  DATABASE_VERSION = 1
        private const val DATABASE_NAME = "YouPickFoodPickerDatabase"
        private const val TABLE_NAME = "YouPickFoodPickerTable"

        // All Columns names
        private const val UID_COL = "_id"
        private const val TITLE_COL = "title"
        private const val IMAGE_COL = "image"
        private const val DESCRIPTION_COL = "description"
        private const val DATE_COL = "date"
        private const val LOCATION_COL = "location"
        private const val LATITUDE_COL = "latitude"
        private const val LONGITUDE_COL = "longitude"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        // creating table with fields
        val createYouPickFoodPickerTable = ("CREATE TABLE " + TABLE_NAME + "("
                + UID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE_COL + " TEXT,"
                + IMAGE_COL + " TEXT,"
                + DESCRIPTION_COL + " TEXT,"
                + DATE_COL + " TEXT,"
                + LOCATION_COL + " TEXT,"
                + LATITUDE_COL + " TEXT,"
                + LONGITUDE_COL + " TEXT" + ")")
        db?.execSQL(createYouPickFoodPickerTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Function to insert AddFoodPlace details to SQLite Database
    fun addFoodPlace(newPlace: YouPickFoodPickerModel): Long {
        val contentValues = ContentValues()
        contentValues.put(TITLE_COL, newPlace.title) // YouPickFoodPickerModel class property 'title'
        contentValues.put(IMAGE_COL, newPlace.image) // YouPickFoodPickerModel class property 'image'
        contentValues.put(DESCRIPTION_COL, newPlace.description) // YouPickFoodPickerModel class property 'description'
        contentValues.put(DATE_COL, newPlace.date) // YouPickFoodPickerModel class property 'data'
        contentValues.put(LOCATION_COL, newPlace.location) // YouPickFoodPickerModel class property 'location'
        contentValues.put(LATITUDE_COL, newPlace.latitude) // YouPickFoodPickerModel class property 'latitude'
        contentValues.put(LONGITUDE_COL, newPlace.longitude) // YouPickFoodPickerModel class property 'longitude'

        val db = this.writableDatabase

        val result = db.insert(TABLE_NAME,null,contentValues)
        db.close()

        return result
    }

    fun updateFoodPlace(newPlace: YouPickFoodPickerModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(TITLE_COL, newPlace.title) // YouPickFoodPickerModel class property 'title'
        contentValues.put(IMAGE_COL, newPlace.image) // YouPickFoodPickerModel class property 'image'
        contentValues.put(DESCRIPTION_COL, newPlace.description) // YouPickFoodPickerModel class property 'description'
        contentValues.put(DATE_COL, newPlace.date) // YouPickFoodPickerModel class property 'data'
        contentValues.put(LOCATION_COL, newPlace.location) // YouPickFoodPickerModel class property 'location'
        contentValues.put(LATITUDE_COL, newPlace.latitude) // YouPickFoodPickerModel class property 'latitude'
        contentValues.put(LONGITUDE_COL, newPlace.longitude) // YouPickFoodPickerModel class property 'longitude'

        val success = db.update(TABLE_NAME,contentValues, UID_COL + "=" + newPlace.id, null)
        db.close()

        return success
    }

    fun deleteFoodPlace(foodPlace: YouPickFoodPickerModel): Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_NAME, UID_COL + "=" + foodPlace.id, null)
        db.close()
        return success
    }

    fun getFoodPlacesList(): ArrayList<YouPickFoodPickerModel>{
        val foodPlaceList = ArrayList<YouPickFoodPickerModel>()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase

        try{
            val cursor: Cursor = db.rawQuery(selectQuery,null)

            if(cursor.moveToFirst()){
                do{
                    val place = YouPickFoodPickerModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(UID_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DATE_COL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_COL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE_COL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE_COL))
                    )
                    foodPlaceList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return foodPlaceList
    }
}
