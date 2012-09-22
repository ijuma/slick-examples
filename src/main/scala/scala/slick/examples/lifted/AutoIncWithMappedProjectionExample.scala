package scala.slick.examples.lifted

import scala.slick.driver.{ExtendedProfile, H2Driver}
import java.sql.Timestamp

case class Place(
  id: Int,
  buildingNo: Option[String],
  buildingName: Option[String],
  address1: String,
  address2: Option[String],
  city: String,
  county: Option[String],
  country: String,
  postcode: String,
  latitude: Option[Double],
  longitude: Option[Double],
  updatedAt: Timestamp
)

class PlaceSchema(val driver: ExtendedProfile) {
  
  // Import the query language features from the driver
  import driver.simple._

  object Places extends Table[Place]("place") {
    
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def buildingNo = column[Option[String]]("building_no")
    def buildingName = column[Option[String]]("building_name")
    def address1 = column[String]("address1")
    def address2 = column[Option[String]]("address2")
    def city = column[String]("city")
    def county = column[Option[String]]("county")
    def country = column[String]("country")
    def postcode = column[String]("postcode")
    def latitude = column[Option[Double]]("latitude")
    def longitude = column[Option[Double]]("longitude")
    def updatedAt = column[Timestamp]("updated_at")

    def * = (id ~ buildingNo ~ buildingName ~ address1 ~ address2 ~ city ~ county ~ country ~ postcode ~
      latitude ~ longitude ~ updatedAt) <> (Place, Place.unapply _)

    def forInsert = buildingNo ~ buildingName ~ address1 ~ address2 ~ city ~ county ~ country ~ postcode ~
      latitude ~ longitude ~ updatedAt <> (
        { Place(0, _, _, _, _, _, _, _, _, _, _, _) },
        { p: Place => Some((p._2, p._3, p._4, p._5, p._6, p._7, p._8, p._9, p._10, p._11, p._12)) }
      ) returning id

    def insert(p: Place)(implicit session: Session): Int = forInsert.insert(p)

  }

  // Other tables go here

}

object AutoIncWithMappedProjectionExample {
  
  def main(args: Array[String]) {
    
    val placeSchema = new PlaceSchema(H2Driver)
    import placeSchema._
    import driver.simple._

    val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
    
    db withTransaction { session: Session =>
      
      implicit val implicitSession = session
      
      def get(id: Int) = (for(p <- Places if p.id === id) yield p).firstOption

      Places.ddl.create
      val id = Places.insert(Place(0, None, Some("The Shard"), "London Bridge Street", None, "London", None, "W1", "UK", None, None, new Timestamp(System.currentTimeMillis)))
      assert(id != 0)

      println(s"  Value for key '$id': ${get(id)}")      

    }
  }

}
