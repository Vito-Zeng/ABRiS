/*
 * Copyright 2018 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.abris.avro.read

import java.lang.Float
import java.lang.Long
import java.lang.Integer
import java.lang.Double
import java.lang.String
import java.lang.Boolean

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.scalatest.FlatSpec
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import za.co.absa.abris.examples.data.generation.TestSchemas
import za.co.absa.abris.examples.data.generation.AvroDataUtils
import scala.collection._
import org.apache.spark.sql.Row
import org.apache.avro.generic.GenericRecord
import org.apache.avro.Schema.Field
import java.util.HashMap
import java.util.TreeMap
import scala.collection.mutable.ListBuffer

class ScalaDatumReaderSpec extends FlatSpec {

  behavior of "ScalaDatumReader"

  it should "read collections and arrays as mutable.ListBuffer[Any]" in {
    val list = immutable.Map[String, Object]("array" -> new ArrayList(Arrays.asList("elem1", "elem2")))
    val set = immutable.Map[String, Object]("array" -> new HashSet[String](Arrays.asList("elem1", "elem2")))

    val listRecord: GenericRecord = AvroDataUtils.mapToGenericRecord(list, TestSchemas.ARRAY_SCHEMA_SPEC)
    val setRecord = AvroDataUtils.mapToGenericRecord(set, TestSchemas.ARRAY_SCHEMA_SPEC)

    assert(listRecord.get(0).isInstanceOf[mutable.ListBuffer[Any]])
    assert(setRecord.get(0).isInstanceOf[mutable.ListBuffer[Any]])
  }

  it should "read native types as Java types" in {
    val natives = immutable.Map[String, Object](
      "string" -> "A Test String",
      "int" -> new Integer(Integer.MAX_VALUE),
      "long" -> new Long(Long.MAX_VALUE),
      "double" -> new Double(Double.MAX_VALUE),
      "float" -> new Float(Float.MAX_VALUE),
      "boolean" -> new Boolean(true))

    val nativesRecord: GenericRecord = AvroDataUtils.mapToGenericRecord(natives, TestSchemas.NATIVE_SCHEMA_SPEC)

    assert(nativesRecord.get(0).isInstanceOf[java.lang.String])
    assert(nativesRecord.get(1).isInstanceOf[java.lang.Integer])
    assert(nativesRecord.get(2).isInstanceOf[java.lang.Long])
    assert(nativesRecord.get(3).isInstanceOf[java.lang.Double])
    assert(nativesRecord.get(4).isInstanceOf[java.lang.Float])
    assert(nativesRecord.get(5).isInstanceOf[java.lang.Boolean])
  }

  it should "read maps as mutable.HashMap[Any,Any]" in {
    val hashMap = new HashMap[String, java.util.ArrayList[Long]]()
    hashMap.put("entry1", new ArrayList(java.util.Arrays.asList(new Long(1), new Long(2))))
    hashMap.put("entry2", new ArrayList(java.util.Arrays.asList(new Long(3), new Long(4))))

    val treeMap = new TreeMap[String, java.util.ArrayList[Long]]()
    treeMap.put("entry1", new ArrayList(java.util.Arrays.asList(new Long(1), new Long(2))))
    treeMap.put("entry2", new ArrayList(java.util.Arrays.asList(new Long(3), new Long(4))))

    val hashMapData = immutable.Map[String, Object]("map" -> hashMap)
    val hashMapRecord: GenericRecord = AvroDataUtils.mapToGenericRecord(hashMapData, TestSchemas.MAP_SCHEMA_SPEC)
    assert(mapsAreEqual(hashMap, hashMapRecord.get(0)))
    
    val treeMapData = immutable.Map[String, Object]("map" -> treeMap)
    val treeMapRecord: GenericRecord = AvroDataUtils.mapToGenericRecord(treeMapData, TestSchemas.MAP_SCHEMA_SPEC)
    assert(mapsAreEqual(treeMap, treeMapRecord.get(0)))    
  }

  private def mapsAreEqual(original: Any, retrieved: Any): Boolean = {

    val retrievedMap = retrieved.asInstanceOf[mutable.HashMap[Any, Any]]
    val scalaMap = original.asInstanceOf[java.util.Map[Any, java.util.ArrayList[Any]]].asScala
    
    for ((key, value) <- scalaMap.iterator) {
      val seq1 = retrievedMap.get(key).get.asInstanceOf[Seq[Any]].toArray
      val seq2 = value.toArray()
      if (seq1 != seq2) {
        false
      }
    }
    true
  }
}