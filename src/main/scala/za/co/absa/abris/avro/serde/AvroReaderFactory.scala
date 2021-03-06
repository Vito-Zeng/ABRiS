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

package za.co.absa.abris.avro.serde

import org.apache.avro.Schema
import za.co.absa.abris.avro.format.ScalaAvroRecord
import za.co.absa.abris.avro.parsing.utils.AvroSchemaUtils
import za.co.absa.abris.avro.read.ScalaDatumReader
import za.co.absa.abris.avro.read.confluent.{ScalaConfluentKafkaAvroDeserializer, SchemaManager}

/**
  * This object works as factory for Avro readers. It can create Avro readers from [[Schema]] objects and also from
  * schema paths in the local file system. Also, it can create specialized Confluent Avro readers that know how to cope
  * with the magic bytes on top of the payload.
  */
private[avro] object AvroReaderFactory {

  /**
    * Creates an instance of ScalaDatumReader for the schema informed.
    */
  def createAvroReader(schemaPath: String): ScalaDatumReader[ScalaAvroRecord] = {
    createAvroReader(AvroSchemaUtils.load(schemaPath))
  }

  /**
    * Creates an instance of ScalaDatumReader for the schema informed.
    */
  def createAvroReader(schema: Schema): ScalaDatumReader[ScalaAvroRecord] = {
    new ScalaDatumReader[ScalaAvroRecord](schema)
  }

  /**
    * Creates an instance of [[ScalaConfluentKafkaAvroDeserializer]] and configures its Schema Registry access in case
    * the parameters to do it are defined.
    */
  def createConfiguredConfluentAvroReader(schemaPath: Option[String], schemaRegistryConf: Option[Map[String,String]]): ScalaConfluentKafkaAvroDeserializer = {
    val schema = if (schemaPath.isDefined) Some(AvroSchemaUtils.load(schemaPath.get)) else None
    val configs = if (schemaRegistryConf.isDefined) schemaRegistryConf.get else Map[String,String]()
    val topic = if (configs.contains(SchemaManager.PARAM_SCHEMA_REGISTRY_TOPIC)) Some(configs(SchemaManager.PARAM_SCHEMA_REGISTRY_TOPIC)) else None

    val reader = new ScalaConfluentKafkaAvroDeserializer(topic, schema)
    reader.configureSchemaRegistry(configs)
    reader
  }
}
