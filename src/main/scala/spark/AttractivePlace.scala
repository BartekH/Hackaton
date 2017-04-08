package spark

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.mongodb.casbah.commons.MongoDBObject
import db.MongoFactory
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.sql.SparkSession

/**
  * Created by bartosz on 08.04.17.
  */
class AttractivePlace {

  val DATE_DIV_NUMBER = 100000
  val db = List((1,"1520"), ())
  def calculate(mac:String):Double={
    val sc = SparkConfig.getSc()
    val spark: org.apache.spark.sql.SparkSession = SparkSession.builder().getOrCreate()
    import spark.implicits._
    import com.mongodb.casbah.Imports._
    val dbData = MongoFactory.beaconinfos.find.map(dd => toLP(dd)).toList.filter(dd => dd.label == placeToNumber(mac))

    val dataRdd = sc.parallelize(dbData)
    dataRdd.foreach(println)
    val numIterations = 100
    val stepSize = 0.000001
    val model = LinearRegressionWithSGD.train(dataRdd, numIterations, stepSize)
    val date = dateToNumber(new Date())
    val estimatedQueue = model.predict(date)
    if (estimatedQueue<1) 0 else estimatedQueue
  }
  def toLP(obj: MongoDBObject):LabeledPoint = {
    val mac = obj.getAs[String]("mac").get
    val date = obj.getAs[Date]("dateTime").get
    LabeledPoint(placeToNumber(mac), dateToNumber(date))
  }

  val placeToNumber = (mac:String) => mac match{
    case "12312dasda:asdas" => 5
    case "7C:4D:1D:5G:04" => 4
    case "7C:4D:1D:5G:03" => 3
    case "7C:4D:1D:5G:02" => 2
    case "7C:4D:1D:5G:01" => 1
  }

  val dateToNumber = (date:Date) =>{
    val localTimeFormat = new SimpleDateFormat("HH:mm")
    val time = localTimeFormat.format(date).replaceAll(":","")

    Vectors.dense(time.toInt)
  }

  val stringToDate = (date:String) => {
    val formatter = new SimpleDateFormat("yyyy/mm/dd ")
  }
}